package com.example.projekti_tehtava_muistuttaja.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projekti_tehtava_muistuttaja.DateTimePickHandler;
import com.example.projekti_tehtava_muistuttaja.MainActivity;
import com.example.projekti_tehtava_muistuttaja.Model.TaskModel;
import com.example.projekti_tehtava_muistuttaja.R;

import java.util.ArrayList;
import java.util.List;

// Luokka on tehtävien adapteri RecyclerView:iä varten, joka perii RecyclerView.Adapter-luokan
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<TaskModel> taskList;
    private MainActivity mainActivity;
    private ItemClickListener mClickListener;

    // Konstruktori, jossa annetaan konteksti MainActivity:lle ja luodaan uusi tyhjä tehtävälista
    public TaskAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        taskList = new ArrayList<>();
    }

    // Luo uuden ViewHolder-olion tehtäväelementin layoutin recycler_view_task_element pohjalta
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_task_element, parent, false);
        return new ViewHolder(itemView);
    }

    // Päivittää tehtävän tiedot ViewHolderiin tietyn position perusteella
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskModel task = taskList.get(position);

        // Haetaan tehtävän tiedot TaskModelista ja asetetaan ne näkyviin
        String taskTitle = task.getTaskTitle();
        String taskInfo = task.getTaskInfo();
        String taskDueDate = task.getTaskDueDate();
        holder.taskTitle.setText(taskTitle);
        holder.taskInfo.setText(taskInfo);
        if (taskDueDate != null && !taskDueDate.equals("null")) {
            // Jos tehtävälle on asetettu määräpäivä, asetetaan se näkyväksi, muuten piilotetaan
            holder.taskDueDate.setVisibility(View.VISIBLE);
            DateTimePickHandler dtph = new DateTimePickHandler(null, null, false);
            String[] dates = taskDueDate.split("-");
            String date = dtph.formatDate(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));
            holder.taskDueDate.setText(mainActivity.getResources().getString(R.string.due_date_text, date));
        } else {
            holder.taskDueDate.setVisibility(View.GONE);
        }
    }

    // Palauttaa tehtävälistan koon
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Palauttaa tehtävän tietyn position perusteella
    public TaskModel getTaskFromPosition(int position) {
        return taskList.get(position);
    }

    // Asettaa tehtävälistan ja päivittää adpaterin
    private void setTasks(List<TaskModel> taskList) {
        Log.d(MainActivity.TAG, "TaskAdapter SetTasks: asetetaan tehtävät");
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    // Apumetodi, jota kutsutaan FirebaseHandler-luokasta tehtävälistan päivittämiseksi
    public void updateTasks(List<TaskModel> taskList) {
        setTasks(taskList);
    }

    // Asettaa klikinkuuntelijan tehtäväelementille
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // Rajapinta tehtäväelementin klikkaamiseen, totetutetaan AppHandler-luokassa
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // Luokka yleiselle tehtävän rakenteelle RecyclerView:ssä
    // Laajentaa RecyclerView.ViewHolder luokan ja toteuttaa myös tehtävälle klikinkuuntelijan
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView taskTitle;
        TextView taskInfo;
        TextView taskDueDate;

        // Haetaan elementit rakentajassa ja asetetaan klikinkuntelija
        ViewHolder(View view) {
            super(view);
            taskTitle = view.findViewById(R.id.textViewTaskTitle);
            taskInfo = view.findViewById(R.id.textViewTaskInfo);
            taskDueDate = view.findViewById(R.id.textViewTaskDueDate);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }
}
