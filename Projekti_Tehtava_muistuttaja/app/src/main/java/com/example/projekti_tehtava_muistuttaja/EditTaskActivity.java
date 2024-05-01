package com.example.projekti_tehtava_muistuttaja;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.projekti_tehtava_muistuttaja.Handler.AppHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;

// Luokka, joka käsittelee tehtävän muokkaamisnäkymän toiminnan, perii luokan AppCompatActivity
public class EditTaskActivity extends AppCompatActivity {
    public static final String EXTRA_REPLY = "com.example.android.projekti_tehtava_muistuttaja.REPLY";

    // Konteksti
    private EditTaskActivity eta;

    // Muuttujat tehtävän tiedoista
    private String dueDate;
    private String dueDateNonFormat;
    private int reminderId;
    private String reminderDate = "";
    private String reminderTime = "";
    private String reminderDateNonFormat;
    private String reminderTimeNonFormat;
    private int alarmId;
    private String alarmDate = "";
    private String alarmTime = "";
    private String alarmDateNonFormat;
    private String alarmTimeNonFormat;
    private String position;

    // Muuttujat tiedoille siitä, oliko tehtävällä aikaisemmin muistutus tai hälytys
    private boolean wasReminder = false;
    private boolean wasAlarm = false;

    // Näkymän elementit
    private TextInputEditText editTextTaskTitle;
    private TextInputEditText editTextTaskInfo;
    private TextView textViewDueDateInfo;
    private TextView textViewReminderInfo;
    private SwitchCompat buttonDueDate;
    private SwitchCompat buttonReminder;
    private TextView textViewAlarmInfo;
    private SwitchCompat buttonAlarm;
    private Button buttonSave;
    private FloatingActionButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asetetaan sovellusmuoto (night mode) pois päältä
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Lukitaan screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Käytetään layoutia edit_task_activity
        setContentView(R.layout.edit_task_activity);

        eta = this;

        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextTaskInfo = findViewById(R.id.editTextTaskInfo);
        textViewDueDateInfo = findViewById(R.id.textViewDueDate);
        textViewReminderInfo = findViewById(R.id.textViewReminderInfo);
        buttonDueDate = findViewById(R.id.buttonDueDate);
        buttonReminder = findViewById(R.id.buttonReminder);
        textViewAlarmInfo = findViewById(R.id.textViewAlarmInfo);
        buttonAlarm = findViewById(R.id.buttonAlarm);
        buttonSave = findViewById(R.id.buttonSave);

        // Asetetaan floating action buttonin väri manuaalisesti valkoiseksi
        ColorStateList csl = ColorStateList.valueOf(getResources().getColor(R.color.white));
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setImageTintList(csl);

        // Asetetaan elementteihin tehtävän vanhat tiedot
        checkPreviousData();

