package com.jacob.listpicker.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jacob.listpicker.R;
import com.jacob.listpicker.Users;

import java.util.ArrayList;
import java.util.List;

/**
 * Package : com.jacob.listpicker.view
 * Author : jacob
 * Date : 15-4-10
 * Description : 这个类是用来xxx
 */
public class ListPickerView extends ScrollView {
    public static final int DEFAULT_OFFSET = 2;
    public static final int DELAY = 25;

    private int mItemHeight;

    private int mItemWidth;

    private int mOffset = DEFAULT_OFFSET;

    private Paint mPaintTriangle;

    private Path mPath;

    private int mSelection;

    private int mTriangleHeight;

    private int mDisplayCount;

    private List<Users> mUserList;

    private List<ViewHolder> mViewHolders;

    private LinearLayout mLinearContainer;

    private int mInitY;

    private AutoScrollRunnable mAutoRunnable = new AutoScrollRunnable();

    private OnListPickerListener mPickerListener;

    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mDisAnimator;

    public ListPickerView(Context context) {
        this(context, null);
    }

    public ListPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        this.setVerticalScrollBarEnabled(false);
        this.setOverScrollMode(OVER_SCROLL_NEVER);

        mLinearContainer = new LinearLayout(getContext());
        mLinearContainer.setOrientation(LinearLayout.VERTICAL);
        this.addView(mLinearContainer);

        mPaintTriangle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTriangle.setStyle(Paint.Style.FILL);
        mPaintTriangle.setColor(Color.WHITE);
        mPaintTriangle.setDither(true);

        mDisplayCount = mOffset * 2 + 1;

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mItemWidth = w;
        setBackgroundDrawable(null);
    }


    @Override
    public void setBackgroundDrawable(Drawable background) {
        background = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                if (mPath == null) {
                    mPath = createPath();
                }
                canvas.drawPath(mPath, mPaintTriangle);
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter cf) {

            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };
        super.setBackgroundDrawable(background);
    }

    private Path createPath() {
        mTriangleHeight = mItemHeight / 7;
        int top = (int) ((mOffset + 0.5) * mItemHeight - mTriangleHeight);
        int left = mItemWidth - mTriangleHeight * 2 / 3;
        Path path = new Path();
        path.moveTo(mItemWidth + 1, top);
        path.lineTo(mItemWidth + 1, top + mTriangleHeight);
        path.lineTo(left, top + mTriangleHeight / 2);
        path.close();
        return path;
    }

    public void setItems(List<Users> userList) {
        if (mUserList == null) {
            mUserList = new ArrayList<>();
        }
        if (mViewHolders == null) {
            mViewHolders = new ArrayList<>();
        }
        mUserList.clear();
        mViewHolders.clear();
        mUserList.addAll(userList);

        for (int i = 0; i < mOffset; i++) {
            mUserList.add(0, null);
            mUserList.add(null);
        }
        addItemView();
        mSelection = mOffset;
        refreshUI();
    }

    private void addItemView() {
        int size = mUserList.size();
        for (int i = 0; i < size; i++) {
            mLinearContainer.addView(createItemView(mUserList.get(i)));
        }
    }

    private View createItemView(Users users) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_user_view, mLinearContainer, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.imageView = (ImageView) view.findViewById(R.id.round_image_view);
        viewHolder.textView = (TextView) view.findViewById(R.id.text_view);
        viewHolder.textView.setPadding(0, dpToPx(10), 0, 0);
        int padding = dpToPx(10);
        view.setPadding(0, padding, 0, 0);
        if (users == null) {
            viewHolder.imageView.setImageDrawable(null);
            viewHolder.textView.setText("");
        } else {
            viewHolder.imageView.setImageResource(users.getAvatar());
            viewHolder.textView.setText(users.getName());
        }
        mViewHolders.add(viewHolder);
        if (mItemHeight == 0) {
            mItemHeight = getViewMeasureHeight(view);
            mLinearContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight * mDisplayCount));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
            this.setLayoutParams(new LinearLayout.LayoutParams(params.width, mItemHeight * mDisplayCount));
        }
        return view;
    }


    private int getViewMeasureHeight(View view) {
        int childWidthSpec;
        int childHeightSpec;

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, layoutParams.width);

        int height = layoutParams.height;
        if (height > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(childWidthSpec, childHeightSpec);
        return view.getMeasuredHeight();
    }


    @Override
    public void fling(int velocityY) {
        super.fling(velocityY / 3);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                startToScrollAtPosition();
                break;
        }
        return super.onTouchEvent(ev);
    }


    private void startToScrollAtPosition() {
        mInitY = getScrollY();
        postDelayed(mAutoRunnable, DELAY);
    }

    private class AutoScrollRunnable implements Runnable {

        int tempY;

        @Override
        public void run() {
            tempY = getScrollY();
            if (tempY - mInitY == 0) {
                int position = mInitY / mItemHeight;
                int remain = mInitY % mItemHeight;
                if (remain == 0) {
                    mSelection = position + mOffset;
                    onCallBack();
                } else {
                    if (remain > mItemHeight / 2) {
                        mSelection = position + mOffset + 1;
                        smoothScrollTo(0, (position + 1) * mItemHeight);
                        onCallBack();
                    } else {
                        mSelection = position + mOffset;
                        smoothScrollTo(0, position * mItemHeight);
                        onCallBack();
                    }
                }

            } else {
                mInitY = getScrollY();
                postDelayed(this, DELAY);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void onCallBack() {
        if (mPickerListener != null) {
            mPickerListener.onListPicker(mSelection - mOffset);
        }
        refreshUI();
    }

    private void refreshUI() {
        int size = mLinearContainer.getChildCount();
        for (int i = 0; i < size; i++) {
            ImageView imageView = mViewHolders.get(i).imageView;
            if (i == mSelection) {
                PropertyValuesHolder bigHolder1 = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f);
                PropertyValuesHolder bigHolder2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
                mShowAnimator = ObjectAnimator.ofPropertyValuesHolder(imageView, bigHolder1, bigHolder2);
                mShowAnimator.setDuration(250);
                mShowAnimator.start();
            } else {
                PropertyValuesHolder smallHolder1 = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f);
                PropertyValuesHolder smallHolder2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f);
                mDisAnimator = ObjectAnimator.ofPropertyValuesHolder(imageView, smallHolder1, smallHolder2);
                mDisAnimator.setDuration(200);
                mDisAnimator.start();
            }
        }
    }

    public interface OnListPickerListener {
        void onListPicker(int position);
    }

    public void setOnListPickerListener(OnListPickerListener listener) {
        this.mPickerListener = listener;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView textView;
    }
}
