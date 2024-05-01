package com.example.projekti_tehtava_muistuttaja.Handler;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projekti_tehtava_muistuttaja.Adapter.TaskAdapter;
import com.example.projekti_tehtava_muistuttaja.Broadcast.AlarmBroadcast;
import com.example.projekti_tehtava_muistuttaja.Broadcast.ReminderBroadcast;
import com.example.projekti_tehtava_muistuttaja.EditTaskActivity;
import com.example.projekti_tehtava_muistuttaja.Firebase.FirebaseHandler;
import com.example.projekti_tehtava_muistuttaja.Login_Register.LoginActivity;
import com.example.projekti_tehtava_muistuttaja.MainActivity;
import com.example.projekti_tehtava_muistuttaja.Model.TaskModel;
import com.example.projekti_tehtava_muistuttaja.NewTaskActivity;
import com.example.projekti_tehtava_muistuttaja.R;
import com.example.projekti_tehtava_muistuttaja.RecyclerItemTouchHelper;
import com.example.projekti_tehtava_muistuttaja.ViewTask;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

// Luokka sovelluksen perustoiminnallisuuden toteuttamiselle
// Perii AppCompatActivity-luokan ja toteuttaa TaskAdapter-luokan klikinkuuntelija-rajapinnan
public class AppHandler extends AppCompatActivity implements TaskAdapter.ItemClickListener {
    // String-vakiot ilmoituskanaville
    public static final String REMINDER_CHANNEL_ID = "MUISTUTTAJA_KANAVA";
    public static final String ALARM_CHANNEL_ID = "HALYTTAJA_KANAVA";

    // String-vakiot tehtävän muokkaamiselle
    public static final String TASK_TITLE = "TASK_TITLE";
    public static final String TASK_INFO = "TASK_INFO";
    public static final String TASK_ID = "TASK_ID";
    public static final String TASK_DUE_DATE = "TASK_DUE_DATE";
    public static final String TASK_REMINDER_ID = "TASK_REMINDER_ID";
    public static final String TASK_ALARM_ID = "TASK_ALARM_ID";
    public static final String TASK_REMINDER_DATE = "TASK_REMINDER_DATE";
    public static final String TASK_REMINDER_TIME = "TASK_REMINDER_TIME";
    public static final String TASK_ALARM_DATE = "TASK_ALARM_DATE";
    public static final String TASK_ALARM_TIME = "TASK_ALARM_TIME";
    public static final String TASK_POSITION = "TASK_POSITION";

    // Int-vakiot activityResultille, tultiinko luomasta uutta vai muokkaamasta vanhaa tehtävää
    private static final int NEW_TASK_ACTIVITY_REQUEST_CODE = 1;
    private static final int EDIT_TASK_ACTIVITY_REQUEST_CODE = 2;

    public static MainActivity mainActivity;
    private RecyclerView taskRecyclerView;
    private ItemTouchHelper itemTouchHelper;

    public TaskAdapter tasksAdapter;
    private FirebaseHandler firebaseHandler;

    private static AlarmManager alarmManager;

