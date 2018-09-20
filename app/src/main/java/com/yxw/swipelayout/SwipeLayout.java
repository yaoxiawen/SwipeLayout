package com.yxw.swipelayout;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class SwipeLayout extends FrameLayout {
    private ViewDragHelper mDragHelper;
    private View mBackView;
    private View mFrontView;
    private int mHeight;
    private int mWidth;
    private int mRange;//拖拽的最大范围
    private Status mStatus = Status.Close;
    private OnDragStatusChangeListener mListener;

    public SwipeLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwipeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化
        mDragHelper = ViewDragHelper.create(this, mCallback);
    }


    /**
     * 回调
     */
    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        /**
         * 根据返回结果决定当前child是否可以拖拽
         * @param child 当前被拖拽的View
         * @param pointerId 区分多点触摸的id
         * @return
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        /**
         * 返回拖拽的范围, 不对拖拽进行真正的限制. 仅仅决定了动画执行速度
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        /**
         * 根据建议值 修正将要移动到的(横向)位置
         * 此时没有发生真正的移动
         * left = oldLeft + dx;
         * @param child 当前拖拽的View
         * @param left 新的位置的建议值
         * @param dx 位置变化量
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // left
            if (child == mFrontView) {
                if (left > 0) {
                    return 0;
                } else if (left < -mRange) {
                    return -mRange;
                }
            } else if (child == mBackView) {
                if (left > mWidth) {
                    return mWidth;
                } else if (left < mWidth - mRange) {
                    return mWidth - mRange;
                }
            }
            return left;
        }

        /**
         * 当View位置改变的时候, 处理要做的事情 (更新状态, 伴随动画, 重绘界面)
         * 此时,View已经发生了位置的改变
         * @param changedView 改变位置的View
         * @param left 新的左边值
         * @param top
         * @param dx 水平方向变化量
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            // 传递事件
            if (changedView == mFrontView) {
                mBackView.offsetLeftAndRight(dx);
            } else if (changedView == mBackView) {
                mFrontView.offsetLeftAndRight(dx);
            }
            dispatchSwipeEvent();
            invalidate();
        }

        /**
         * 当View被释放的时候, 处理的事情(执行动画)
         * @param releasedChild 被释放的子View
         * @param xvel 水平方向的速度, 向右为+
         * @param yvel 竖直方向的速度, 向下为+
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            // 判断执行 关闭/开启
            // 先考虑所有开启的情况,剩下的就都是关闭的情况
            if (xvel == 0 && mFrontView.getLeft() < -mRange / 2.0f) {
                open();
            } else if (xvel < 0) {
                open();
            } else {
                close();
            }
        }
    };

    /**
     * 状态枚举
     */
    public enum Status {
        Close, Open, Draging;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    /**
     * 状态改变接口，向外暴露
     */
    public interface OnDragStatusChangeListener {
        void onClose(SwipeLayout mSwipeLayout);

        void onOpen(SwipeLayout mSwipeLayout);

        void onDraging(SwipeLayout mSwipeLayout);
        // 要去关闭
        void onStartClose(SwipeLayout mSwipeLayout);
        // 要去开启
        void onStartOpen(SwipeLayout mSwipeLayout);
    }

    /**
     * 设置状态监听，传入状态改变接口
     *
     * @param mListener
     */
    public void setDragStatusListener(OnDragStatusChangeListener mListener) {
        this.mListener = mListener;
    }

    protected void dispatchSwipeEvent() {
        if (mListener != null) {
            mListener.onDraging(this);
        }
        // 更新状态, 执行回调
        Status preStatus = mStatus;
        // 更新当前状态
        mStatus = updateStatus();
        if (preStatus != mStatus && mListener != null) {
            // 状态发生变化
            if (mStatus == Status.Close) {
                mListener.onClose(this);
            } else if (mStatus == Status.Open) {
                mListener.onOpen(this);
            } else if (mStatus == Status.Draging) {
                if (preStatus == Status.Close) {
                    mListener.onStartOpen(this);
                } else if (preStatus == Status.Open) {
                    mListener.onStartClose(this);
                }
            }
        }
    }

    private Status updateStatus() {

        int left = mFrontView.getLeft();
        if (left == 0) {
            return Status.Close;
        } else if (left == -mRange) {
            return Status.Open;
        }
        return Status.Draging;
    }

    /**
     * 将触摸事件传递给mDragHelper处理
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 获取子view的引用
     * 该方法当1级的子view全部加载完时会调用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取子view的引用
        mBackView = getChildAt(0);
        mFrontView = getChildAt(1);
    }

    /**
     * 当尺寸有变化的时候调用
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = mFrontView.getMeasuredHeight();
        mWidth = mFrontView.getMeasuredWidth();
        mRange = mBackView.getMeasuredWidth();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 摆放位置
        layoutContent(false);
    }

    /**
     * 摆放子view的位置
     *
     * @param isOpen
     */
    private void layoutContent(boolean isOpen) {
        // 摆放前View
        Rect frontRect = computeFrontViewRect(isOpen);
        mFrontView.layout(frontRect.left, frontRect.top, frontRect.right, frontRect.bottom);
        // 摆放后View
        Rect backRect = computeBackViewViaFront(frontRect);
        mBackView.layout(backRect.left, backRect.top, backRect.right, backRect.bottom);

        // 调整顺序, 把mFrontView前置
        bringChildToFront(mFrontView);
    }

    private Rect computeFrontViewRect(boolean isOpen) {
        int left = 0;
        if (isOpen) {
            left = -mRange;
        }
        return new Rect(left, 0, left + mWidth, 0 + mHeight);
    }

    private Rect computeBackViewViaFront(Rect frontRect) {
        int left = frontRect.right;
        return new Rect(left, 0, left + mRange, 0 + mHeight);
    }

    /**
     * 关闭
     */
    public void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        int finalLeft = 0;
        if (isSmooth) {
            //触发一个平滑动画
            if (mDragHelper.smoothSlideViewTo(mFrontView, finalLeft, 0)) {
                //返回true代表还没有移动到指定位置, 需要刷新界面.
                //参数传this(child所在的ViewGroup)
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(false);
        }
    }

    /**
     * 开启
     */
    public void open() {
        open(true);
    }

    public void open(boolean isSmooth) {
        int finalLeft = -mRange;
        if (isSmooth) {
            //触发一个平滑动画
            if (mDragHelper.smoothSlideViewTo(mFrontView, finalLeft, 0)) {
                //返回true代表还没有移动到指定位置, 需要刷新界面.
                //参数传this(child所在的ViewGroup)
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(true);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //持续平滑动画 (高频率调用)
        if (mDragHelper.continueSettling(true)) {
            //如果返回true, 动画还需要继续执行
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

}
