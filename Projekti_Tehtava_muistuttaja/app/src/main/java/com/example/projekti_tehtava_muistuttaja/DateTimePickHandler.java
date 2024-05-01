package com.example.projekti_tehtava_muistuttaja;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

// Luokka, joka totetuttaa päivämäärän ja ajan valitsemisen muistutuksia ja hälytyksiä varten
// Perii luokan DialogFragment
public class DateTimePickHandler extends DialogFragment {

    // Muuttujat konteksteille, valituille päivämäärille ja ajoille muotuiltuna ja ilman
    // Lisäksi totuusarvo, tehdäänkö muistutus ja kalenteri-instanssi
    private NewTaskActivity nta;
    private EditTaskActivity eta;
    private Context context;
    private String date = "";
    private String time = "";
    private String dateNonFormat = "";
    private String timeNonFormat = "";
    private boolean reminder;
    private Calendar calendar;

    public DateTimePickHandler(NewTaskActivity newTA, EditTaskActivity eTA, boolean reminder) {
        // context asetetaan sen perusteella, mistä aktiviteetista tultiin. Voidaan myös hyödyntää
        // luokan formatTime ja formatDate-metodeja ilman näitä konteksteja, jolloin molemmat on null
        this.nta = newTA;
        this.reminder = reminder;
        this.eta = eTA;
        if (eta == null) {
            context = newTA;
        } else {
            context = eTA;
        }
        if (context != null) {
            selectDate();
        }
    }

    // Metodi päivämäärän valitsemiseksi. Valitsemisikkunalle asetettu oma teema
    private void selectDate() {
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.CustomDatePickerDialog, new DatePickerDialog.OnDateSetListener() {
            // Vaihdetaan muuttujien arvot valittujen perusteella muotoiltuna ja ilman muotoilua
            // Jatketaan myös selectTime-metodiin
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(year, month, day);
                date = formatDate(year, month, day);
                dateNonFormat = year + "-" + month + "-" + day;
                selectTime();
            }
        }, year, month, day);
        datePickerDialog.setTitle("Valitse muistutuksen päivämäärä");
        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            // Ikkunasta poistettuessa asetetaan muuttujien arvot konteksteissa "null" updateReminderValues ja updateAlarmValues avulla
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (date.equals("")) {
                    if (reminder) {
                        if (context == nta) {
                            nta.updateReminderValues(true, date, time, dateNonFormat, timeNonFormat);
                        } else {
                            eta.updateReminderValues(true, date, time, dateNonFormat, timeNonFormat);
                        }
                    } else {
                        if (context == nta) {
                            nta.updateAlarmValues(true, date, time, dateNonFormat, timeNonFormat);
                        } else {
                            eta.updateAlarmValues(true, date, time, dateNonFormat, timeNonFormat);
                        }
                    }
                }
            }
        });
        // Asetetaan, että päivän voi valita vain nykyhetkestä eteenpäin
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    // Metodi päivämäärän valitsemiseksi. Valitsemisikkunalle asetettu oma teema
    private void selectTime() {
        Calendar currentDate = Calendar.getInstance();
        int hour = currentDate.get(Calendar.HOUR_OF_DAY);
        int minute = currentDate.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, R.style.CustomTimePickerDialog, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                boolean validTime = true;

                // Jos päiväksi valittiin nykypäivä, asetetaan mahdolliseksi valita aika vain tästä hetkestä eteenpäin
                if (calendar.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                        calendar.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                        calendar.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)) {
                    if (!selectedTime.after(currentDate)) {
                        validTime = false;
                    }
                }
                // Jos aika oli ok, päivitetään kontekstien muuttujien arvot updateReminderValues ja updateAlarmValues avulla
                if (validTime) {
                    time = formatTime(hour, minute);
                    timeNonFormat = hour + ":" + minute;
                    if (reminder) {
                        if (context == nta) {
                            nta.updateReminderValues(false, date, time, dateNonFormat, timeNonFormat);
                        } else {
                            eta.updateReminderValues(false, date, time, dateNonFormat, timeNonFormat);
                        }
                    } else {
                        if (context == nta) {
                            nta.updateAlarmValues(false, date, time, dateNonFormat, timeNonFormat);
                        } else {
                            eta.updateAlarmValues(false, date, time, dateNonFormat, timeNonFormat);
                        }
                    }
                } else {
                    Toast.makeText(context, "Valitse tuleva aika", Toast.LENGTH_SHORT).show();
                }
            }

        }, hour, minute, true);
        timePickerDialog.setTitle("Valitse muistutuksen kellonaika");
        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            // Ikkunasta poistettuessa asetetaan muuttujien arvot konteksteissa "null" updateReminderValues ja updateAlarmValues avulla
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (reminder) {
                    if (time.equals("") || date.equals("")) {
                        if (context == nta) {
                            nta.updateReminderValues(true, date, time, dateNonFormat, timeNonFormat);
                        } else {
                            eta.updateReminderValues(true, date, time, dateNonFormat, timeNonFormat);
                        }
                    }
                } else {
                    if (time.equals("") || date.equals("")) {
                        if (context == nta) {
                            nta.updateAlarmValues(true, date, time, dateNonFormat, timeNonFormat);

                        } else {
                            eta.updateAlarmValues(true, date, time, dateNonFormat, timeNonFormat);
                        }
                    }
                }
            }
        });
        timePickerDialog.show();
    }

    // Metodi, joka muotoilee päivän, kuukauden ja vuoden merkkijonoksi käyttäen välimerkkinä pistettä
    public String formatDate(int year, int month, int day) {
        String formattedDay = "";
        String formattedMonth = "";
        month++;
        if (day / 10 == 0) {
            formattedDay = "0" + day;
        } else {
            formattedDay = "" + day;
        }
        if (month / 10 == 0) {
            formattedMonth = "0" + month;
        } else {
            formattedMonth = "" + month;
        }
        return formattedDay + "." + formattedMonth + "." + year;
    }

    // Metodi, joka muotoilee tunnin ja minuutin merkkijonoksi käyttäen välimerkkinä kaksoispistettä
    public String formatTime(int hour, int minute) {
        String formattedMinute;
        if (minute / 10 == 0) {
            formattedMinute = "0" + minute;
        } else {
            formattedMinute = "" + minute;
        }
        return hour + ":" + formattedMinute;
    }
}