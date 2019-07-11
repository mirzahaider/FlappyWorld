package com.example.flappobird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Bird implements Runnable {

    private int mFrameHeight = GameView.sViewHeight;
    private int mFrameWidth = GameView.sViewWidth;
    private Context mContext;

    private Bitmap[] mImages;
    private int mImageCount = 2;
    private int mCurrentImage = 0;                 // index of image to draw
    private double mBirdHeight = 6.0/100;          // a percentage of frame's height

    private float mXpos;
    private float mYpos;

    private float mInitialXpos;
    private float mInitialYpos;
    private float mVelocity = (float) 0.00034 * mFrameHeight;//0.4;         // pixels per millis - relative to mFrameHeight
    private float mGravity = (float) (0.000304054 * mFrameHeight * 0.06);   // pixels per millis - relative to mFrameHeight
    private float mJump = (float) ((0.858/100.00 * mFrameHeight));          // expressed as a percentage of mFrameHeight

    private final int FLAP_TIME = 160;      // milliseconds between a flap
    private Thread mThread;
    private boolean mRunning = false;       // decides whether to keep running or exit from the thread loop
    private boolean mFlap = true;           // inside the thread loop, decides whether to flap bird or not
    private boolean mDead = false;          // the bird is dead now

    // default constructor
    public Bird(Context context) {

        mContext = context;

        System.out.println("*** Bird.java ***");
        System.out.println("Bird.frameWidth: "+mFrameWidth);
        System.out.println("Bird.frameHeight: "+mFrameHeight);
        System.out.println("Bird.mJump: "+mJump);

        // set up Bird images and tune dimensions
        mImages = new Bitmap[mImageCount];
        int reqHeight = (int) (mFrameHeight * mBirdHeight);
        mImages[0] = GameView.decodeScaledBitmapFromResource(mContext.getResources(), R.drawable.bird1, 0, reqHeight);
        mImages[1] = GameView.decodeScaledBitmapFromResource(mContext.getResources(), R.drawable.bird2, 0, reqHeight);

        initializeBirdPos();
    }

    public void draw(Canvas canvas) {

        canvas.drawBitmap(mImages[mCurrentImage], mXpos, mYpos, null);
    }

    public void fall() {

        // bird should update/fall only if game has started
        if(GameView.sGameStarted) {

            // the Y-pos is incremented by velocity
            // the velocity is then incremented by gravity*deltaTime
            mYpos += mVelocity;
            mVelocity += mGravity * GameView.getDeltaTime();

            // bird is dead, mDead=true if it sufficiently crosses mFrameHeight
            if (mYpos > mFrameHeight * 1.25) {
                mYpos = (float) (mFrameHeight * 1.25);
                mDead = true;
            }
        }
    }

    public void jump() {

        if(mYpos+mImages[mCurrentImage].getHeight() > 0) {      // if the bird is not above screen

            mVelocity = -mJump;     //change the direction of bird movement by value of mJump
        }
    }

    // return the current image i.e the image being displayed
    public Bitmap getImage() {

        return mImages[mCurrentImage];
    }

    // set initial bird positions
    public void initializeBirdPos() {

        mInitialXpos = mFrameWidth * 30/100 - mImages[mCurrentImage].getWidth()/2;
        mInitialYpos = mFrameHeight * 40/100 - mImages[mCurrentImage].getHeight()/2;
        mXpos = mInitialXpos;
        mYpos = mInitialYpos;
    }

    public float getXpos() {
        return mXpos;
    }

    public float getYpos() {
        return mYpos;
    }

    public float getInitialXpos() {
        return mInitialXpos;
    }

    public float getInitialYpos() {
        return mInitialYpos;
    }

    @Override
    public void run() {

        while(mRunning) {
            if(mFlap) {
                if (mCurrentImage == 0)
                    mCurrentImage = 1;
                else
                    mCurrentImage = 0;

                try {
                    Thread.sleep(FLAP_TIME);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void startFlapThread() {

            mRunning = true;
            mThread = new Thread(this);
            mThread.start();
    }

    public void stopFlapThread() {

        mRunning = false;
        try {
            mThread.join();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void startFlapping() {
        mFlap = true;
    }

    public void stopFlapping() {
        mFlap = false;
    }

    public boolean isDead() {
        return mDead;
    }

    public void reset() {

        initializeBirdPos();
        mFlap = true;
        mDead = false;
    }
}
