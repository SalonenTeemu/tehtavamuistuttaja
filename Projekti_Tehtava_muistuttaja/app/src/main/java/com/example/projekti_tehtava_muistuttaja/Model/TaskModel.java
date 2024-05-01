package com.example.projekti_tehtava_muistuttaja.Model;

// Luokka tehtävän mallintamiseen
public class TaskModel {
    // Tehtävän id, jonka avulla tehtävä voidaan hakea Firebasesta
    private String taskID;
    // Tehtävän otsikko
    private String taskTitle;
    // Tehtävän lisätiedot
    private String taskInfo;
    // Tehtävän mahdollinen määräpäivä String-muodossa
    private String taskDueDate;
    // Tehtävälle asetetun muistutuksen id, vakiona -1 eli ei muistutusta asetettu
    private int reminderID = -1;
    // Tehtävälle asetetun muistutuksen päivämäärä String-muodossa
    private String reminderDate;
    // Tehtävälle asetetun muistutuksen kellonaika String-muodossa
    private String reminderTime;
    // Tehtävälle asetetun hälytyksen id, vakiona -1 eli ei hälytystä asetettu
    private int alarmID = -1;
    // Tehtävälle asetetun hälytyksen päivämäärä String-muodossa
    private String alarmDate;
    // Tehtävälle asetetun hälytyksen kellonaika String-muodossa
    private String alarmTime;

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(String taskInfo) {
        this.taskInfo = taskInfo;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getTaskDueDate() {
        return taskDueDate;
    }

    public void setTaskDueDate(String taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public int getReminderID() {
        return reminderID;
    }

    public void setReminderID(int reminderID) {
        this.reminderID = reminderID;
    }

    public String getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public int getAlarmID() {
        return alarmID;
    }

    public void setAlarmID(int alarmID) {
        this.alarmID = alarmID;
    }

    public String getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(String alarmDate) {
        this.alarmDate = alarmDate;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }
}
