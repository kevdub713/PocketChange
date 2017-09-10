package com.example.kevdub.moneytracker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kevinwang on 7/31/17.
 */

public class MoneyContract {

    private MoneyContract() {}

    // URI Constants
    public static final String CONTENT_AUTHORITY = "com.example.kevdub.moneytracker";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_LOG = "moneylog";

    public static final class MoneyEntry implements BaseColumns {
        // Table Name
        public static final String TABLE_NAME = "moneylog";

        // Column Names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_FLOW = "flow";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_TAG = "tag";

        // Flow constats
        public static final int INFLOW = 0;
        public static final int OUTFLOW = 1;

        public static boolean isValidFlow(int f) {
            return f == INFLOW || f == OUTFLOW;
        }

        // Complete Content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LOG);

        // MIME types
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOG;
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOG;
    }
}
