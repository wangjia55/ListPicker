package com.jacob.listpicker.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jacob.listpicker.R;
import com.jacob.listpicker.Users;

/**
 * Package : com.jacob.listpicker.view
 * Author : jacob
 * Date : 15-4-8
 * Description : 这个类是用来xxx
 */
public class UserView extends RelativeLayout {
    public static final float SCALE_BIG = 1.2f;
    public static final float SCALE_SMALL = 0.8f;

    private RoundImageView mImageView;
    private TextView mTextView;

    private ObjectAnimator mAnimBig;
    private ObjectAnimator mAnimSmall;

    public UserView(Context context) {
        this(context, null);
    }

    public UserView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_user_view, this);
        mImageView = (RoundImageView) findViewById(R.id.round_image_view);
        mTextView = (TextView) findViewById(R.id.text_view);

        PropertyValuesHolder bigHolder1 = PropertyValuesHolder.ofFloat(View.SCALE_X, SCALE_BIG);
        PropertyValuesHolder bigHolder2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, SCALE_BIG);
        mAnimBig = ObjectAnimator.ofPropertyValuesHolder(mImageView, bigHolder1, bigHolder2);
        mAnimBig.setDuration(300);

        PropertyValuesHolder smallHolder1 = PropertyValuesHolder.ofFloat(View.SCALE_X, SCALE_BIG, 1f);
        PropertyValuesHolder smallHolder2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, SCALE_BIG, 1f);
        mAnimSmall = ObjectAnimator.ofPropertyValuesHolder(mImageView, smallHolder1, smallHolder2);
        mAnimSmall.setDuration(300);

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void setUserInfo(Users users) {
        mImageView.setImageResource(users.getAvatar());
        mTextView.setText(users.getName());
    }

    public void animScaleBig() {
        mAnimBig.start();
    }

    public void animScaleSmall() {
        mAnimSmall.start();
    }
}
