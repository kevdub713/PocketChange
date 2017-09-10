package com.example.kevdub.moneytracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.kevdub.moneytracker.MainActivity;
import com.example.kevdub.moneytracker.data.MoneyContract.MoneyEntry;

import layout.HeaderFragment;

/**
 * Created by kevinwang on 7/31/17.
 */

public class MoneyProvider extends ContentProvider {

    /** URI matcher code for the content URI for the Money Activity table */
    private static final int LOG_ENTRY = 100;
    /** URI matcher code for the content URI for a single entry in Money Activity table */
    private static final int LOG_ENTRY_ID = 101;
    /** URI matcher code for the content URI requiring a Group By clause in query */
    private static final int LOG_GROUPBY = 102;

    /** UriMatcher object */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(MoneyContract.CONTENT_AUTHORITY, MoneyContract.PATH_LOG, LOG_ENTRY);
        sUriMatcher.addURI(MoneyContract.CONTENT_AUTHORITY, MoneyContract.PATH_LOG + "/#", LOG_ENTRY_ID);
        sUriMatcher.addURI(MoneyContract.CONTENT_AUTHORITY, MoneyContract.PATH_LOG + "/GROUPBY", LOG_GROUPBY);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = MoneyProvider.class.getSimpleName();

    /** Database helper */
    private MoneyDBHelper mDbHelper;
    private Intent intent;

    @Override
    public boolean onCreate() {
        mDbHelper = new MoneyDBHelper(getContext());
        intent = new Intent(HeaderFragment.RADIO_DATASET_CHANGED);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] columns, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case LOG_ENTRY:
                cursor = db.query(MoneyEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, sortOrder);
                break;
            case LOG_ENTRY_ID:
                selection = MoneyEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(MoneyEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, sortOrder);
                break;
            case LOG_GROUPBY:
                String groupBy = MoneyEntry.COLUMN_FLOW;
                cursor = db.query(MoneyEntry.TABLE_NAME, columns, selection, selectionArgs, groupBy, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case LOG_ENTRY:
                return insertEntry(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    /**
     * Helper method for inserting a new row into the moneytracker database
     * @param uri
     * @param contentValues
     * @return
     */
    private Uri insertEntry(Uri uri, ContentValues contentValues) {
        // Sanity checks
        Integer flow = contentValues.getAsInteger(MoneyEntry.COLUMN_FLOW);
        if (flow == null || !MoneyEntry.isValidFlow(flow))
            throw new IllegalArgumentException("Log Entry must have either inflow or outflow");
        String date = contentValues.getAsString(MoneyEntry.COLUMN_DATE);
        if (date == null)
            throw new IllegalArgumentException("Log Entry must have a date");
        String name = contentValues.getAsString(MoneyEntry.COLUMN_NAME);
        if (name == null)
            throw new IllegalArgumentException("Log Entry must have a name");
        Double amount = contentValues.getAsDouble(MoneyEntry.COLUMN_AMOUNT);
        if (amount == null)
            throw new IllegalArgumentException("Log Entry must have an amount");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(MoneyEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        getContext().sendBroadcast(intent);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int result;
        switch (match) {
            case LOG_ENTRY:
                result = db.delete(MoneyEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOG_ENTRY_ID:
                selection = MoneyEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                result = db.delete(MoneyEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion not supported for " + uri);
        }
        if (result != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        getContext().sendBroadcast(intent);

        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case LOG_ENTRY:
                return updateEntry(uri, contentValues, selection, selectionArgs);
            case LOG_ENTRY_ID:
                selection = MoneyEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateEntry(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Helper method for updatin values in a row in the database
     * @param uri
     * @param contentValues
     * @param selection
     * @param selectionArgs
     * @return
     */
    private int updateEntry(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // Sanity checks
        if (contentValues.containsKey(MoneyEntry.COLUMN_FLOW)) {
            Integer flow = contentValues.getAsInteger(MoneyEntry.COLUMN_FLOW);
            if (flow == null || !MoneyEntry.isValidFlow(flow))
                throw new IllegalArgumentException("Log entry must have either inflow or outflow");
        }
        if (contentValues.containsKey(MoneyEntry.COLUMN_DATE)) {
            String date = contentValues.getAsString(MoneyEntry.COLUMN_DATE);
            if (date == null)
                throw new IllegalArgumentException("Log entry must have a valid date");
        }
        if (contentValues.containsKey(MoneyEntry.COLUMN_NAME)) {
            String name = contentValues.getAsString(MoneyEntry.COLUMN_NAME);
            if (name == null)
                throw new IllegalArgumentException("Log entry must have a name");
        }
        if (contentValues.containsKey(MoneyEntry.COLUMN_AMOUNT)) {
            Double amount = contentValues.getAsDouble(MoneyEntry.COLUMN_AMOUNT);
            if (amount == null)
                throw new IllegalArgumentException("Log entry must have an amount");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int result = db.update(MoneyEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (result != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        getContext().sendBroadcast(intent);

        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LOG_ENTRY:
                return MoneyEntry.CONTENT_ITEM_TYPE;
            case LOG_ENTRY_ID:
                return MoneyEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
