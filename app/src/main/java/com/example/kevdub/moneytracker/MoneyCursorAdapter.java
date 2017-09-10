package com.example.kevdub.moneytracker;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.kevdub.moneytracker.data.MoneyContract;

import java.util.Locale;


/**
 * Created by kevinwang on 5/19/17.
 */



public class MoneyCursorAdapter extends CursorAdapter {

    public MoneyCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvDate = (TextView) view.findViewById(R.id.date);
        TextView tvAmount = (TextView) view.findViewById(R.id.value);
        TextView tvTag = (TextView) view.findViewById(R.id.tag);
        TextView tvDot = (TextView) view.findViewById(R.id.dot);
//        ImageView ivTagIcon = (ImageView) view.findViewById(R.id.tag_icon);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_NAME));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_DATE));
        Double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_AMOUNT)) * .01;
        String tag = cursor.getString(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_TAG));

        tvName.setText(name);
        tvDate.setText(date);

        /** Change text color of amount */
        int flow = cursor.getInt(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_FLOW));
        switch (flow) {
            case MoneyContract.MoneyEntry.INFLOW:
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.inflowColor));
                tvAmount.setText("+");
                break;
            case MoneyContract.MoneyEntry.OUTFLOW:
                tvAmount.setTextColor(ContextCompat.getColor(context, R.color.outflowColor));
                tvAmount.setText("-");
                break;
        }
        tvAmount.append(String.format(Locale.ENGLISH, "%.2f", amount));

        /** Choosing appropriate tag */
        if (tag != null) {
            tvDot.setText("·");
            switch (tag) {
                case EditorActivity.TAG_NONE:
                    tvTag.setText("");
                    tvDot.setText("");
//                    ivTagIcon.setImageResource(R.drawable.ic_remove_white_24dp);
                    break;
                case EditorActivity.TAG_BILLS:
                    tvTag.setText(EditorActivity.TAG_BILLS);
                    tvTag.setTextColor(ContextCompat.getColor(context, R.color.billColor));
//                    ivTagIcon.setImageResource(R.drawable.ic_payment_blue_24pd);
                    break;
                case EditorActivity.TAG_DINING:
                    tvTag.setText(EditorActivity.TAG_DINING);
                    tvTag.setTextColor(ContextCompat.getColor(context, R.color.foodColor));
//                    ivTagIcon.setImageResource(R.drawable.ic_restaurant_orange_24pd);
                    break;
                case EditorActivity.TAG_ENTERTAINMENT:
                    tvTag.setText(EditorActivity.TAG_ENTERTAINMENT);
                    tvTag.setTextColor(ContextCompat.getColor(context, R.color.entertainmentColor));
//                    ivTagIcon.setImageResource(R.drawable.ic_theaters_red_24pd);
                    break;
                case EditorActivity.TAG_GROCERIES:
                    tvTag.setText(EditorActivity.TAG_GROCERIES);
                    tvTag.setTextColor(ContextCompat.getColor(context, R.color.groceriesColor));
                    break;
                case EditorActivity.TAG_PURCHASES:
                    tvTag.setText(EditorActivity.TAG_PURCHASES);
                    tvTag.setTextColor(ContextCompat.getColor(context, R.color.purchaseColor));
//                    ivTagIcon.setImageResource(R.drawable.ic_shopping_basket_green_24pd);
                    break;
                case EditorActivity.TAG_TRAVEL:
                    tvTag.setText(EditorActivity.TAG_TRAVEL);
                    tvTag.setTextColor(ContextCompat.getColor(context, R.color.gasColor));
//                    ivTagIcon.setImageResource(R.drawable.ic_gas_station_yellow_24pd);
                    break;
            }
        }
        else {
            tvTag.setText("");
            tvDot.setText("");
//            ivTagIcon.setImageResource(R.drawable.ic_remove_white_24dp);
        }
    }
}