        // Asetetaan määräpäivän valitsemiselle käytetävälle napille kuuntelija
        buttonDueDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Jos nappi asetettiin päälle, avataan päivämäärän valitsemiseksi ikkuna
                if (isChecked) {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(eta, R.style.CustomDatePickerDialog, new DatePickerDialog.OnDateSetListener() {
                        // Vaihdetaan muuttujien arvot valittujen perusteella muotoiltuna ja ilman muotoilua
                        // Asetetaan elementtiin textViewDueDateInfo myös valittu päivämäärä
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            dueDateNonFormat = year + "-" + month + "-" + day;
                            DateTimePickHandler dtph = new DateTimePickHandler(null, null, false);
                            dueDate = dtph.formatDate(year, month, day);
                            textViewDueDateInfo.setText(getResources().getString(R.string.current_due_date_text, dueDate));
                        }
                    }, year, month, day);
                    datePickerDialog.setTitle("Valitse määräpäivä");
                    // Asetetaan, että päivän voi valita vain nykyhetkestä eteenpäin
                    datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                    datePickerDialog.show();
                } else {
                    // Muuten, jos nappi asetettiin pois päältä, muutetaan arvot "null" ja päivitetään elementin teksti
                    dueDate = "null";
                    dueDateNonFormat = "null";
                    textViewDueDateInfo.setText(getResources().getString(R.string.no_current_due_date_text));
                }
            }
        });

        // Asetetaan muistutuksen valitsemiselle käytetävälle napille kuuntelija
        buttonReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Jos tarvitut oikeudet on annettu, asetetaan muistutus DateTimePickHandler-luokan avulla
                if (MainActivity.allPermissionsGranted()) {
                    if (isChecked) {
                        new DateTimePickHandler(null, eta, true);
                    } else {
                        // Jos tehtävälle oli aikaisemmin valittu muistutus, perutaan vanha
                        if (wasReminder) {
                            AppHandler.cancelReminder(reminderId);
                            wasReminder = false;
                        }
                         // Asetetaan muuttujien arvot "null"
                        updateReminderValues(true, reminderDate, reminderTime, reminderDateNonFormat, reminderTimeNonFormat);
                    }
                } else {
                    // Jos tarvittavia oikeuksia ei ole annettu, infotaan tästä Toastilla ja asetetaan nappi unchecked
                    Toast.makeText(eta, "Ei tarvittavia oikeuksia. Voit antaa oikeudet kotiruudun menusta.",
                            Toast.LENGTH_LONG).show();
                    buttonReminder.setChecked(false);
                }
            }
        });

        // Asetetaan hälytyksen valitsemiselle käytetävälle napille kuuntelija
        buttonAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Jos tarvitut oikeudet on annettu, asetetaan hälytys DateTimePickHandler-luokan avulla
                if (MainActivity.allPermissionsGranted()) {
                    if (isChecked) {
                        new DateTimePickHandler(null, eta, false);
                    } else {
                        // Jos tehtävälle oli aikaisemmin valittu hälytys, perutaan vanha
                        if (wasAlarm) {
                            AppHandler.cancelAlarm(alarmId);
                            wasAlarm = false;
                        }
                        // Asetetaan muuttujien arvot "null"
                        updateAlarmValues(true, alarmDate, alarmTime, alarmDateNonFormat, alarmTimeNonFormat);
                    }
                } else {
                    // Jos tarvittavia oikeuksia ei ole annettu, infotaan tästä Toastilla ja asetetaan nappi unchecked
                    Toast.makeText(eta, "Ei tarvittavia oikeuksia. Voit antaa oikeudet kotiruudun menusta.",
                            Toast.LENGTH_LONG).show();
                    buttonAlarm.setChecked(false);
                }
            }
        });

        // Asetetaan tehtävän tallentamiselle käytettävälle napille kuuntelija
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent replyIntent = new Intent();
                // Jos tarvittavat kentät ovat tyhjiä, ilmoitetaan tästä
                if (TextUtils.isEmpty(editTextTaskInfo.getText()) || TextUtils.isEmpty(editTextTaskTitle.getText())) {
                    setResult(RESULT_CANCELED, replyIntent);
                    Toast.makeText(getApplicationContext(), "Täytä kaikki kentät muokkaaksesi tehtävää",
                            Toast.LENGTH_LONG).show();
                } else {
                    // Asetetaan asetetut tiedot yhteen merkkijonoon erotettuna puolipisteellä
                    String task = position + ";" + editTextTaskTitle.getText().toString() + ";" + editTextTaskInfo.getText().toString() + ";" + dueDateNonFormat
                            + ";" + reminderDateNonFormat + ";" + reminderTimeNonFormat + ";" + alarmDateNonFormat + ";" + alarmTimeNonFormat;
                    // Loppuun asetetaan vielä totuusarvot numeroina siitä oliko tehtävillä aiemmin
                    // muistutukset ja/tai hälytykset asetettuna
                    if (wasReminder) {
                        task += ";" + 1;
                    } else {
                        task += ";" + 0;
                    }
                    if (wasAlarm) {
                        task += ";" + 1;
                    } else {
                        task += ";" + 0;
                    }
                    // Palataan takaisin RESULT_OK resultCode mukaan
                    Log.d(MainActivity.TAG, task);
                    replyIntent.putExtra(EXTRA_REPLY, task);
                    setResult(RESULT_OK, replyIntent);
                    finish();
                }
            }
        });

        // Asetetaan takaisin palaamiseen käytetyllä napille kuuntelija, jota painettaessa aktiviteetti suljetaan
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Metodi tehtävän vanhojen arvojen asettamiselle elementteihin
    private void checkPreviousData() {
        // Haetaan vanhat tiedot intentistä saatavien extra-arvojen perusteella
        Intent intent = getIntent();
        String taskTitle = intent.getStringExtra(AppHandler.TASK_TITLE);
        String taskInfo = intent.getStringExtra(AppHandler.TASK_INFO);
        position = intent.getStringExtra(AppHandler.TASK_POSITION);
        dueDateNonFormat = intent.getStringExtra(AppHandler.TASK_DUE_DATE);
        alarmId = intent.getIntExtra(AppHandler.TASK_ALARM_ID, -1);
        reminderId = intent.getIntExtra(AppHandler.TASK_REMINDER_ID, -1);
        reminderDateNonFormat = intent.getStringExtra(AppHandler.TASK_REMINDER_DATE);
        reminderTimeNonFormat = intent.getStringExtra(AppHandler.TASK_REMINDER_TIME);
        alarmDateNonFormat = intent.getStringExtra(AppHandler.TASK_ALARM_DATE);
        alarmTimeNonFormat = intent.getStringExtra(AppHandler.TASK_ALARM_TIME);

        // Asetetaan otsikko ja lisätiedot
        if (taskTitle != null && !taskTitle.equals("") && taskInfo != null && !taskInfo.equals("")) {
            editTextTaskTitle.setText(taskTitle);
            editTextTaskInfo.setText(taskInfo);
        }

        // Asetetaan määräpäivä, jos se oli aiemmin tehtävälle asetettu, muuten asetetaan no_current_due_date_text
        if (dueDateNonFormat != null && !dueDateNonFormat.equals("null")) {
            // Hyödynnetään DateTimePickHandler-luokan format-metodeja
            DateTimePickHandler dateTimePickHandler = new DateTimePickHandler(null, null, false);
            String[] dates = dueDateNonFormat.split("-");
            dueDate = dateTimePickHandler.formatDate(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));

            textViewDueDateInfo.setText(getResources().getString(R.string.current_due_date_text, dueDate));
            buttonDueDate.setChecked(true);
        } else {
            textViewDueDateInfo.setText(getResources().getString(R.string.no_current_due_date_text));
        }

        // Asetetaan muistutusajankohta, jos se oli aiemmin tehtävälle asetettu, muuten asetetaan no_current_reminder_time_text
        if (!reminderDateNonFormat.equals("null") && !reminderTimeNonFormat.equals("null")) {
            // Hyödynnetään DateTimePickHandler-luokan format-metodeja
            DateTimePickHandler dateTimePickHandler = new DateTimePickHandler(null, null, false);
            String[] dates = reminderDateNonFormat.split("-");
            reminderDate = dateTimePickHandler.formatDate(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));
            String[] times = reminderTimeNonFormat.split(":");
            reminderTime = dateTimePickHandler.formatTime(Integer.parseInt(times[0]), Integer.parseInt(times[1]));

            textViewReminderInfo.setText(getResources().getString(R.string.current_reminder_time_text, reminderDate, reminderTime));
            buttonReminder.setChecked(true);
            wasReminder = true;
        } else {
            textViewReminderInfo.setText(getResources().getString(R.string.no_current_reminder_time_text));
        }
        // Asetetaan hälytysajankohta, jos se oli aiemmin tehtävälle asetettu, muuten asetetaan no_current_alarm_time_text
        if (!alarmDateNonFormat.equals("null") && !alarmTimeNonFormat.equals("null")) {
            // Hyödynnetään DateTimePickHandler-luokan format-metodeja
            DateTimePickHandler dateTimePickHandler = new DateTimePickHandler(null, null, false);
            String[] dates = alarmDateNonFormat.split("-");
            alarmDate = dateTimePickHandler.formatDate(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));
            String[] times = alarmTimeNonFormat.split(":");
            alarmTime = dateTimePickHandler.formatTime(Integer.parseInt(times[0]), Integer.parseInt(times[1]));

            textViewAlarmInfo.setText(getResources().getString(R.string.current_alarm_time_text, alarmDate, alarmTime));
            buttonAlarm.setChecked(true);
            wasAlarm = true;
        } else {
            textViewAlarmInfo.setText(getResources().getString(R.string.no_current_alarm_time_text));
        }
    }

    // Metodi muistutukseen liittyvien muuttujien asettamiseksi
    public void updateReminderValues(boolean unchecked, String d, String t, String dnf, String tnf) {
        // Jos muistutus otettiin pois, asetetaan arvot "null" ja päivitetään tekstielementti
        if (unchecked) {
            reminderDateNonFormat = "null";
            reminderTimeNonFormat = "null";
            buttonReminder.setChecked(false);
            textViewReminderInfo.setText(getResources().getString(R.string.no_current_reminder_time_text));
        } else {
            // Muuten asetetaan valitut arvot muuttujiin ja päivitetään tekstielementti
            reminderDate = d;
            reminderTime = t;
            reminderDateNonFormat = dnf;
            reminderTimeNonFormat = tnf;
            textViewReminderInfo.setText(getResources().getString(R.string.current_reminder_time_text, reminderDate, reminderTime));
        }
    }

    // Metodi hälytykseen liittyvien muuttujien asettamiseksi
    public void updateAlarmValues(boolean unchecked, String d, String t, String dnf, String tnf) {
        // Jos hälytys otettiin pois, asetetaan arvot "null" ja päivitetään tekstielementti
        if (unchecked) {
            alarmDateNonFormat = "null";
            alarmTimeNonFormat = "null";
            buttonAlarm.setChecked(false);
            textViewAlarmInfo.setText(getResources().getString(R.string.no_current_alarm_time_text));
        } else {
            // Muuten asetetaan valitut arvot muuttujiin ja päivitetään tekstielementti
            alarmDate = d;
            alarmTime = t;
            alarmDateNonFormat = dnf;
            alarmTimeNonFormat = tnf;
            textViewAlarmInfo.setText(getResources().getString(R.string.current_alarm_time_text, alarmDate, alarmTime));
        }
    }
}