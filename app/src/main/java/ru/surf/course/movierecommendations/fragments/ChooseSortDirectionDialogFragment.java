package ru.surf.course.movierecommendations.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.surf.course.movierecommendations.R;

/**
 * Created by Sergey on 16.02.2017.
 */

public class ChooseSortDirectionDialogFragment extends DialogFragment {

    private static final String SORT_PREFS = "sort_direction_prefs";
    private static final String CHOSEN_SORT = "chosen_sort_direction";
    private CharSequence[] sortDirectionNames = new CharSequence[]{"In descending order", "In ascending order"};
    private List<ChooseSortDirectionDialogFragment.SavePressedListener> listeners = new ArrayList<>();
    private int chosen;
    private int previous;
    private List<String> directionsTypes = Arrays.asList("desc", "asc");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        chosen = getChosen(getActivity());
        builder.setTitle("Choose sort type")
                .setSingleChoiceItems(sortDirectionNames, chosen, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        previous = chosen;
                        chosen = which;
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (previous != chosen) {
                            for (ChooseSortDirectionDialogFragment.SavePressedListener listener :
                                    listeners
                                    ) {
                                listener.saved();
                            }
                            saveChosen(chosen, getActivity());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    private int getChosen(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SORT_PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(CHOSEN_SORT, 0);
    }

    private boolean saveChosen(int chosen, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SORT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(CHOSEN_SORT, chosen);
        return editor.commit();
    }

    public void addListener(ChooseSortDirectionDialogFragment.SavePressedListener toAdd) {
        listeners.add(toAdd);
    }

    public String getChosenSortDirection() {
        return directionsTypes.get(chosen);
    }

    public interface SavePressedListener {
        void saved();
    }
}
