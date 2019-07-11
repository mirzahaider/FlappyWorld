package com.example.flappobird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Ground {

    private Context mContext;
    private int mFrameWidth = GameView.sViewWidth;
    private int mFrameHeight = GameView.sViewHeight;

    private Bitmap mImage;
    private Rect mImageRect1;                                                           // rect1 that fills the image
    private Rect mImageRect2;                                                           // rect2 ..
    static float sTop = (float) (83.3/100.0 * GameView.sViewHeight);                    // top coordinate of mImage
    static float sVelocity = (float) ((0.43055/100.0 * GameView.sViewWidth)*0.06);      // pixels per millisecond - expressed as percentage of frameWidth
    private long mDeltaTime;

    public Ground(Context context) {

        mContext = context;

        //mImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ground);
        mImage = GameView.decodeScaledBitmapFromResource(mContext.getResources(), R.drawable.ground, mFrameWidth, 0);
        mImageRect1 = new Rect(0, (int) sTop, mFrameWidth, mFrameHeight);
        mImageRect2 = new Rect(mImageRect1.right, (int) sTop, mFrameWidth*2, mFrameHeight);

        System.out.println("*** Ground.java ***");
        System.out.println("sTop = "+sTop);
        System.out.println("sVelocity = "+sVelocity);
    }

    public void draw(Canvas canvas) {

        canvas.drawBitmap(mImage, null, mImageRect1, null);
        canvas.drawBitmap(mImage, null, mImageRect2, null);
    }

    public void update() {

        // Ground should update only if game not over yet
        if(!GameView.sGameOver) {

            // get delta time
            mDeltaTime = GameView.getDeltaTime();

            // update left/right coordinates of both Rects
            mImageRect1.left = Math.round(mImageRect1.left - sVelocity*mDeltaTime);
            mImageRect1.right = Math.round(mImageRect1.right - sVelocity*mDeltaTime);
            mImageRect2.left = Math.round(mImageRect2.left - sVelocity*mDeltaTime);
            mImageRect2.right = Math.round(mImageRect2.right - sVelocity*mDeltaTime);

            // reset Rects if going out of screen
            if (mImageRect1.right <= 0) {
                mImageRect1.left = 0;
                mImageRect1.right = mFrameWidth;
                mImageRect2.left = mImageRect1.right;
                mImageRect2.right = mFrameWidth * 2;
            }
        }
    }

    public boolean isColliding(Bird bird) {

        // return true if bird is below ground i.e bird's Y-pos less than sTop
        if(bird.getYpos()+(bird.getImage().getHeight()/2 )> sTop) {

            bird.jump();
            return true;
        }
        return false;
    }
}
