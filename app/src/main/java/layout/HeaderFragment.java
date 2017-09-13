package layout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kevdub.moneytracker.EditorActivity;
import com.example.kevdub.moneytracker.MainActivity;
import com.example.kevdub.moneytracker.R;
import com.example.kevdub.moneytracker.data.MoneyContract;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HeaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HeaderFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NAME = "name";
    private static final String ARG_VALUE = "value";

    private String name;
    private double valDouble;
    private boolean amountColorIsRed = false; // default text color is green

    private View view;
    private TextView nameTv;
    private TextView valTv;

    public static final String RADIO_DATASET_CHANGED = "com.yourapp.app.RADIO_DATASET_CHANGED";

    private Radio radio;

    public HeaderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param name Parameter 1.
//     * @param val
     * @return A new instance of fragment HeaderFragment.
     */
    public static HeaderFragment newInstance(String name) {
        HeaderFragment fragment = new HeaderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);

            int value = getNewValue(getContext());
            valDouble = value / 100.0;
        }
        radio = new Radio();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_header, container, false);
        nameTv = (TextView) view.findViewById(R.id.name);
        valTv = (TextView) view.findViewById(R.id.val);
        nameTv.setText(this.name);
        valTv.setText(String.format(Locale.ENGLISH, "%.2f", valDouble));

        if (valDouble < 0 ) {
            valTv.setTextColor(ContextCompat.getColor(getContext(), R.color.outflowColor));
        } else if (valDouble > 0 ) {
            valTv.setTextColor(ContextCompat.getColor(getContext(), R.color.inflowColor));
        } else {
            valTv.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RADIO_DATASET_CHANGED);
        getActivity().getApplicationContext().registerReceiver(radio, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            getActivity().getApplicationContext().unregisterReceiver(radio);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot unregister receiver");
        }
    }

    private int getNewValue(Context context) {
        int sumInflows = getSumInflows(context);
        int sumOutflows = getSumOutflows(context);
        int amountBudgeted = getAmountBudgeted(context);
        int amountSaved = Math.round((sumInflows - amountBudgeted) * MainActivity.sharedVals.getFloat(MainActivity.PERCENT_SAVE, 0));
        switch (name) {
            case MainActivity.SPEND_MONEY:
                return sumInflows - Math.round(sumInflows * MainActivity.sharedVals.getFloat(MainActivity.PERCENT_SAVE, 0)) - sumOutflows;
            case MainActivity.SAVE_MONEY:
                return amountSaved;
            case MainActivity.TOT_MONEY:
                return sumInflows - sumOutflows;
            default:
                return -1;
        }
    }

    private int getSumInflows(Context context) {
        String[] projection = {"SUM(" + MoneyContract.MoneyEntry.COLUMN_AMOUNT + ")"};

        // Select inflows for the appropriate month
        String selection = MoneyContract.MoneyEntry.COLUMN_FLOW + "=? AND " +
                MoneyContract.MoneyEntry.COLUMN_DATE + " LIKE ?";

        String[] selectionArgs = {String.valueOf(MoneyContract.MoneyEntry.INFLOW),
                MainActivity.monthNum + "/%/" + MainActivity.year};

        Cursor cursor = context.getContentResolver().query(
                MoneyContract.MoneyEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        int sumInflows = 0;
        if (cursor.moveToFirst())
            sumInflows = cursor.getInt(cursor.getColumnIndexOrThrow(projection[0]));

        return sumInflows;
    }

    private int getSumOutflows(Context context) {
        String[] projection = {"SUM(" + MoneyContract.MoneyEntry.COLUMN_AMOUNT + ")"};
        // Select outflows for the appropriate month
        String selection = MoneyContract.MoneyEntry.COLUMN_FLOW + "=? AND " +
                MoneyContract.MoneyEntry.COLUMN_DATE + " LIKE ?";

        String[] selectionArgs = {String.valueOf(MoneyContract.MoneyEntry.OUTFLOW),
                MainActivity.monthNum + "/%/" + MainActivity.year};

        Cursor cursor = context.getContentResolver().query(
                MoneyContract.MoneyEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        int sumOutflows = 0;
        if (cursor.moveToFirst())
            sumOutflows = cursor.getInt(cursor.getColumnIndexOrThrow(projection[0]));
        return sumOutflows;
    }

    private int getAmountBudgeted(Context context) {
        String[] projection = {"SUM(" + MoneyContract.MoneyEntry.COLUMN_AMOUNT + ")"};
        String selection = MoneyContract.MoneyEntry.COLUMN_TAG + "=?";
        String[] selectionArgs = {EditorActivity.TAG_BUDGET};

        Cursor cursor = context.getContentResolver().query(
                MoneyContract.MoneyEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        int result = -1;
        if (cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndexOrThrow(projection[0]));
        return result;
    }

    public class Radio extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RADIO_DATASET_CHANGED)) {
                int value = getNewValue(context);
                valDouble = value / 100.0;
                valTv.setText(String.format(Locale.ENGLISH, "%.2f", valDouble));

                if (valDouble < 0 ) {
                    valTv.setTextColor(ContextCompat.getColor(context, R.color.outflowColor));
                } else if (valDouble > 0 ) {
                    valTv.setTextColor(ContextCompat.getColor(context, R.color.inflowColor));
                } else {
                    valTv.setTextColor(ContextCompat.getColor(context, R.color.white));
                }
            }
        }
    }
}
