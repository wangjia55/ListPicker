package com.jacob.listpicker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.jacob.listpicker.Users;

import java.util.List;

/**
 * Package : com.jacob.listpicker
 * Author : jacob
 * Date : 15-4-8
 */
public class LinearPickerLayout extends LinearLayout {

    /**
     * 传入的人员列表
     */
    private List<Users> mUserList;

    /**
     * 默认屏幕显示的人员个数
     */
    public static final int DEFAULT_COUNT = 5;

    /**
     * 每个item的高度
     */
    private int mItemHeight;

    /**
     * 是否已经初始化
     */
    private boolean hasInit = false;

    /**
     * Y轴的偏移量
     */
    private int mTranslateY;

    /**
     * 当前的位置
     */
    private int mCurrentPosition;

    /**
     * Y轴最小的偏移量
     */
    private int mMinTranslate;

    /**
     * Y轴最大的偏移量
     */
    private int mMaxTranslate;

    /**
     * 绘制三角形画笔
     */
    private Paint mPaintTriangle;

    /**
     * 绘制三角形路径
     */
    private Path mPath;

    /**
     * 绘制三角形高度
     */
    private int mTriangleHeight;


    public LinearPickerLayout(Context context) {
        this(context, null);
    }

    public LinearPickerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearPickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        mPaintTriangle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTriangle.setDither(true);
        mPaintTriangle.setColor(Color.WHITE);
        mPaintTriangle.setStyle(Paint.Style.FILL);
        mPath = new Path();

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawPath(mPath, mPaintTriangle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        mItemHeight = heightSize / DEFAULT_COUNT;
        if (!hasInit) {
            addItemView();
            drawTriangle();
        }
        hasInit = true;
    }

    private void drawTriangle() {
        mTriangleHeight = (int) (mItemHeight * 1f / 8);
        mPath.reset();
        int top = (int) (getScaleY() + (mCurrentPosition + 0.5f) * mItemHeight + mTriangleDistance - mTriangleHeight / 2);
        int width = getMeasuredWidth();
        mPath.moveTo(width + 1, top);
        mPath.lineTo(width + 1, top + mTriangleHeight);
        mPath.lineTo(width - mTriangleHeight / 2, top + mTriangleHeight / 2);
        mPath.close();
        invalidate();
    }

    private void addItemView() {
        int count = mUserList.size();
        for (int i = 0; i < count; i++) {
            UserView userView = new UserView(getContext());
            LayoutParams layoutParams =
                    new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.height = mItemHeight;
            userView.setLayoutParams(layoutParams);
            userView.setUserInfo(mUserList.get(i));
            addView(userView);
        }
        mTranslateY = -mItemHeight * 2;
        mMinTranslate = -mItemHeight * 2;
        mMaxTranslate = (count - 3) * mItemHeight;
        mCurrentPosition = ((mItemHeight * 2) + mTranslateY) / mItemHeight;
        scrollTo(0, mTranslateY);
        showSelectedView(mCurrentPosition);
    }

    private void showSelectedView(int position) {
        if (mCurrentPosition != position) {
            View viewOld = getChildAt(mCurrentPosition);
            if (viewOld instanceof UserView) {
                ((UserView) viewOld).animScaleSmall();
            }
        }

        View view = getChildAt(position);
        if (view instanceof UserView) {
            ((UserView) view).animScaleBig();
        }
        mCurrentPosition = position;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private float mLastY;
    private float mTouchDistance;
    private float mTriangleDistance;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                y = event.getY();
                mTouchDistance = mLastY - y;
                mTriangleDistance = mLastY - y;
                int translate = (int) (mTranslateY + mTouchDistance);
                translate = limitTranslateY(translate);

                scrollTo(0, translate);
                drawTriangle();
                break;
            case MotionEvent.ACTION_UP:
                mTranslateY = (int) (mTranslateY + mTouchDistance);
                mTranslateY = calculateTranslate(mTranslateY);
                scrollTo(0, mTranslateY);
                mTouchDistance = 0;
                mTriangleDistance = 0;
                int position = ((mItemHeight * 2) + mTranslateY) / mItemHeight;
                showSelectedView(position);
                mLastY = event.getY();
                drawTriangle();
                break;
        }
        return true;
    }


    private int limitTranslateY(int translate) {
        if (translate <= mMinTranslate) {
            translate = mMinTranslate;
            mTriangleDistance = 0;
        }
        if (translate >= mMaxTranslate) {
            translate = mMaxTranslate;
            mTriangleDistance = 0;
        }
        return translate;
    }

    private int calculateTranslate(int translate) {
        translate = limitTranslateY(translate);

        int balance = translate % mItemHeight;
        int index = translate / mItemHeight;

        translate = index * mItemHeight;

        if (Math.abs(balance) > mItemHeight / 2) {
            if (index > 0) {
                translate = translate + mItemHeight;
            } else if (index == 0) {
                if (balance >= 0) {
                    translate = mItemHeight;
                } else {
                    translate = -mItemHeight;
                }
            } else {
                translate = translate - mItemHeight;
            }
        }
        return translate;
    }


    public void setUserList(List<Users> mUserList) {
        this.mUserList = mUserList;
    }


    private int getScreenHeight() {
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }
}
