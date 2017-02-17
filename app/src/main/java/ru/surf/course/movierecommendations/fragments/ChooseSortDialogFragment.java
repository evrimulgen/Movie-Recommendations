package ru.surf.course.movierecommendations.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.surf.course.movierecommendations.R;

/**
 * Created by Sergey on 13.02.2017.
 */

public class ChooseSortDialogFragment extends DialogFragment {

    private List<ChooseSortDialogFragment.SavePressedListener> listeners = new ArrayList<>();
    private int chosen;
    private int previous;
    private List<String> sortTypes = Arrays.asList("vote_average", "first_air_date", "popularity");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] charSequences = new CharSequence[]{"By popularity", "By first air date", "By average votes"};
        builder.setTitle("Choose sort type")
                .setSingleChoiceItems(charSequences, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        previous = chosen;
                        chosen = which;
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (previous != chosen) {
                            for (ChooseSortDialogFragment.SavePressedListener listener :
                                    listeners
                                    ) {
                                listener.saved();
                            }
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

    public void addListener(ChooseSortDialogFragment.SavePressedListener toAdd) {
        listeners.add(toAdd);
    }

    public String getChosenSort() {
        return sortTypes.get(chosen);
    }

    public interface SavePressedListener {
        void saved();
    }

}