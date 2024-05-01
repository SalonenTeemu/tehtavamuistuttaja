package com.example.projekti_tehtava_muistuttaja;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projekti_tehtava_muistuttaja.Handler.AppHandler;

import java.util.Objects;

// Luokka tehtäväelementin sivulle pyyhkäisyä varten, perii ItemTouchHelper.SimpleCallback
public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private MainActivity mainActivity;
    private AppHandler appHandler;

    // Asetetaan pyyhkäisysuunnat vasemmalle ja oikealle, sekä kontekstit
    public RecyclerItemTouchHelper(MainActivity mA, AppHandler handler) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.mainActivity = mA;
        this.appHandler = handler;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    // Kutsutaan, kun käyttäjä pyyhkäisee tehtävää joko vasemmalle tai oikealle
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        // Vasemmalle pyyhkäisy on tehtävän poistamista varten, oikealle sen muokkaamista varten
        if (direction == ItemTouchHelper.LEFT) {
            appHandler.confirmDeleteTask(position);
        } else {
            appHandler.editTask(position);
        }
    }

    // Asettaa pyyhkäistäessä näkyvät kuvakkeet ja värit
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable icon;
        ColorDrawable background;

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        if (dX > 0) {
            icon = ContextCompat.getDrawable(mainActivity, R.drawable.ic_baseline_edit);
            background = new ColorDrawable(ContextCompat.getColor(mainActivity, R.color.holo_orange_dark));
        } else {
            icon = ContextCompat.getDrawable(mainActivity, R.drawable.ic_baseline_delete_24);
            background = new ColorDrawable(Color.RED);
        }

        int iconMargin = (itemView.getHeight() - Objects.requireNonNull(icon).getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0 ) {
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int)dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) {
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int)dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            background.setBounds(0, 0, 0, 0);
        }
        background.draw(c);
        icon.draw(c);
    }
}