    // Konstruktorissa luodaan uusi FirebaseHandler-olio, RecyclerView ja TaskAdapter
    // Lisäksi asetetaan ItemClickListener ja ItemTouchHelper, sekä luodaan
    // muistutus- ja hälytyskanavat ja alustetaan alarmManager
    public AppHandler(MainActivity mA) {
        mainActivity = mA;

        firebaseHandler = new FirebaseHandler(this);

        taskRecyclerView = mainActivity.findViewById(R.id.tasksRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        tasksAdapter = new TaskAdapter(mainActivity);

        tasksAdapter.setClickListener((TaskAdapter.ItemClickListener) this);
        taskRecyclerView.setAdapter(tasksAdapter);

        itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(mainActivity, this));
        itemTouchHelper.attachToRecyclerView(taskRecyclerView);

        createReminderNotificationChannel();
        createAlarmNotificationChannel();
        alarmManager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);
    }

    // Metodi, jolla päivitetään tehtävät käyttöliittymässä FirebaseHandler-luokan avulla
    public void checkTaskUpdates() {
        firebaseHandler.checkFirebaseChanges();
    }

    // Haetaan tehtävä tietystä positiosta
    public TaskModel getTaskFromPosition(int position) {
        return tasksAdapter.getTaskFromPosition(position);
    }

    // Lisää uuden tehtävän Firebase-kantaan ja päivittää käyttöliittymän
    public void addTask(TaskModel task) {
        firebaseHandler.addTask(task);
        checkTaskUpdates();
    }

    // Metodi avaa muokkausnäkymän tehtävän muokkaamiseksi
    // Lähettää aktivitettille vanhat tehtävän arvot, jotta ne voidaan asettaa näkymässä elemtteihin
    public void editTask(int position) {
        TaskModel task = getTaskFromPosition(position);
        Intent intent = new Intent(mainActivity.getApplicationContext(), EditTaskActivity.class);
        intent.putExtra(TASK_TITLE, task.getTaskTitle());
        intent.putExtra(TASK_INFO, task.getTaskInfo());
        intent.putExtra(TASK_ID, task.getTaskID());
        intent.putExtra(TASK_DUE_DATE, task.getTaskDueDate());
        intent.putExtra(TASK_POSITION, String.valueOf(position));
        intent.putExtra(TASK_ALARM_ID, task.getAlarmID());
        intent.putExtra(TASK_REMINDER_ID, task.getReminderID());
        intent.putExtra(TASK_REMINDER_DATE, task.getReminderDate());
        intent.putExtra(TASK_REMINDER_TIME, task.getReminderTime());
        intent.putExtra(TASK_ALARM_DATE, task.getAlarmDate());
        intent.putExtra(TASK_ALARM_TIME, task.getAlarmTime());
        mainActivity.startActivityForResult(intent, EDIT_TASK_ACTIVITY_REQUEST_CODE);
    }

    // Metodi, joka poistaa yhden tehtävän ja kaikki siihen liittyvät muistutukset ja hälytykset
    public void deleteTask(TaskModel task) {
        firebaseHandler.deleteTask(task);
        cancelReminder(task.getReminderID());
        cancelAlarm(task.getAlarmID());
        checkTaskUpdates();
    }

    // Metodi, joka poistaa kaikki tehtävät ja niiden liittyvät muistutukset ja hälytykset
    public void deleteAllTasks() {
        List<TaskModel> taskList = firebaseHandler.getAllTasksFromFirebase();
        for (TaskModel task : taskList) {
            firebaseHandler.deleteTask(task);
            cancelReminder(task.getReminderID());
            cancelAlarm(task.getAlarmID());
        }
        checkTaskUpdates();
        Log.d(MainActivity.TAG, "Poistettiin kaikki tehtävät");
    }

    // Toteuttaa TaskAdapter-luokan rajapinnan toteutuksen tehtävää klikatessa
    @Override
    public void onItemClick(View view, int position) {
        // Haetaan valitun tehtävän tiedot adapterista ja pakataan ne Bundle-olioon, jonka avulla
        // ViewTask-fragmentti käynnistetään. Fragmentti näyttää tehtävän yksityiskohdat
        TaskModel task = tasksAdapter.getTaskFromPosition(position);
        Bundle bundle = new Bundle();
        bundle.putString("title", task.getTaskTitle());
        bundle.putString("info", task.getTaskInfo());
        bundle.putString("duedate", task.getTaskDueDate());
        bundle.putString("reminderdate", task.getReminderDate());
        bundle.putString("remindertime", task.getReminderTime());
        bundle.putString("alarmdate", task.getAlarmDate());
        bundle.putString("alarmtime", task.getAlarmTime());
        ViewTask fragment = new ViewTask();
        fragment.setArguments(bundle);
        fragment.show(mainActivity.getSupportFragmentManager(), MainActivity.TAG);
    }

    // Tarkastaa käyttäjän valtuutuksen. Jos valtuutus ei ole kunnossa, avataan LoginActivity.
    // Muuten jos metodia kutsuttiin kirjautuneena olevan käyttäjän tarkistamiseksi, näytetään
    // Toast tämän sähköpostista. Muuten tarkistetaan päivitykset Firebasesta
    public void checkUserValidation(boolean checkLoginStatus) {
        if (!firebaseHandler.isUserAuthValid()) {
            mainActivity.startActivity(new Intent(mainActivity, LoginActivity.class));
        } else {
            if (!checkLoginStatus) {
                checkTaskUpdates();
            } else {
                Toast.makeText(mainActivity, "Kirjautuneena: " + firebaseHandler.getUserEmail(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Kirjataan käyttäjä ulos FirebaseHandler-luokan avulla. Jos destroy on tosi, siirrytään LoginAcitivityyn
    public void signUserOut(boolean destroy) {
        firebaseHandler.signUserOut(mainActivity, destroy);
    }

    // Käsittelee uuden tehtävän lisäämiseen käytetyn napin siirtymällä aktiviteettiin NewTaskActivityyn
    public void buttonAddPressed() {
        Intent intent = new Intent(mainActivity.getApplicationContext(), NewTaskActivity.class);
        mainActivity.startActivityForResult(intent, NEW_TASK_ACTIVITY_REQUEST_CODE);
    }

    // Käsittelee kaikkien tehtävien poistamiseen käytetyn napin painalluksen avaamalla
    // varmistusikkunan confirmDeleteAllTasks-metodin avulla
    public void buttonDeletePressed() {
        mainActivity.confirmDeleteAllTasks();
    }

    // Luo varmistusikkunan yhtä tehtävää poistettaessa
    public void confirmDeleteTask(int position) {
        TaskModel task = tasksAdapter.getTaskFromPosition(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(false);
        builder.setTitle("Vahvista tehtävän poisto");
        builder.setMessage("Oletko varma, että haluat poistaa tehtävän: " + task.getTaskTitle() + "?");
        builder.setPositiveButton("Vahvista",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTask(tasksAdapter.getTaskFromPosition(position));
                    }
                });
        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tasksAdapter.notifyItemChanged(position);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Käsittelee EditTaskActivityn ja NewTaskActivityn palauttamaa dataa
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Jos pyyntykoodi oli uuden tehtävän luomisesta, luodaan uusi tehtävä annettujen arvojen perusteella
        if (requestCode == NEW_TASK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            String word = data.getStringExtra(NewTaskActivity.EXTRA_REPLY);

            // Erotellaan data palautetusta merkkijonosta
            String[] splitted = word.split(";");
            String taskTitle = splitted[0];
            String taskInfo = splitted[1];
            String taskID = UUID.randomUUID().toString();
            String taskDueDate = splitted[2];
            String taskReminderDate = splitted[3];
            String taskReminderTime = splitted[4];
            String taskAlarmDate = splitted[5];
            String taskAlarmTime = splitted[6];

            // Luodaan uusi tehtävä-olio
            TaskModel newTask = new TaskModel();
            newTask.setTaskTitle(taskTitle);
            newTask.setTaskInfo(taskInfo);
            newTask.setTaskID(taskID);
            newTask.setTaskDueDate(taskDueDate);
            newTask.setReminderDate(taskReminderDate);
            newTask.setReminderTime(taskReminderTime);
            newTask.setAlarmDate(taskAlarmDate);
            newTask.setAlarmTime(taskAlarmTime);

            // Jos käyttäjä asetti ilmoituksen tai hälytyksen, luodaan hälytys ja/tai
            // setAlarm ja setReminder-metodien avulla
            boolean setReminder = false;
            boolean setAlarm = false;

            if (!taskReminderDate.equals("null") && !taskReminderTime.equals("null")) {
                int reminderId = new Random().nextInt(Integer.MAX_VALUE);
                newTask.setReminderID(reminderId);
                setReminder = true;
            }
            if (!taskAlarmDate.equals("null") && !taskAlarmTime.equals("null")) {
                int alarmId = new Random().nextInt(Integer.MAX_VALUE);
                newTask.setAlarmID(alarmId);
                setAlarm = true;
            }
            if (setReminder) {
                setReminder(newTask);
            }
            if (setAlarm) {
                setAlarm(newTask);
            }
            // Lisätään luotu tehtävä Firebaseen ja päivitetään käyttöliittymä
            addTask(newTask);

            // Jos pyyntykoodi oli vanhan tehtävän muokkaamisesta, muokataan tehtävää annettujen arvojen perusteella
        } else if (requestCode == EDIT_TASK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            String word = data.getStringExtra(EditTaskActivity.EXTRA_REPLY);

            // Erotellaan data palautetusta merkkijonosta
            String[] splitted = word.split(";");
            int position = Integer.parseInt(splitted[0]);
            String taskTitle = splitted[1];
            String taskInfo = splitted[2];
            String taskDueDate = splitted[3];
            String taskReminderDate = splitted[4];
            String taskReminderTime = splitted[5];
            String taskAlarmDate = splitted[6];
            String taskAlarmTime = splitted[7];
            int wasReminder = Integer.parseInt(splitted[8]);
            int wasAlarm = Integer.parseInt(splitted[9]);

            // Muokataan vanhaa tehtävää annettujen arvojen perusteella
            TaskModel task = tasksAdapter.getTaskFromPosition(position);
            task.setTaskTitle(taskTitle);
            task.setTaskInfo(taskInfo);
            task.setTaskDueDate(taskDueDate);
            task.setReminderDate(taskReminderDate);
            task.setReminderTime(taskReminderTime);
            task.setAlarmDate(taskAlarmDate);
            task.setAlarmTime(taskAlarmTime);

            // Jos käyttäjä asetti ilmoituksen tai hälytyksen, luodaan hälytys ja/tai
            // setAlarm ja setReminder-metodien avulla
            if (wasReminder == 0) {
                if (!taskReminderDate.equals("null") && !taskReminderTime.equals("null")) {
                    int reminderId = new Random().nextInt(Integer.MAX_VALUE);
                    task.setReminderID(reminderId);
                    setReminder(task);
                }
            }
            if (wasAlarm == 0) {
                if (!taskAlarmDate.equals("null") && !taskAlarmTime.equals("null")) {
                    int alarmId = new Random().nextInt(Integer.MAX_VALUE);
                    task.setAlarmID(alarmId);
                    setAlarm(task);
                }
            }
            // Muokataan kyseistä tehtävää vielä Firebasessa ja päivitetään käyttöliittymä
            firebaseHandler.editTask(task);
            tasksAdapter.notifyItemChanged(position);
        }
    }

    // Asettaa muistutuksen tehtävälle
    private void setReminder(TaskModel task) {
        // Haetaan muistutuksen päivämäärän ja ajan tiedot muutetaan ne kokonaisluvuiksi
        String[] sDateInfo = task.getReminderDate().split("-");
        int[] dateInfo = new int[3];
        dateInfo[0] = Integer.parseInt(sDateInfo[0]);
        dateInfo[1] = Integer.parseInt(sDateInfo[1]);
        dateInfo[2] = Integer.parseInt(sDateInfo[2]);

        String[] sTimeInfo = task.getReminderTime().split(":");
        int[] timeInfo = new int[2];
        timeInfo[0] = Integer.parseInt(sTimeInfo[0]);
        timeInfo[1] = Integer.parseInt(sTimeInfo[1]);

        // Luodaan uusi kalenteri-instanssi ja asetetaan siihen päivämäärä ja aika
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateInfo[0], dateInfo[1], dateInfo[2], timeInfo[0], timeInfo[1]);

        // Luodaan uusi Intent ReminderBroadcast-luokasta ja annetaan sille tehtävän tiedot
        Intent intent = new Intent(mainActivity, ReminderBroadcast.class);
        intent.putExtra(ReminderBroadcast.REMINDER_TITLE, task.getTaskTitle());
        intent.putExtra(ReminderBroadcast.REMINDER_INFO, task.getTaskInfo());
        intent.putExtra(ReminderBroadcast.REMINDER_ID, task.getReminderID());

        // Luodaan uusi PendingIntent, joka odottaa käynnistystä ilmoituksen saapuessa
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mainActivity, task.getReminderID(), intent, PendingIntent.FLAG_IMMUTABLE);
        // Asetetaan tarkka ilmoitussajankohta AlarmManageriin
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d(MainActivity.TAG, "Asetettu muistutus ajankohdalle: " + task.getReminderDate() + " klo:" + task.getReminderTime());
    }

    // Asettaa hälytyksen tehtävälle
    private void setAlarm(TaskModel task) {
        // Haetaan hälytyksen päivämäärän ja ajan tiedot muutetaan ne kokonaisluvuiksi
        String[] sDateInfo = task.getAlarmDate().split("-");
        int[] dateInfo = new int[3];
        dateInfo[0] = Integer.parseInt(sDateInfo[0]);
        dateInfo[1] = Integer.parseInt(sDateInfo[1]);
        dateInfo[2] = Integer.parseInt(sDateInfo[2]);

        String[] sTimeInfo = task.getAlarmTime().split(":");
        int[] timeInfo = new int[2];
        timeInfo[0] = Integer.parseInt(sTimeInfo[0]);
        timeInfo[1] = Integer.parseInt(sTimeInfo[1]);

        // Luodaan uusi kalenteri-instanssi ja asetetaan siihen päivämäärä ja aika
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateInfo[0], dateInfo[1], dateInfo[2], timeInfo[0], timeInfo[1]);

        // Luodaan uusi Intent AlarmBroadcast-luokasta ja annetaan sille tehtävän tiedot
        Intent intent = new Intent(mainActivity, AlarmBroadcast.class);
        intent.putExtra(AlarmBroadcast.ALARM_TITLE, task.getTaskTitle());
        intent.putExtra(AlarmBroadcast.ALARM_INFO, task.getTaskInfo());
        intent.putExtra(AlarmBroadcast.ALARM_ID, task.getAlarmID());

        // Luodaan uusi PendingIntent, joka odottaa käynnistystä hälytyksen saapuessa
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mainActivity, task.getAlarmID(), intent, PendingIntent.FLAG_IMMUTABLE);
        // Asetetaan tarkka ilmoitussajankohta AlarmManageriin
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d(MainActivity.TAG, "Asetettu hälytys ajankohdalle: " + task.getAlarmDate() + " klo: " + task.getAlarmTime());
    }

    // Metodi luo muistutusilmoituksille ilmoituskanavan
    private void createReminderNotificationChannel() {
        // Jos API on yli 26, tarvitaan notificationChannel, muuten ei
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Asetetaan kanavan tiedot
            CharSequence name = "MuistuttajaKanava";
            String description = "Kanava muistutuksille";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(REMINDER_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.YELLOW);
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);
            channel.setVibrationPattern(new long[]{1000});
            Uri uri = Uri.parse("android.resource://" + mainActivity.getPackageName() + "/" + R.raw.notification);
            AudioAttributes att = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
            channel.setSound(uri, att);

            // Haetaan NotificationManager ja luodaan kanava
            NotificationManager notificationManager = mainActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Log.d(MainActivity.TAG, "Luotu notifikaatiokanava muistutuksille: " + channel.getName());
        }
    }

    // Metodi luo hälytysilmoituksille ilmoituskanavan
    private void createAlarmNotificationChannel() {
        // Jos API on yli 26, tarvitaan notificationChannel, muuten ei
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Asetetaan kanavan tiedot
            CharSequence name = "HälyttäjäKanava";
            String description = "Kanava hälytyksille";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(ALARM_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.YELLOW);
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);

            // Haetaan NotificationManager ja luodaan kanava
            NotificationManager notificationManager = mainActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Log.d(MainActivity.TAG, "Luotu notifikaatiokanava hälytyksille: " + channel.getName());
        }
    }

    // Peruu muistutuksen sen id:n avulla
    public static void cancelReminder(int reminderId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity);
        // Tarkastetaan, onko muistutusta luotu, jos id on -1, muistutusta ei ole luotu
        if (reminderId != -1) {
            // Luodaan uusi intent ja pendingIntent, jotka vastaavat käyttäjän määrittämää muistutusta
            Intent intent = new Intent(mainActivity, ReminderBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mainActivity, reminderId, intent, PendingIntent.FLAG_IMMUTABLE);
            // Peruutetaan käyttäjän määrittämä muistutus
            alarmManager.cancel(pendingIntent);
            notificationManager.cancel(reminderId);
            Log.d(MainActivity.TAG, "Peruttiin muistutus id: " + reminderId);
        }
    }

    // Peruu hälytyksen sen id:n avulla
    public static void cancelAlarm(int alarmId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity);
        // Tarkastetaan, onko hälytystä luoto, jos id on -1, hälytystä ei ole luotu
        if (alarmId != -1) {
            // Luodaan uusi intent ja pendingIntent, jotka vastaavat käyttäjän määrittämää hälytystä
            Intent intent = new Intent(mainActivity, AlarmBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mainActivity, alarmId, intent, PendingIntent.FLAG_IMMUTABLE);
            // Peruutetaan käyttäjän määrittämä muistutus
            alarmManager.cancel(pendingIntent);
            notificationManager.cancel(alarmId);
            Log.d(MainActivity.TAG, "Peruttiin hälytys id: " + alarmId);
        }
    }
}