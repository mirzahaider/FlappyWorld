package com.example.flappobird;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/*
 * this class manages gameover effects etc
 * it uses 2 nested classes ImageArea and ScoreCard
 */

public class GameoverManager {

    private class ImageArea {

        Bitmap gameoverImage;
        // initial image coordinates for gameoverImage
        float left;
        float top;
        Paint paint = new Paint();

        public void slideDown() {
            // add gravity to velocity
            // drag the image down by rate of velocity
            mVelocity = mVelocity + mGravity;
            top = top + mVelocity;
        }

        public void draw(Canvas canvas) {

            // paint Rect as a background color for gamaoverImage
            paint = new Paint();
            paint.setColor(-8073234);       // light blue
            canvas.drawRect(left, top, left+gameoverImage.getWidth(), top+gameoverImage.getHeight(), paint);
            // draw mGaveoverImage
            canvas.drawBitmap(mImageArea.gameoverImage, mImageArea.left, mImageArea.top, null);
        }
    }

    private class ScoreCard {
        // coordinates of rect
        float left;
        float top;
        float right;
        float bottom;
        Paint paint = new Paint();

        public void slideDown() {
            mVelocity = mVelocity + mGravity;
            top = top + mVelocity;
            bottom = bottom + mVelocity;
        }

        public void draw(Canvas canvas) {

            paint = new Paint();
            paint.setColor(-7590369);       // maroon
            canvas.drawRect(left, top, right, bottom, paint);

            // write the scores
            paint.setColor(Color.WHITE);
            paint.setTextSize(60);
            paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("BEST  :", (float)((right+left)*.55), top+100, paint);
            canvas.drawText("SCORE :", (float)((right+left)*.55), top+200, paint);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("\t\t"+mHiScore, (float)((right+left)*.55), top+100, paint);
            canvas.drawText("\t\t"+mScore, (float)((right+left)*.55), top+200, paint);
        }
    }

    Context mContext;
    int mFrameWidth = GameView.sViewWidth;
    int mFrameHeight = GameView.sViewHeight;

    private ImageArea mImageArea;
    private ScoreCard mScoreCard;
    private float mInitialVelocity = (float) 0.5;
    private float mVelocity;
    private float mGravity = (float) 1;
    private boolean mIsAnimationDone = false;       // false if updates can be done on ImageArea, ScoreCard
    private int mScore = 0;
    private int mHiScore;

    SharedPreferences mSharedPref;
    public static final String PREFERENCE_FILE_KEY = "PrefFileKey";
    public static final String HI_SCORE_KEY = "hiScore";

    public GameoverManager(Context context) {

        mContext = context;

        // read hi score from sharedPref file
        mSharedPref = mContext.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        mHiScore = mSharedPref.getInt(HI_SCORE_KEY, 0);

        mVelocity = mInitialVelocity;

        // set up mImageArea and scale its image
        mImageArea = new ImageArea();
        mImageArea.gameoverImage = GameView.decodeScaledBitmapFromResource(mContext.getResources(), R.drawable.gameover, mFrameWidth, 0);
        setImageAreaPos();

        // set up mScoreCard
        mScoreCard = new ScoreCard();
        setScoreCardPos();
    }

    public void draw(Canvas canvas) {

        mImageArea.draw(canvas);
        mScoreCard.draw(canvas);
    }

    public void update() {

        if(mImageArea.top < 15) {       // slide down only if ImageArea is above this threshold
            mImageArea.slideDown();
            mScoreCard.slideDown();
        }
        else {
            // if ImageArea somehow crosses the threshold due to gravity, bring it back
            // also set mIsAnimationDone = true since no more updates can be performed
            mImageArea.top = 15;
            mIsAnimationDone = true;
        }

    }

    public void setScore(int score) {

        mScore = score;
        // if hi score created, write on SharedPref file
        if(mScore > mHiScore) {
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putInt(HI_SCORE_KEY, mScore);
            editor.apply();
            mHiScore = mScore;
        }
    }

    public boolean isAnimationDone() {
        return mIsAnimationDone;
    }

    public void reset() {

        // set to initial states
        mVelocity = mInitialVelocity;
        setImageAreaPos();
        setScoreCardPos();
        mScore = 0;
        mIsAnimationDone = false;
    }

    private void setImageAreaPos() {

        mImageArea.top = -((float)(mFrameHeight * 3/4.0));      //ImageArea begins from above screen. Exact pos is relative to FrameHeight
        mImageArea.left = 0;
    }

    private void setScoreCardPos() {

        mScoreCard.left = mImageArea.left + 100;
        mScoreCard.top = mImageArea.top + mImageArea.gameoverImage.getHeight()  + 100;
        mScoreCard.right = mImageArea.left + mImageArea.gameoverImage.getWidth() - 100;
        mScoreCard.bottom = mScoreCard.top + 300;
    }
}
