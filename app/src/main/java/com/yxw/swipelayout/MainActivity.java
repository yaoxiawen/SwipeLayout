package com.yxw.swipelayout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        final MyListView mList = findViewById(R.id.lv);
        mList.setAdapter(new MyAdapter(MainActivity.this));
        //listview的滚动监听，需要重写onInterceptTouchEvent方法，将上下滑的事件拦截，才能监听到
        mList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //System.out.println("00000000000000000"+firstVisibleItem);//ok的，可以输出
                //System.out.println("00000000000000000"+visibleItemCount);
            }
        });
    }
}
