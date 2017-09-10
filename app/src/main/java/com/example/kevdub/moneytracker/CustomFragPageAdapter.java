package com.example.kevdub.moneytracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import layout.HeaderFragment;

/**
 * Created by kevinwang on 5/25/17.
 */

public class CustomFragPageAdapter extends FragmentPagerAdapter {

    private static int NUM_ITEMS = 3;

    public CustomFragPageAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
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
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return MainActivity.SPEND_MONEY;
            case 1:
                return MainActivity.SAVE_MONEY;
            case 2:
                return MainActivity.TOT_MONEY;
            default:
                return "";
        }
    }
}
