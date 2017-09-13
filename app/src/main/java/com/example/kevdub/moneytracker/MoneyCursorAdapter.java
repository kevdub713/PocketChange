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
import java.util.zip.Inflater;


/**
 * Created by kevinwang on 5/19/17.
 */



public class MoneyCursorAdapter extends CursorAdapter {

    public MoneyCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View listItemView = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        holder.name = (TextView) listItemView.findViewById(R.id.name);
        holder.date = (TextView) listItemView.findViewById(R.id.date);
        holder.amount = (TextView) listItemView.findViewById(R.id.amount);
        holder.tag = (TextView) listItemView.findViewById(R.id.tag);
        holder.dot = (TextView) listItemView.findViewById(R.id.dot);
        listItemView.setTag(holder);
        return listItemView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String name = cursor.getString(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_NAME));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_DATE));
        Double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_AMOUNT)) * .01;
        String tag = cursor.getString(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_TAG));

        holder.name.setText(name);
        holder.date.setText(date);

        /** Change text color of amount */
        int flow = cursor.getInt(cursor.getColumnIndexOrThrow(MoneyContract.MoneyEntry.COLUMN_FLOW));
        switch (flow) {
            case MoneyContract.MoneyEntry.INFLOW:
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.inflowColor));
                holder.amount.setText("+");
                break;
            case MoneyContract.MoneyEntry.OUTFLOW:
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.outflowColor));
                holder.amount.setText("-");
                break;
        }
        holder.amount.append(String.format(Locale.ENGLISH, "%.2f", amount));

        /** Choosing appropriate tag */
        if (tag != null) {
            holder.dot.setText("·");
            switch (tag) {
                case EditorActivity.TAG_NONE:
                    holder.tag.setText("");
                    holder.dot.setText("");
                    break;
                case EditorActivity.TAG_BILLS:
                    holder.tag.setText(EditorActivity.TAG_BILLS);
                    holder.tag.setTextColor(ContextCompat.getColor(context, R.color.billColor));
                    break;
                case EditorActivity.TAG_BUDGET:
                    holder.tag.setText(EditorActivity.TAG_BUDGET);
                    holder.tag.setTextColor(ContextCompat.getColor(context, R.color.budgetColor));
                    break;
                case EditorActivity.TAG_DINING:
                    holder.tag.setText(EditorActivity.TAG_DINING);
                    holder.tag.setTextColor(ContextCompat.getColor(context, R.color.foodColor));
                    break;
                case EditorActivity.TAG_ENTERTAINMENT:
                    holder.tag.setText(EditorActivity.TAG_ENTERTAINMENT);
                    holder.tag.setTextColor(ContextCompat.getColor(context, R.color.entertainmentColor));
                    break;
                case EditorActivity.TAG_GROCERIES:
                    holder.tag.setText(EditorActivity.TAG_GROCERIES);
                    holder.tag.setTextColor(ContextCompat.getColor(context, R.color.groceriesColor));
                    break;
                case EditorActivity.TAG_PURCHASES:
                    holder.tag.setText(EditorActivity.TAG_PURCHASES);
                    holder.tag.setTextColor(ContextCompat.getColor(context, R.color.purchaseColor));
                    break;
                case EditorActivity.TAG_TRAVEL:
                    holder.tag.setText(EditorActivity.TAG_TRAVEL);
                    holder.tag.setTextColor(ContextCompat.getColor(context, R.color.gasColor));
                    break;
            }
        }
        else {
            holder.tag.setText("");
            holder.dot.setText("");
        }
    }

    private static class ViewHolder {
        TextView name;
        TextView date;
        TextView amount;
        TextView tag;
        TextView dot;
    }
}
