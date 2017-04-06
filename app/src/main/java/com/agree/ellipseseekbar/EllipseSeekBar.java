package com.agree.camberseekbar;

/**
 * 作者：王海洋
 * 时间：2017/2/28 16:37
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

class EllipseSeekBar extends View {

    private final boolean DEBUG = true;
    private final String TAG = "CircleSeekBar";

    private Context mContext = null;
    private AttributeSet mAttrs = null;

    private Drawable mThumbDrawable = null;
    private int mThumbHeight = 0;
    private int mThumbWidth = 0;
    private int[] mThumbNormal = null;
    private int[] mThumbPressed = null;

    private int mSeekBarMax = 0;
    private Paint mSeekBarBackgroundPaint = null;
    private Paint mSeekbarProgressPaint = null;
    private Paint mWithInPaint = null;
    private RectF mArcRectF = null;
    private RectF mWithInArcRectF = null;
    private Paint mPaintCircleLeft = null;
    private Paint mPaintCircleRiht = null;

    private boolean mIsShowProgressText = false;
    private Paint mProgressTextPaint = null;
    private int mProgressTextSize = 0;

    private int mViewHeight = 0;
    private int mViewWidth = 0;
    private int mSeekBarSize = 0;
    private int mSeekBarRadius = 0;
    private int mSeekBarCenterX = 0;
    private int mSeekBarCenterY = 0;
    private float mThumbLeft = 0;
    private float mThumbTop = 0;

    private float mSeekBarDegree = 1.0f;
    private int mCurrentProgress = 0;
    private double temporaryRadian = -1;
    private float temporaryLocation = 0;
    private int minRadian = 140;
    private int swipRadian = 260;
    private double progreeRadian = 0;
    private boolean isFirst = true;
    private float bottomProgressWidth;

    int bottomCx1 = 0;
    int bottomCy1 = 0;
    int bottomArcRadius = 0;

    public EllipseSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mAttrs = attrs;
        initView();
    }

    public EllipseSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        initView();
    }

    public EllipseSeekBar(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    private void initView(){
        if(DEBUG) Log.d(TAG, "initView");
        TypedArray localTypedArray = mContext.obtainStyledAttributes(mAttrs, R.styleable.EllipseSeekBar);

        //thumb的属性是使用android:thumb属性进行设置的
        //返回的Drawable为一个StateListDrawable类型，即可以实现选中效果的drawable list
        //mThumbNormal和mThumbPressed则是用于设置不同状态的效果，当点击thumb时设置mThumbPressed，否则设置mThumbNormal
        mThumbDrawable = localTypedArray.getDrawable(R.styleable.EllipseSeekBar_android_thumb);
        mThumbWidth = this.mThumbDrawable.getIntrinsicWidth();
        mThumbHeight = this.mThumbDrawable.getIntrinsicHeight();

        mThumbNormal = new int[]{-android.R.attr.state_focused, -android.R.attr.state_pressed,
                -android.R.attr.state_selected, -android.R.attr.state_checked};
        mThumbPressed = new int[]{android.R.attr.state_focused, android.R.attr.state_pressed,
                android.R.attr.state_selected, android.R.attr.state_checked};

        float progressWidth = localTypedArray.getDimension(R.styleable.EllipseSeekBar_progress_width, 5);
        int progressBackgroundColor = localTypedArray.getColor(R.styleable.EllipseSeekBar_progress_background, Color.GRAY);
        int progressFrontColor = localTypedArray.getColor(R.styleable.EllipseSeekBar_progress_front, Color.BLUE);
        mSeekBarMax = localTypedArray.getInteger(R.styleable.EllipseSeekBar_progress_max, 100);

        mSeekbarProgressPaint = new Paint();
        mSeekBarBackgroundPaint = new Paint();
        mWithInPaint = new Paint();

        mSeekbarProgressPaint.setColor(progressFrontColor);
        mSeekBarBackgroundPaint.setColor(progressBackgroundColor);
        mWithInPaint.setColor(progressFrontColor);

        mSeekbarProgressPaint.setAntiAlias(true);
        mSeekBarBackgroundPaint.setAntiAlias(true);
        mWithInPaint.setAntiAlias(true);

        mSeekbarProgressPaint.setStyle(Paint.Style.STROKE);
        mSeekBarBackgroundPaint.setStyle(Paint.Style.STROKE);
        mWithInPaint.setStyle(Paint.Style.STROKE);

        mSeekBarBackgroundPaint.setStrokeWidth(progressWidth);
        mSeekbarProgressPaint.setStrokeWidth(progressWidth);
        mWithInPaint.setStrokeWidth(5);
        bottomProgressWidth = progressWidth;

        mArcRectF = new RectF();
        mWithInArcRectF = new RectF();

        mIsShowProgressText = localTypedArray.getBoolean(R.styleable.EllipseSeekBar_show_progress_text, false);
        int progressTextStroke = (int) localTypedArray.getDimension(R.styleable.EllipseSeekBar_progress_text_stroke_width, 5);
        int progressTextColor = localTypedArray.getColor(R.styleable.EllipseSeekBar_progress_text_color, Color.GREEN);
        mProgressTextSize = (int) localTypedArray.getDimension(R.styleable.EllipseSeekBar_progress_text_size, 50);
        mProgressTextPaint = new Paint();
        mProgressTextPaint.setColor(progressTextColor);
        mProgressTextPaint.setAntiAlias(true);
        mProgressTextPaint.setStrokeWidth(progressTextStroke);
        mProgressTextPaint.setTextSize(mProgressTextSize);

        // ProgressBar结尾和开始画2个圆，实现ProgressBar的圆角。

        mPaintCircleLeft = new Paint();
        mPaintCircleLeft.setAntiAlias(true);
        mPaintCircleLeft.setColor(progressFrontColor);

        mPaintCircleRiht = new Paint();
        mPaintCircleRiht.setAntiAlias(true);
        mPaintCircleRiht.setColor(progressBackgroundColor);
        temporaryRadian = Math.toRadians(minRadian);

        localTypedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(DEBUG) Log.d(TAG, "onMeasure");
        mViewWidth = getWidth();
        mViewHeight = getHeight();

        mSeekBarSize = mViewWidth > mViewHeight ? mViewHeight : mViewWidth;

        mSeekBarCenterX = mViewWidth / 2;
        mSeekBarCenterY = mViewHeight / 2;

        mSeekBarRadius = mSeekBarSize / 2 - mThumbWidth / 2;

        int left = mSeekBarCenterX - mSeekBarRadius;
        int right = mSeekBarCenterX + mSeekBarRadius;
        int top = mSeekBarCenterY - mSeekBarRadius;
        int bottom = mSeekBarCenterY + mSeekBarRadius;
        mArcRectF.set(left, top, right, bottom);
        mWithInArcRectF.set(left + 40, top + 40, right - 40, bottom - 40);

        // 计算弧形的圆心和半径。
        bottomCx1 = (left + right) / 2;
        bottomCy1 = (top + bottom) / 2;
        bottomArcRadius = (right - left) / 2;


        // 起始位置，左下角
        setThumbPosition(Math.toRadians(minRadian));
    }

    @Override
    protected void onDraw(Canvas canvas) {



        canvas.drawCircle(
                (float) (bottomCx1 + bottomArcRadius * Math.cos(minRadian * 3.14 / 180)),
                (float) (bottomCy1 + bottomArcRadius * Math.sin(minRadian * 3.14 / 180)),
                bottomProgressWidth / 2, mPaintCircleLeft);// 小圆

        canvas.drawCircle(
                (float) (bottomCx1 + bottomArcRadius
                        * Math.cos((180 - minRadian) * 3.14 / 180)),
                (float) (bottomCy1 + bottomArcRadius
                        * Math.sin((180 - minRadian) * 3.14 / 180)),
                bottomProgressWidth / 2, mPaintCircleRiht);// 小圆


        canvas.drawArc(this.mArcRectF, minRadian, swipRadian, false,
                mSeekBarBackgroundPaint);
//        canvas.drawCircle(mSeekBarCenterX, mSeekBarCenterY, mSeekBarRadius,
//                mSeekBarBackgroundPaint);
        canvas.drawArc(this.mArcRectF, minRadian, mSeekBarDegree, false, mSeekbarProgressPaint);
        canvas.drawArc(this.mWithInArcRectF, minRadian, swipRadian, false, mWithInPaint);
//        canvas.drawArc(this.mArcRectF, 0.0F, mSeekBarDegree, false, mSeekbarProgressPaint);
        drawThumbBitmap(canvas);
        drawProgressText(canvas);

        super.onDraw(canvas);
    }

    private void drawThumbBitmap(Canvas canvas) {
        this.mThumbDrawable.setBounds((int) mThumbLeft, (int) mThumbTop,
                (int) (mThumbLeft + mThumbWidth), (int) (mThumbTop + mThumbHeight));
        this.mThumbDrawable.draw(canvas);
    }

    private void drawProgressText(Canvas canvas) {
        if (true == mIsShowProgressText){
            float textWidth = mProgressTextPaint.measureText("" + mCurrentProgress);
            canvas.drawText("" + mCurrentProgress, mSeekBarCenterX - textWidth / 2, mSeekBarCenterY
                    + mProgressTextSize / 2, mProgressTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                seekTo(eventX, eventY, false);
                break ;

            case MotionEvent.ACTION_MOVE:
                seekTo(eventX, eventY, false);
                break ;

            case MotionEvent.ACTION_UP:
                seekTo(eventX, eventY, true);
                break ;
        }
        return true;
    }

    private void seekTo(float eventX, float eventY, boolean isUp) {
        if (true == isPointOnThumb(eventX, eventY) && false == isUp) {

            mThumbDrawable.setState(mThumbPressed);
            double radian = Math.atan2(eventY - mSeekBarCenterY, eventX - mSeekBarCenterX);
            /*
             * 由于atan2返回的值为[-pi,pi]
             * 因此需要将弧度值转换一下，使得区间为[0,2*pi]
             */
            if (radian < 0){
                radian = radian + 2*Math.PI;
            }

            double max = Math.toRadians(minRadian);
            int temp = 0;
            if(swipRadian - (360 - minRadian) > 0){
                temp = swipRadian - (360 - minRadian);
            }
            double min = Math.toRadians(temp);
