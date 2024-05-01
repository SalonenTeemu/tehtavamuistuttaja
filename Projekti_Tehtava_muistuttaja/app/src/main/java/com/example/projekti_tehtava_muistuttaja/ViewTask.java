package com.example.projekti_tehtava_muistuttaja;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

// Luokka tehtävän tietojen näyttämiseksi perityn BottomSheetDialogFragment-luokan avulla
public class ViewTask extends BottomSheetDialogFragment {

    // Näkymän elementit
    private TextView taskTitle;
    private TextView taskInfo;
    private TextView taskDueDate;
    private TextView taskReminder;
    private TextView taskAlarm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Luo näkymän asettamalla fragmentin käyttöliittymän layoutin view_task pohjalta
        View view = inflater.inflate(R.layout.view_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        view.setBackgroundColor(getResources().getColor(R.color.holo_orange_dark));
        return view;
    }

    // Metodi, joka suoritetaan, kun näkymä on luotu
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Haetaan elementit layoutista
        taskTitle = getView().findViewById(R.id.textViewTaskTitle);
        taskInfo = getView().findViewById(R.id.textViewTaskInfo);
        taskDueDate = getView().findViewById(R.id.textViewTaskDueDate);
        taskReminder = getView().findViewById(R.id.textViewTaskReminder);
        taskAlarm = getView().findViewById(R.id.textViewTaskAlarm);
        // Haetaan tehtävän tiedot bundlen kautta
        final Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            String info = bundle.getString("info");
            String dueDate = bundle.getString("duedate");
            String reminderDate = bundle.getString("reminderdate");
            String reminderTime = bundle.getString("remindertime");
            String alarmDate = bundle.getString("alarmdate");
            String alarmTime = bundle.getString("alarmtime");

            // Asetetaan otsikko ja lisätiedot
            taskTitle.setText(title);
            taskInfo.setText(info);

            // Asetetaan määräpäivä, jos se oli tehtävälle asetettu, muuten asetetaan task_view_no_due_date_text
            if (!dueDate.equals("null")) {
                // Hyödynnetään DateTimePickHandler-luokan format-metodeja
                DateTimePickHandler dtph = new DateTimePickHandler(null, null, false);
                String[] dates = dueDate.split("-");
                String date = dtph.formatDate(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));

                taskDueDate.setText(getResources().getString(R.string.task_view_due_date_text, date));
            } else {
                taskDueDate.setText(getResources().getString(R.string.task_view_no_due_date_text));
            }

            // Asetetaan muistutusajankohta, jos se oli tehtävälle asetettu, muuten asetetaan task_view_no_reminder_text
            if (!reminderDate.equals("null") && !reminderTime.equals("null")) {
                // Hyödynnetään DateTimePickHandler-luokan format-metodeja
                DateTimePickHandler dtph = new DateTimePickHandler(null, null, false);
                String[] dates = reminderDate.split("-");
                String date = dtph.formatDate(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));

                String[] times = reminderTime.split(":");
                String time = dtph.formatTime(Integer.parseInt(times[0]), Integer.parseInt(times[1]));
                taskReminder.setText(getResources().getString(R.string.task_view_reminder_text, date, time));
            } else {
                taskReminder.setText(getResources().getString(R.string.task_view_no_reminder_text));
            }

            // Asetetaan hälytysajankohta, jos se oli tehtävälle asetettu, muuten asetetaan task_view_no_alarm_text
            if (!alarmDate.equals("null") && !alarmTime.equals("null")) {
                // Hyödynnetään DateTimePickHandler-luokan format-metodeja
                DateTimePickHandler dtph = new DateTimePickHandler(null, null, false);
                String[] dates = alarmDate.split("-");
                String date = dtph.formatDate(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));

                String[] times = alarmTime.split(":");
                String time = dtph.formatTime(Integer.parseInt(times[0]), Integer.parseInt(times[1]));
                taskAlarm.setText(getResources().getString(R.string.task_view_alarm_text, date, time));
            } else {
                taskAlarm.setText(getResources().getString(R.string.task_view_no_alarm_text));
            }
        }
    }
}
