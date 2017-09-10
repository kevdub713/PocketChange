package com.example.kevdub.moneytracker;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.preference.PreferenceActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.kevdub.moneytracker.data.MoneyContract;
import com.example.kevdub.moneytracker.data.MoneyContract.MoneyEntry;
import com.example.kevdub.moneytracker.data.MoneyDBHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import layout.HeaderFragment;
import layout.MonthDialogFragment;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, MonthDialogFragment.MonthDialogListener {

    // class constants
    public static final String TOT_MONEY = "Total";
    public static final String SPEND_MONEY = "Balance";
    public static final String SAVE_MONEY = "Saved";
    public static final String PERCENT_SAVE = "Percent to Save";
    private static final int MONEY_LOADER = 0;

    public static MoneyCursorAdapter cursorAdapter;
    public static SharedPreferences sharedVals;
    public ViewPager viewPager;
    public static String month;
    public static String monthNum;
    public static String year;

    private FloatingActionButton fab;
    private ListView lv;
    private TextView monthTv;
    private Intent headerIntent;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar tb = (Toolbar) findViewById(R.id.toolBar);
        setActionBar(tb);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditor(v);
            }
        });

        sharedVals = getApplicationContext().getSharedPreferences(EditorActivity.MAIN_VALUES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedVals.edit();
        editor.putFloat(PERCENT_SAVE, 0.2f);
        editor.apply();

        // Set up ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        cursorAdapter = new MoneyCursorAdapter(this, null);
        lv = (ListView) findViewById(R.id.log);
        lv.setAdapter(cursorAdapter);
        registerForContextMenu(lv);

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            int lastFirstVisible;
            boolean fabVisible = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                final int currentFirstVisible = lv.getFirstVisiblePosition();
//                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
//                    if (fabVisible && currentFirstVisible > lastFirstVisible) { // scroll down
//                        fab.animate().cancel();
//                        fab.animate().translationYBy(350);
//                        fabVisible = false;
//                    } else if (!fabVisible && currentFirstVisible < lastFirstVisible) { // scroll up
//                        fab.animate().cancel();
//                        fab.animate().translationYBy(-350);
//                        fabVisible = true;
//                    }
//                }
//                lastFirstVisible = currentFirstVisible;
                int fabPositionY = fab.getScrollY();
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL || scrollState == SCROLL_STATE_FLING) {
                    fab.animate().cancel();
//                    fab.animate().translationYBy(250).setDuration(150);
                    fab.hide();
                } else {
                    fab.animate().cancel();
//                    fab.animate().translationY(fabPositionY).setDuration(150);
                    fab.show();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        // Setting up month Textview functionality
        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMMM", Locale.US);
        java.text.SimpleDateFormat monthNumFormat = new java.text.SimpleDateFormat("MM", Locale.US);
        java.text.SimpleDateFormat yearFormat = new java.text.SimpleDateFormat("yyyy", Locale.US);

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        month = monthFormat.format(date);
        monthNum = monthNumFormat.format(date);
        year = yearFormat.format(date);

        if (MonthDialogFragment.yearsLogged == null)
            MonthDialogFragment.yearsLogged = new ArrayList<>();
        if (!MonthDialogFragment.yearsLogged.contains(year))
            MonthDialogFragment.yearsLogged.add(year);

        monthTv = (TextView) findViewById(R.id.monthText);
        monthTv.setText(month + " " + year);
        monthTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthDialog();
            }
        });

        headerIntent = new Intent(HeaderFragment.RADIO_DATASET_CHANGED);
        getSupportLoaderManager().initLoader(MONEY_LOADER, null, this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Uri currentUri = ContentUris.withAppendedId(MoneyEntry.CONTENT_URI, info.id);
        switch (item.getItemId()) {
            case R.id.edit:
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.setData(currentUri);
                startActivity(intent);
                return true;
            case R.id.delete:
                showDeleteDialog(currentUri);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteDialog(final Uri currentUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int rowsDeleted = getContentResolver().delete(currentUri, null, null);
                if (rowsDeleted == 0)
                    Toast.makeText(MainActivity.this, "Entry deletion failed", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "Entry successfully deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null)
                    dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void openEditor(View v) {
        Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MoneyEntry.COLUMN_DATE + " LIKE ?";
        String[] selectionArgs = {monthNum + "/%/" + year};
        String[] projection = {
                MoneyContract.MoneyEntry._ID,
                MoneyContract.MoneyEntry.COLUMN_AMOUNT,
                MoneyContract.MoneyEntry.COLUMN_NAME,
                MoneyContract.MoneyEntry.COLUMN_TAG,
                MoneyContract.MoneyEntry.COLUMN_FLOW,
                MoneyContract.MoneyEntry.COLUMN_DATE
        };
        return new CursorLoader(this, MoneyEntry.CONTENT_URI, projection, selection, selectionArgs, MoneyContract.MoneyEntry.COLUMN_DATE + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    public void showMonthDialog() {
        DialogFragment dialog = new MonthDialogFragment();
        dialog.show(getSupportFragmentManager(), "MonthDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(String selectedMonth, String selectedMonthNum, String selectedYear) {
        // Set text
        month = selectedMonth;
        monthNum = selectedMonthNum;
        year = selectedYear;
        monthTv.setText(month + " " + year);

        // query the month's entries
        getSupportLoaderManager().restartLoader(MONEY_LOADER, null, this);

        // broadcast to HeaderFragment to update view with new values
        sendBroadcast(headerIntent);
    }

    /**
     * Adapter for the ViewPager Fragment
     */
    public static class MyPagerAdapter extends FragmentPagerAdapter {


        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return HeaderFragment.newInstance(MainActivity.SPEND_MONEY);

                case 1:
                    return HeaderFragment.newInstance(MainActivity.SAVE_MONEY);
                case 2:
                    return HeaderFragment.newInstance(MainActivity.TOT_MONEY);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Balance";
                case 1:
                    return "Saved";
                case 2:
                    return "Total";
                default:
                    return "no title";
            }
        }
    }
}
