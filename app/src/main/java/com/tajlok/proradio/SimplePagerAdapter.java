package com.tajlok.proradio;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class SimplePagerAdapter extends PagerAdapter {

    private final Context mContext;
    private final ViewPager mPager;

    private final int[] layouts = {
            R.layout.show_tip_1,
            R.layout.show_tip_2,
            R.layout.show_tip_3,
    };

    public SimplePagerAdapter(Context context, ViewPager pager) {
        mContext = context;
        mPager = pager;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup viewGroup, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(
                layouts[position], viewGroup, false);

        Button btnNext = layout.findViewById(R.id.btn_next);
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (position < layouts.length - 1) {
                    setItem(position + 1);
                } else {
                    Intent intentMain = new Intent(mContext, ChoiceLoveActivity.class);
                    mContext.startActivity(intentMain);
                }
            });
        }

        viewGroup.addView(layout);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup viewGroup, int position, @NonNull Object view) {
        viewGroup.removeView((View) view);
    }

    @Override
    public int getCount() {
        return layouts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    private void setItem(int num) {
        if (num < 0) {
            num = 0;
        }
        if (num >= layouts.length) {
            num = layouts.length - 1;
        }

        mPager.setCurrentItem(num, true);
    }

}