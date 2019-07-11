package com.example.flappobird;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Context mContext;
    private SurfaceHolder mSurfaceHolder;
    boolean mRunning;
    private Thread mThread;

    private long mStartTime;
    private static long sDeltaTime;
    private int mTargetFPS = 60;
    private long mTargetFrameTime = (long) (1000.0 / mTargetFPS);
    private long mTotalFrameTime = 0;
    private int mFrameCount;

    static int sViewWidth;
    static int sViewHeight;

    private Bitmap mBackgroundImage;
    private Bitmap mClickImage;
    private Bird mBird;
    private Ground mGround;
    private WallObstacleManager mWallObstacleManager;
    private GameoverManager mGameoverManager;
    MediaPlayer mCollsionSound;

    private int mScore = 0;                                     // current score
    Paint mScorePaint;

    boolean mGamePaused = false;                                // turns true e.g when activity pauses
    static boolean sGameStarted = false;                        // turns true after first tap on screen
    static boolean sGameOver = false;                           // turns true on bird collision with wall
    boolean mFlashEffect = false;                               // turns true when done with flash effect upon collision

    // constructor
    public GameView(Context context) {
        super(context);

        System.out.println("GameView constructor called");
        mContext = context;
        mSurfaceHolder = getHolder();
        mBackgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        setScorePaint();
        mCollsionSound = MediaPlayer.create(mContext, R.raw.collsion);
    }

    // called when View is visible and gives latest width and height of View
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        System.out.println("GameView.onSizeChanged() called");
        sViewWidth = w;
        sViewHeight = h;
        mBird = new Bird(mContext);        // initialize mBird
        mBird.startFlapThread();                                        // start the bird flapping loop in a thread
        mGround = new Ground(mContext);
        mWallObstacleManager = new WallObstacleManager(mContext,4, mBird.getInitialXpos());
        // resize click image
        mClickImage = decodeScaledBitmapFromResource(getResources(), R.drawable.click, 0, mBird.getImage().getHeight());
        mGameoverManager = new GameoverManager(mContext);
    }

    // the game loop
    @Override
    public void run() {

        Canvas canvas;
        mStartTime = System.nanoTime();

        while (mRunning) {

            if (!mGamePaused) {                                         // if game is paused, don't run the game loop
                if (mSurfaceHolder.getSurface().isValid()) {

                    // if game has started but not over yet
                    // check if bird collided with wall or ground
                    if(sGameStarted && !sGameOver) {
                        if (mWallObstacleManager.isColliding(mBird) || mGround.isColliding(mBird)) {
                            mCollsionSound.start();                     // play collsion sound
                            mBird.stopFlapping();
                            mGameoverManager.setScore(mScore);          // pass current score to GameoverManager so it can display
                            sGameOver = true;                           // so updates to Bird, Walls are stopped and GameoverManager can act
                        }
                    }

                    // do the updates
                    sDeltaTime = calculateDeltaTime();
                    // updates should pe placed after sDeltaTime & before mStartTime
                    mBird.fall();
                    mWallObstacleManager.update();
                    mGround.update();
                    // reset startTime for correct calculation of sDeltaTime on next iteration
                    mStartTime = System.nanoTime();

                    // do the drawing
                    canvas = mSurfaceHolder.lockCanvas();
                    canvas.drawBitmap(mBackgroundImage, null, new Rect(0, 0, sViewWidth, sViewHeight), null);
                    decorateBeforeGameStarted(canvas);
                    mWallObstacleManager.draw(canvas);
                    mGround.draw(canvas);
                    mBird.draw(canvas);
                    // draw score
                    mScore = mWallObstacleManager.getScore();
                    canvas.drawText(String.valueOf(mScore), (float) (sViewWidth * 0.45), (float) (sViewHeight * 0.15), mScorePaint);

                    if (sGameOver) {
                        if(!mFlashEffect) {
                            createFlashEffect(canvas);
                            mFlashEffect = true;
                        }
                        // check if bird is dead and only then allow GameoverManager to act
                        if(mBird.isDead()) {
                            mGameoverManager.update();
                            mGameoverManager.draw(canvas);
                        }
                    }
                    mSurfaceHolder.unlockCanvasAndPost(canvas);

                    //maintainFPS();
                }
            }
        }
        System.out.println("exiting Run() ");
    }

    // handle touch events i.e mBird should jump on tap
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {     // if tap on screen

            if(!sGameOver) {                            // if game not over yet
                sGameStarted = true;
                mBird.jump();
            }
            if(mGameoverManager.isAnimationDone()) {    // if gameover and GameoverManager done with animations etc
                restartGame();
            }
        }

        return true;
    }

    public void setScorePaint() {

        mScorePaint = new Paint();
        mScorePaint.setAlpha(100);
        mScorePaint.setTextSize(150);
    }

    public void decorateBeforeGameStarted(Canvas canvas) {

        // draw stuff that's to display before user first taps screen

        if(!sGameStarted) {     // proceed only if game have'nt started

            canvas.drawBitmap(mClickImage, mBird.getInitialXpos(), mBird.getInitialYpos()+200, null);
        }
    }

    public void maintainFPS(long startTime) {

        // sleep thread if work done before mTargetFrameTime

        long currFrameMillis = (System.nanoTime() - startTime) / 1000000;
        long waitTime = mTargetFrameTime - currFrameMillis;

        if(waitTime > 0) {

            try {
                Thread.sleep(waitTime);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        // show Frame time on Log
        // mainly for debugging purpose
        mTotalFrameTime += System.nanoTime()-startTime;
        mFrameCount++;
        if(mFrameCount == mTargetFPS) {
            double avgFPS = mFrameCount / (mTotalFrameTime/1000000000.0);
            mFrameCount = 0;
            mTotalFrameTime = 0;
            Log.i("FPS", String.valueOf(avgFPS));
            System.out.println("FPS: " + avgFPS);
        }

    }

    // returns delta time in millis w.r.t value of mStartTime
    private long calculateDeltaTime() {

        sDeltaTime = (long) ((System.nanoTime() - mStartTime)/1000000.0);       // sDeltaTime is time elapsed since mStartTime
        if(sDeltaTime > 33)                                                     // sDeltaTime not allowed to exceed 33ms
            return 33;
        else
            return sDeltaTime;
    }

    public static long getDeltaTime() {
        return sDeltaTime;
    }

    public void createFlashEffect(Canvas canvas) {

        // create and display a white Rect
        Rect rect = new Rect(0,0, sViewWidth, sViewHeight);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(rect, paint);
    }

    public void restartGame() {

        sGameStarted = false;
        mBird.reset();
        mWallObstacleManager.reset();
        mGameoverManager.reset();
        sGameOver = false;
        mFlashEffect = false;
    }

    public static Bitmap scaleBitmap(Bitmap image, int newWidth, int newHeight) {

        /*
         * scale image and return Bitmap
         * ratio of dimensions is preserved
         * either newWidth or newHeight must be zero
         */
        float aspectRatio;
        if(newHeight > 0) {
            // if newHeight has been provided, calculate new width
            aspectRatio = (float)image.getWidth() / image.getHeight();
            int w = (int)(aspectRatio * newHeight);
            return Bitmap.createScaledBitmap(image, w, newHeight, false);
        }
        else {
            // else newWidth has been provided, calculate new height
            aspectRatio = (float)image.getHeight() / image.getWidth();
            int h = (int)(aspectRatio * newWidth);
            return Bitmap.createScaledBitmap(image, newWidth, h, false);
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
                System.out.println("halfWidth: "+halfWidth);
                System.out.println("inSampleSize = "+inSampleSize);
                System.out.println("reqWidth: "+reqWidth);
            }
        }
        System.out.println("returned inSampleSize: "+inSampleSize);
        return inSampleSize;
    }

    /*
     * efficiently decodes a bitmap and scales down
     * either of reqWidth or reqHeight must be 0
     */
    public static Bitmap decodeScaledBitmapFromResource(Resources res, int resId,
                                                        int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // if only reqHeight has been provided, determine the proportionate reqWidth
        float aspectRatio;
        if(reqWidth <= 0) {
            aspectRatio = (float)options.outWidth / options.outHeight;
            reqWidth = (int)(aspectRatio * reqHeight);
        }

        // Calculate inSampleSize
        //options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        // now the bitmap can be efficiently scaled down to perfectly match reqWidth
        options.inJustDecodeBounds = false;
        options.inScaled = true;
        options.inDensity = options.outWidth;
        options.inTargetDensity = reqWidth;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    // begin game loop by starting a thread
    public void beginGameLoop() {

        mRunning = true;
        mThread = new Thread(this);
        mThread.start();

        System.out.println("CALLED THREAD.START()");
    }

    // end the game loop by stopping thread
    public void endGameLoop() {

        mBird.stopFlapThread();
        mRunning = false;
        try {
            mThread.join();

            System.out.println("DONE WITH GAMEVIEW.THREAD.JOIN()");
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
