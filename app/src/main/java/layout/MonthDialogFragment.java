package layout;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.kevdub.moneytracker.MainActivity;
import com.example.kevdub.moneytracker.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by kevinwang on 9/7/17.
 */

public class MonthDialogFragment extends DialogFragment {

    public static ArrayList<String> yearsLogged;
    private Spinner monthSpinner;
    private Spinner yearSpinner;

    /** The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. **/
    public interface MonthDialogListener {
        void onDialogPositiveClick(String month, String monthNum, String year);
    }

    // Use this instance of the listener to deliver action events
    MonthDialogListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (MonthDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        //Layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.choose_month_dialog, null);
        builder.setView(view);
        setupSpinner(view);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(
                                monthSpinner.getSelectedItem().toString(),
                                String.format(Locale.US, "%02d", monthSpinner.getSelectedItemPosition() + 1),
                                yearSpinner.getSelectedItem().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                    }
                });
        return builder.create();
    }

    /**
     * Sets up the two spinners in the dialog
     */
    private void setupSpinner(View view) {
        monthSpinner = (Spinner) view.findViewById(R.id.monthSpinner);
        yearSpinner = (Spinner) view.findViewById(R.id.yearSpinner);
        ArrayList<String> months = new ArrayList<>(Arrays.asList(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        ));

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        if (yearsLogged == null) {
            yearsLogged = new ArrayList<>();
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, yearsLogged);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Month spinner default selection
        int monthSelectPosition = 0;
        for (int i = 0; i < months.size(); i++) {
            if (months.get(i).equals(MainActivity.month))
                monthSelectPosition = i;
        }
        monthSpinner.setSelection(monthSelectPosition);

        // Year spinner default selection
        int yearSelectPosition = 0;
        for (int i = 0; i < yearsLogged.size(); i++) {
            if (yearsLogged.get(i).equals(MainActivity.year))
                yearSelectPosition = i;
        }
        yearSpinner.setSelection(yearSelectPosition);
    }
}
