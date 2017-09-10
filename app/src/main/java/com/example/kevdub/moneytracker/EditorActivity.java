package com.example.kevdub.moneytracker;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.kevdub.moneytracker.data.MoneyContract.MoneyEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by kevinwang on 5/22/17.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String MAIN_VALUES = "com.kevdub.MoneyTracker.mainValues";

//    public static final String TOT_MONEY = "Total Assets";
//    public static final String SPEND_MONEY = "Spending Money";
//    public static final String SAVE_MONEY = "Saved";
//    public static final String PERCENT_SAVE = "Percent to Save";

    private static float totalMoney; // total amountEditText of money made this month
    private static float spendingMoney; // amountEditText available to spend
    private static float savingMoney; // amountEditText set aside for savings
    private static float percentToSave = 0.20f; // percentage of total amountEditText to set aside in savings
    private static int flow;

    public static final String TAG_TRAVEL = "Travel";
    public static final String TAG_BILLS = "Bills";
    public static final String TAG_DINING = "Dining";
    public static final String TAG_ENTERTAINMENT = "Entertainment";
    public static final String TAG_GROCERIES = "Groceries";
    public static final String TAG_PURCHASES = "Purchases";
    public static final String TAG_NONE = "None";

    private static final int POS_NONE = 0;
    private static final int POS_BILLS = 1;
    private static final int POS_DINING = 2;
    private static final int POS_ENTERTAINMENT = 3;
    private static final int POS_GROCERIES = 4;
    private static final int POS_PURCHASES = 5;
    private static final int POS_TRAVEL = 6;

    private static Map<String, Integer> tagMap;

    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener datePicker;

    private EditText nameEditText;
    private EditText amountEditText;
    private EditText dateEditText;
    private Spinner tagSpinner;
    private RadioButton outflowRadioButton;
    private RadioButton inflowRadioButton;

    private Uri currentUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_editor);

        setupSpinner();

        currentUri = getIntent().getData();
        if (currentUri == null) {
            bar.setTitle("New Entry");
        }
        else {
            bar.setTitle("Edit Entry");
            getSupportLoaderManager().initLoader(0, null, this);
        }



        /** Default radio button toggled is Outflow */
        inflowRadioButton = (RadioButton) findViewById(R.id.inflow_radio);
        outflowRadioButton = (RadioButton) findViewById(R.id.outflow_radio);
        outflowRadioButton.toggle();
        flow = MoneyEntry.OUTFLOW;

        nameEditText = (EditText) findViewById(R.id.name);
        amountEditText = (EditText) findViewById(R.id.amount);
        dateEditText = (EditText) findViewById(R.id.date);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (currentUri == null) {
            amountEditText.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

        myCalendar = Calendar.getInstance();
        updateLabel();

        datePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(); // sets dateEditText to the present date
            }
        };

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditorActivity.this, datePicker,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.log_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!amountEditText.getText().toString().equals("")) {
                    saveEntry();
                    finish();
                } else {
                    showEmptyAmountDialog();
                }
            }
        });
    }

    /**
     * Setup the tagSpinner
     */
    private void setupSpinner() {
        List<String> spinnerChoices = new ArrayList<>();
        spinnerChoices.add(TAG_NONE);
        spinnerChoices.add(TAG_BILLS);
        spinnerChoices.add(TAG_DINING);
        spinnerChoices.add(TAG_ENTERTAINMENT);
        spinnerChoices.add(TAG_GROCERIES);
        spinnerChoices.add(TAG_PURCHASES);
        spinnerChoices.add(TAG_TRAVEL);

        tagMap = new TreeMap<>();
        tagMap.put(TAG_NONE, POS_NONE);
        tagMap.put(TAG_BILLS, POS_BILLS);
        tagMap.put(TAG_DINING, POS_DINING);
        tagMap.put(TAG_ENTERTAINMENT, POS_ENTERTAINMENT);
        tagMap.put(TAG_GROCERIES, POS_GROCERIES);
        tagMap.put(TAG_PURCHASES, POS_PURCHASES);
        tagMap.put(TAG_TRAVEL, POS_TRAVEL);

        tagSpinner = (Spinner) findViewById(R.id.tag_spinner);
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerChoices);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(spinnerAdapter);
    }

    /**
     * Updates the text inside dateEditText to the user selected date
     */
    private void updateLabel() {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        dateEditText.setText(sdf.format(myCalendar.getTime()));
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.inflow_radio:
                if (checked)
                    flow = MoneyEntry.INFLOW;
                break;
            case R.id.outflow_radio:
                if (checked)
                    flow = MoneyEntry.OUTFLOW;
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if (!entryChanged()) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!entryChanged()) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveEntry() {
        String dateString = dateEditText.getText().toString();
        String name = nameEditText.getText().toString().trim();
        float floatAmount = Float.parseFloat(amountEditText.getText().toString());
        int intAmount = Math.round(floatAmount * 100); // in cents

        ContentValues values = new ContentValues();
        values.put(MoneyEntry.COLUMN_FLOW, flow);
        values.put(MoneyEntry.COLUMN_DATE, dateString);
        values.put(MoneyEntry.COLUMN_NAME, name);
        values.put(MoneyEntry.COLUMN_AMOUNT, intAmount);
        values.put(MoneyEntry.COLUMN_TAG, tagSpinner.getSelectedItem().toString());

        if (currentUri == null) {
            Uri uri = getContentResolver().insert(MoneyEntry.CONTENT_URI, values);
            if (uri == null)
                Toast.makeText(this, "Error saving entry", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Entry successfully saved", Toast.LENGTH_SHORT).show();
        } else {
            int rowsUpdated = getContentResolver().update(currentUri, values, null, null);
            if (rowsUpdated == 0)
                Toast.makeText(this, "Error updating entry", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Entry successfully updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEmptyAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please enter a value at the top");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                amountEditText.requestFocus();
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Checks if the current entry has been changed
     * Returns true if entry changed, false otherwise
     */
    private boolean entryChanged() {
        if (TextUtils.isEmpty(nameEditText.getText()) &&
            TextUtils.isEmpty(amountEditText.getText()) &&
            tagSpinner.getSelectedItem().toString().equals(TAG_NONE))
            return false;
        else
            return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
            MoneyEntry._ID,
            MoneyEntry.COLUMN_AMOUNT,
            MoneyEntry.COLUMN_NAME,
            MoneyEntry.COLUMN_TAG,
            MoneyEntry.COLUMN_FLOW,
            MoneyEntry.COLUMN_DATE
        };
        return new CursorLoader(this, currentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String name = data.getString(data.getColumnIndexOrThrow(MoneyEntry.COLUMN_NAME));
            double amount = data.getDouble(data.getColumnIndexOrThrow(MoneyEntry.COLUMN_AMOUNT));
            String tag = data.getString(data.getColumnIndexOrThrow(MoneyEntry.COLUMN_TAG));
            int flow = data.getInt(data.getColumnIndexOrThrow(MoneyEntry.COLUMN_FLOW));
            String date = data.getString(data.getColumnIndexOrThrow(MoneyEntry.COLUMN_DATE));

            amount = amount / 100;
            nameEditText.setText(name);
            amountEditText.setText(String.format(Locale.ENGLISH, "%.2f", amount));
            dateEditText.setText(date);

            if (tagMap.containsKey(tag)) {
                int tagPosition = tagMap.get(tag);
                tagSpinner.setSelection(tagPosition);
            }

            if (flow == MoneyEntry.OUTFLOW) {
                outflowRadioButton.toggle();
            } else if (flow == MoneyEntry.INFLOW) {
                inflowRadioButton.toggle();
            }
            EditorActivity.flow = flow;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.getText().clear();
        amountEditText.getText().clear();
        dateEditText.getText().clear();
        tagSpinner.setSelection(0);
        EditorActivity.flow = MoneyEntry.OUTFLOW;
        outflowRadioButton.toggle();
    }
}
