package com.yxw.swipelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class MyListView extends ListView {
    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private int y;
    //将上下滑的事件拦截，才能够监听到listview的滚动
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                y= (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int yy = (int) ev.getY();
                if(Math.abs(y-yy)>50){
                    return true;
                }
                y=yy;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
