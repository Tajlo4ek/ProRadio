package com.tajlok.proradio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;

public class ViewRadioAdapter extends PagerAdapter {

    private final MainActivity mContext;
    private final int itemSize;

    private final List<Radio> radioList;

    private boolean isChanged;

    enum ViewType {
        LIKED("Понравившиеся"),
        ALL("Все"),
        POPULAR("Популярное");

        public final String title;

        ViewType(String title) {
            this.title = title;
        }
    }

    public ViewRadioAdapter(MainActivity activity, List<Radio> radioList, int itemSize) {
        mContext = activity;
        this.radioList = radioList;
        this.itemSize = itemSize;

        isChanged = true;
    }

    public void SetChanged() {
        isChanged = true;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(
                R.layout.radio_tab, viewGroup, false);

        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mContext.UpdateData(refreshLayout, position);
            }
        });

        List<Radio> radioList = new ArrayList<>();
        ViewType myType = ViewType.values()[position];

        layout.setTag(myType);

        for (int i = 0; i < this.radioList.size(); i++) {

            Radio item = this.radioList.get(i);

            switch (myType) {
                case LIKED:
                    if (item.getUserLike()) radioList.add(item);
                    break;
                case ALL:
                    radioList.add(item);
                    break;
                case POPULAR:
                    if (item.getPopular()) radioList.add(item);
                    break;
            }
        }


        parseData(layout.findViewById(R.id.radio_table), radioList, itemSize);
        viewGroup.addView(layout);


        return layout;
    }

    @Override
    public void destroyItem(ViewGroup viewGroup, int position, Object view) {
        //viewGroup.removeView((View) view);
    }

    public int getItemPosition(Object object) {

        if (isChanged) {
            isChanged = false;
            return POSITION_NONE;
        }

        return POSITION_UNCHANGED;
    }

    @Override
    public int getCount() {
        return ViewType.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ViewType.values()[position].title;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    private void parseData(TableLayout table, List<Radio> radioList, int size) {

        int rowCount = (radioList.size() + 1) / 2;

        for (int rowInd = 0; rowInd < rowCount; rowInd++) {
            TableRow tableRow = new TableRow(mContext);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT));

            for (int i = rowInd * 2; (i < (rowInd + 1) * 2) && (i < radioList.size()); i++) {
                tableRow.addView(viewFromRadio(mContext, radioList.get(i), size));
            }

            table.addView(tableRow, rowInd);
        }

    }

    private View viewFromRadio(Context context, Radio radio, int size) {

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        TableRow.LayoutParams lineParams = new TableRow.LayoutParams(
                size,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.5f);
        root.setLayoutParams(lineParams);

        LinearLayout textLine = new LinearLayout(context);
        textLine.setLayoutParams(new LinearLayout.LayoutParams(
                size,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f)
        );

        TextView text = new TextView(context);
        text.setText(radio.getName());
        text.setGravity(Gravity.CENTER);
        text.setTextColor(context.getResources().getColor(R.color.black));
        text.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.5f
                )
        );


        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.RunRadio(radio);
            }
        });

        textLine.addView(text);

        ImageView image = new ImageView(context);
        image.setLayoutParams(
                new LinearLayout.LayoutParams(
                        size,
                        size
                )
        );

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Drawable drawable = ImageBuffer.GetImage(radio.getCoverUrl());

                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            image.setImageDrawable(drawable);
                        }
                    };

                    mainHandler.post(runnable);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        root.addView(image);
        root.addView(textLine);

        return root;
    }


}