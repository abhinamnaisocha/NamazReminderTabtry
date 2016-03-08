package com.mba.tabtry;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Muhammad Bilal on 08/02/2016.
 */
public class PageAdapter extends FragmentStatePagerAdapter {

    int numTabs = 2;

    public PageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.numTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                NamazFragment tab1 = new NamazFragment();
                return tab1;
            case 1:
                QiblaDirectionFragment tab2 = new QiblaDirectionFragment();
                return tab2;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