//        if(radian < 2.36){
//            radian = 2.36 ;
//        }else if(radian > 0.6){
//            radian = max ;
//        }
//            if(!isFirst){
//                if(Math.abs(temporaryRadian - radian) > 1 && Math.abs(temporaryRadian - radian) < 6){
//                    return;
//                }
//            }else{
//
//            }&& !isFirst
            if(Math.abs(temporaryRadian - radian) > 1 && Math.abs(temporaryRadian - radian) < 6 ){
                Log.e("why", "111111");
                Log.e("why", Math.abs(temporaryRadian - radian) + "!!!!!");
                return;
            }
//            if(isFirst && Math.abs(temporaryRadian - radian) < 1){
//                Log.e("why", "222222");
//                Log.e("why", Math.abs(temporaryRadian - radian) + "!!!!!");
//                return;
//            }
            Log.e("why", Math.abs(temporaryRadian - radian) + "!!!!!");
            isFirst = false;
            if(min<radian && radian < max){
                radian = temporaryRadian;
            }else {
                temporaryRadian = radian;
            }


            double tempProgreeRadian = radian;
            if(radian == 0){
                tempProgreeRadian = Math.toRadians(360);
            }
            if(0 < radian && radian < 0.9){
                tempProgreeRadian = Math.toRadians(360) + radian;
            }
            setThumbPosition(radian);

            progreeRadian = tempProgreeRadian - Math.toRadians(minRadian);
            if(DEBUG) Log.e(TAG, "seekTo radian = " + progreeRadian + "T" + Math.toRadians(360) + "T" + Math.toRadians(0) + "T" + Math.toRadians(minRadian));
            mSeekBarDegree = (float) Math.round(Math.toDegrees(progreeRadian));
            //因一些机型等于0会画成一个圈
            if(mSeekBarDegree == 0 || mSeekBarDegree < 0){
                mSeekBarDegree = 1.0f;
            }
            mCurrentProgress = (int) (mSeekBarMax * mSeekBarDegree / swipRadian);
            if(DEBUG) Log.e(TAG, "mCurrentProgress = " + mCurrentProgress);
            Log.e("why", "mSeekBarDegree:" + mSeekBarDegree);
            invalidate();
        }else{
            mThumbDrawable.setState(mThumbNormal);
            invalidate();
        }
    }

    private boolean isPointOnThumb(float eventX, float eventY) {
        boolean result = false;
        double distance = Math.sqrt(Math.pow(eventX - mSeekBarCenterX, 2)
                + Math.pow(eventY - mSeekBarCenterY, 2));
        if (distance < mSeekBarSize && distance > (mSeekBarSize / 2 - mThumbWidth)){
            result = true;
        }
        return result;
    }

    private void setThumbPosition(double radian) {
        if(DEBUG) Log.v(TAG, "setThumbPosition radian = " + radian);


        double x = mSeekBarCenterX + mSeekBarRadius * Math.cos(radian);
        double y = mSeekBarCenterY + mSeekBarRadius * Math.sin(radian);
//        if(DEBUG) Log.v(TAG, "setThumbPosition radian = " + x + "$$$" + y);
        mThumbLeft = (float) (x - mThumbWidth / 2);
        mThumbTop = (float) (y - mThumbHeight / 2);
    }

    /*
     * 增加set方法，用于在java代码中调用
     */
    public void setProgress(int progress) {
        if(DEBUG) Log.v(TAG, "setProgress progress = " + progress);
        if (progress > mSeekBarMax){
            progress = mSeekBarMax;
        }
        if (progress < 0){
            progress = 0;
        }
        mCurrentProgress = progress;
        mSeekBarDegree = (progress * 360 / mSeekBarMax);
        if(DEBUG) Log.d(TAG, "setProgress mSeekBarDegree = " + mSeekBarDegree);
        setThumbPosition(Math.toRadians(mSeekBarDegree));

        invalidate();
    }

    public int getProgress(){
        return mCurrentProgress;
    }

    public void setProgressMax(int max){
        if(DEBUG) Log.v(TAG, "setProgressMax max = " + max);
        mSeekBarMax = max;
    }

    public int getProgressMax(){
        return mSeekBarMax;
    }

    public void setProgressThumb(int thumbId){
        mThumbDrawable = mContext.getResources().getDrawable(thumbId);
    }

    public void setProgressWidth(int width){
        if(DEBUG) Log.v(TAG, "setProgressWidth width = " + width);
        mSeekbarProgressPaint.setStrokeWidth(width);
        mSeekBarBackgroundPaint.setStrokeWidth(width);
    }

    public void setProgressBackgroundColor(int color){
        mSeekBarBackgroundPaint.setColor(color);
    }

    public void setProgressFrontColor(int color){
        mSeekbarProgressPaint.setColor(color);
    }

    public void setProgressTextColor(int color){
        mProgressTextPaint.setColor(color);
    }

    public void setProgressTextSize(int size){
        if(DEBUG) Log.v(TAG, "setProgressTextSize size = " + size);
        mProgressTextPaint.setTextSize(size);
    }

    public void setProgressTextStrokeWidth(int width){
        if(DEBUG) Log.v(TAG, "setProgressTextStrokeWidth width = " + width);
        mProgressTextPaint.setStrokeWidth(width);
    }

    public void setIsShowProgressText(boolean isShow){
        mIsShowProgressText = isShow;
    }
}