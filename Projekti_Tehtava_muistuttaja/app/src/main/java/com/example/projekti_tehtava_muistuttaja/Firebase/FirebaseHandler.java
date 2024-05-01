package com.example.projekti_tehtava_muistuttaja.Firebase;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.projekti_tehtava_muistuttaja.Handler.AppHandler;
import com.example.projekti_tehtava_muistuttaja.Login_Register.LoginActivity;
import com.example.projekti_tehtava_muistuttaja.MainActivity;
import com.example.projekti_tehtava_muistuttaja.Model.TaskModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

// Luokka Firebase-tietokannan kanssa kommunikoimiseen, sekä autentikointiin
public class FirebaseHandler {

    private AppHandler appHandler;
    private FirebaseFirestore mFirestore;
    private CollectionReference tasksReference;
    private FirebaseAuth mAuth;
    private List<TaskModel> taskList;

    // Konstruktori, joka ottaa vastaan AppHandler-olion ja luo tarvittavat yhteydet Firebaseen,
    // sekä hakee autentikoinnin
    public FirebaseHandler(AppHandler appHandler) {
        this.appHandler = appHandler;
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        taskList = new ArrayList<>();
        if (mAuth.getCurrentUser() != null) {
            tasksReference = mFirestore.collection("users").document(getUserID()).collection("tasks");
        }
    }

    // Metodi, joka palauttaa kirjautuneen käyttäjän UID:n
    public String getUserID() {
        return mAuth.getUid();
    }

    // Metodi, joka palauttaa kirjautuneen käyttäjän sähköpostiosoitteen
    public String getUserEmail() {
        return Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
    }

    // Metodi, joka palauttaa listan kaikista käyttäjän Firebase-tietokannassa tallennetuista tehtävistä
    public List<TaskModel> getAllTasksFromFirebase() {
        return taskList;
    }

    // Metodi, joka hakee käyttäjän tallentamat tehtävät firebasesta ja lähettää päivitetyn listan adapterille
    public void checkFirebaseChanges() {
        tasksReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null) {
                    taskList.clear();
                    Log.d(MainActivity.TAG, "löydettiin tehtäviä firebasesta");

                    // Käy läpi kaikki dokumentit Firebasessa ja lisää ne listaan
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        TaskModel task = snapshot.toObject(TaskModel.class);
                        if (task != null && task.getTaskID() != null) {
                            taskList.add(task);
                        }
                    }
                    // Lajittele tehtävät ensisijaisesti määräpäivämäärän mukaan, muuten nimen mukaan
                    Collections.sort(taskList, new Comparator<TaskModel>() {
                        @Override
                        public int compare(TaskModel task1, TaskModel task2) {
                            String date1 = task1.getTaskDueDate();
                            String date2 = task2.getTaskDueDate();
                            if (date1.equals("null") && date2.equals("null")) {
                                return task1.getTaskTitle().compareTo(task2.getTaskTitle());
                            } else if (date1.equals("null")) {
                                return 1;
                            } else if (date2.equals("null")) {
                                return -1;
                            } else {
                                Date d1 = parseDate(date1);
                                Date d2 = parseDate(date2);
                                if (d1.equals(d2)) {
                                    return task1.getTaskTitle().compareTo(task2.getTaskTitle());
                                } else {
                                    return d1.compareTo(d2);
                                }
                            }
                        }
                    });
                    // Päivitä tehtävät käyttöliittymässä adapterin avulla
                    if (appHandler != null && appHandler.tasksAdapter != null) {
                        appHandler.tasksAdapter.updateTasks(taskList);
                    }
                } else {
                    Log.d(MainActivity.TAG, "Querysnapshot on null");
                }
            }
        });
    }

    // Metodi, joka lisää uuden tehtävän Firebaseen
    public void addTask(TaskModel task) {
        tasksReference = mFirestore.collection("users").document(getUserID()).collection("tasks");
        tasksReference.document(task.getTaskID()).set(task);
        Log.d(MainActivity.TAG, "Uusi tehtävä: " + task.getTaskTitle() + " lisättiin");
    }

    // Metodi, joka päivittää tietyn tehtävän tiedot Firebasessa
    public void editTask(TaskModel task) {
        DocumentReference documentReference = tasksReference.document(task.getTaskID());
        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("taskID", task.getTaskID());
        updatedFields.put("taskTitle", task.getTaskTitle());
        updatedFields.put("taskInfo", task.getTaskInfo());
        updatedFields.put("taskDueDate", task.getTaskDueDate());
        updatedFields.put("reminderID", task.getReminderID());
        updatedFields.put("reminderDate", task.getReminderDate());
        updatedFields.put("reminderTime", task.getReminderTime());
        updatedFields.put("alarmID", task.getAlarmID());
        updatedFields.put("alarmDate", task.getAlarmDate());
        updatedFields.put("alarmTime", task.getAlarmTime());

        documentReference.update(updatedFields)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(MainActivity.TAG, "Tehtävä " + task.getTaskTitle() + " päivitetty firebasessa onnistuneesti");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(MainActivity.TAG, "Virhe päivitettäessä tehtävää " + task.getTaskTitle() + " firebasessa", e);
                    }
                });
    }

    // Metodi, joka poistaa tehtävän Firebasesta
    public void deleteTask(TaskModel task) {
        tasksReference.document(task.getTaskID())
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(MainActivity.TAG, "Tehtävä " + task.getTaskTitle() + " poistettu.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(MainActivity.TAG, "Tehtävää " + task.getTaskTitle() + " ei saatu poistetttua.");
                    }
                });
    }

    // Metodi, joka tarkistaa, onko käyttäjän autentikointi kelvollinen
    public boolean isUserAuthValid() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return false;
        }
        else {
            return true;
        }
    }

    // Metodi, joka kirjaa käyttäjän ulos Firebase Authentication-palvelusta
    public void signUserOut(Context ctx, boolean destroy) {
        AuthUI.getInstance().signOut(ctx).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(MainActivity.TAG, "Uloskirjauduttu onnistuneesti");
                Toast.makeText(ctx, "Uloskirjauduttu onnistuneesti", Toast.LENGTH_LONG).show();
            }
        });
        // Jos käyttäjä itse kirjautui ulos, siirrytään aktiviteetti LoginActivityyn
        if (!destroy) {
            ctx.startActivity(new Intent(ctx, LoginActivity.class));
        } else {
            mAuth = null;
        }
    }

    // Metodi päivämäärän parsimiseen annetusta merkkijonosta, joka palauttaa tämän Date-objektina
    private static Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
