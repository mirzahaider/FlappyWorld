package com.example.flappobird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;

import java.util.Random;

/*
 * WallObstacleManager defines a Wall, keeps an array of them and manages their movement etc
 */

public class WallObstacleManager {

    private class Wall {

        /*
         * Wall contains Bottom, Left coordinates of the top wall
         * also the vertical gap between top and bottom wall
         * this much info is sufficient for calculating coordinates of bottom wall
         * also has a num which is one greater for every next wall
         */
        int num;
        double verticalGap;
        float mBottom_TopWall;
        float mLeft_TopWall;
    }

    private int mFrameWidth = GameView.sViewWidth;
    private int mFrameHeight = GameView.sViewHeight;
    private Context mContext;

    private Bitmap mImage;                              // regular wall img
    private Bitmap mImage_gold;                         // gold wall img
    private Wall[] mWalls;
    private int mWallsArrSize;
    private int mCurrentWallIndex = 0;                  // index of the wall to be crossed by bird
    private int mLargestWallNum;                        // records the largest wall.num
    private int mGoldWallNum = 50;                      // num of gold wall -- after every this num, gold wall will appear
    private int mVertGapChangeNum = 10;                  // wall number which starts the new mVerticalGap

    private float mInitialBirdXpos;                     // the initial X coordinate of the bird
    private int mScore;                                 // bird's total score by crossing the walls

    // following block of variables expressed as percentage of frame height
    private double mVertGap1 = 30/100.0 * mFrameHeight;                 // option 1 as the verticalGap between walls
    private double mVertGap2 = 24/100.0 * mFrameHeight;
    private double mOffsetMin = 1/100.0 * mFrameHeight;                 // Wall.mBottom_TopWall can range between mOffsetMin & OffsetMax
    private double mOffsetMax = Ground.sTop;                            // Wall.mBottom_TopWall shouldn't go below ground ideally

    // following block expressed as percentage of frame width
    private double mHorizontalGap = 53/100.0 * mFrameWidth;
    private float mWallVelocity = Ground.sVelocity;                     // Ground and walls have the same speed

    private Random mRandom = new Random();
    MediaPlayer mObstacleClearedSound;

    public WallObstacleManager(Context context, int wallArrSize, float initialBirdXpos) {

        mContext = context;
        mWallsArrSize = wallArrSize;
        mWalls = new Wall[mWallsArrSize];
        for(int i=0; i<mWalls.length; i++) {
            mWalls[i] = new Wall();
            mWalls[i].verticalGap = mVertGap1;
        }
        mInitialBirdXpos = initialBirdXpos;

        System.out.println("*** Wall.java ***");
        System.out.println("Wall Velocity = "+mWallVelocity);
        System.out.println("mOffsetMin = "+mOffsetMin);
        System.out.println("mOffsetMax = "+mOffsetMax);

        mImage = GameView.decodeScaledBitmapFromResource(mContext.getResources(), R.drawable.wall, (int)(mFrameWidth/6.5), 0);
        mImage_gold = GameView.decodeScaledBitmapFromResource(mContext.getResources(), R.drawable.wall_gold, (int)(mFrameWidth/6.5), 0);

        setUpWalls();
        resetScore();

        mObstacleClearedSound = MediaPlayer.create(mContext, R.raw.obstacle_cleared);
    }

    public void setUpWalls() {

        mCurrentWallIndex = 0;
        mLargestWallNum = mWallsArrSize;

        // set num, verticalGap and coordinates of each wall
        for(int i=0; i<mWalls.length; i++) {
            // set num
            mWalls[i].num = i+1;

            // check which vertical gap needs to apply (since we are setting up walls before gameplay)
            if(mWalls[i].num >= mVertGapChangeNum) {
                mWalls[i].verticalGap = mVertGap2;
            }
            else {
                mWalls[i].verticalGap = mVertGap1;
            }

            // for mBottom_TopWall, choose a random value between mOffsetMin and mOffsetMax
            mWalls[i].mBottom_TopWall = (float) mOffsetMin + mRandom.nextInt((int) (mOffsetMax - mOffsetMin - mWalls[i].verticalGap));
            mWalls[i].mLeft_TopWall = (float) (mFrameWidth + (i * mHorizontalGap));
        }
    }

    // update Xpos, num of each wall and the score
    public void update() {

        // walls should update only if game has started but is not over yet
        if(GameView.sGameStarted && !GameView.sGameOver) {

            // update Xpos of each wall
            for (int i = 0; i < mWalls.length; i++) {

                mWalls[i].mLeft_TopWall -= mWallVelocity * GameView.getDeltaTime();

                // if a wall is going beyond screen, update its num and reset its coordinates
                if (mWalls[i].mLeft_TopWall + mImage.getWidth() < 0) {

                    // set num to one greater than mLargestWallNum
                    // then update mLargestWallNum
                    mWalls[i].num = mLargestWallNum+1;
                    mLargestWallNum++;

                    // check if vertical gap needs to change
                    if(mWalls[i].num >= mVertGapChangeNum) {
                        mWalls[i].verticalGap = mVertGap2;
                    }

                    // update coordinates
                    mWalls[i].mBottom_TopWall = (float) mOffsetMin + mRandom.nextInt((int) (mOffsetMax - mOffsetMin - mWalls[i].verticalGap));
                    mWalls[i].mLeft_TopWall = (float) (mWalls.length * mHorizontalGap);
                }
            }
            // if the bird has crossed mCurrentWallIndex
            if (mInitialBirdXpos > (mWalls[mCurrentWallIndex].mLeft_TopWall + mImage.getWidth())) {

                mScore++;                                   // increment mSore
                mObstacleClearedSound.start();              // play sound
                mCurrentWallIndex++;                        // update mCurrentWallIndex
                if (mCurrentWallIndex >= mWallsArrSize)
                    mCurrentWallIndex = 0;
            }
        }
    }

    public void draw(Canvas canvas) {

        // for every wall draw mImage twice
        // once for the top wall and another for bottom wall
        for(int i=0; i<mWalls.length; i++) {

            // draw only if the wall is in the frame
            if(mWalls[i].mLeft_TopWall <= mFrameWidth) {

                // check if gold wall is to appear or regular
                if(mWalls[i].num % mGoldWallNum == 0) {
                    canvas.drawBitmap(mImage_gold, mWalls[i].mLeft_TopWall, mWalls[i].mBottom_TopWall - mImage.getHeight(), null);
                    canvas.drawBitmap(mImage_gold, mWalls[i].mLeft_TopWall, mWalls[i].mBottom_TopWall + (float) mWalls[i].verticalGap, null);
                }
                else {
                    canvas.drawBitmap(mImage, mWalls[i].mLeft_TopWall, mWalls[i].mBottom_TopWall - mImage.getHeight(), null);
                    canvas.drawBitmap(mImage, mWalls[i].mLeft_TopWall, mWalls[i].mBottom_TopWall + (float) mWalls[i].verticalGap, null);
                }
            }
        }
    }

    // check if the Bird is colliding with mCurrentWallIndex
    // if so return true
    public boolean isColliding(Bird bird) {

        Bitmap birdImage = bird.getImage();
        // create Rect containing bird image
        Rect birdRect = new Rect((int)(bird.getXpos()), (int)(bird.getYpos()), (int)(bird.getXpos()+birdImage.getWidth()), (int)(bird.getYpos()+birdImage.getHeight()));

        Wall w = mWalls[mCurrentWallIndex];

        // create Rect containing top wall
        Rect topWallRect = new Rect((int)(w.mLeft_TopWall), (int)(w.mBottom_TopWall-mImage.getHeight()), (int)(w.mLeft_TopWall+mImage.getWidth()), (int)(w.mBottom_TopWall));

        if (Rect.intersects(topWallRect, birdRect)) {
            // birdRect and wallRect do intersect but we have to be pixel perfect
            return isPixelColliding(birdImage, birdRect, topWallRect);
        }
        else {
            // create Rect containing bottom wall
            Rect bottomWallRect = new Rect((int)(w.mLeft_TopWall), (int)(w.mBottom_TopWall+w.verticalGap), (int)(w.mLeft_TopWall+mImage.getWidth()), (int)(w.mBottom_TopWall+w.verticalGap+mImage.getHeight()));
            if(Rect.intersects(bottomWallRect, birdRect)) {
                return isPixelColliding(birdImage, birdRect, bottomWallRect);
            }
        }
        return false;
    }

    // return the intersection of 2 Rects
    private Rect getCollisionArea(Rect rect1, Rect rect2) {

        int left = Math.max(rect1.left, rect2.left);
        int top = Math.max(rect1.top, rect2.top);
        int right = Math.min(rect1.right, rect2.right);
        int bottom = Math.min(rect1.bottom, rect2.bottom);

        return new Rect(left, top, right, bottom);
    }

    private boolean isPixelColliding(Bitmap birdImage, Rect birdRect, Rect wallRect) {
        /*
         * birdRect and wallRect intersect
         * create a Rect of collision area and iterate through each pixel in it
         * if a pixel in collision area is not transparent in both birdRect as well as wallRect, both images collide
         */
        Rect collisionArea = getCollisionArea(birdRect, wallRect);
        for(int y=collisionArea.top; y<collisionArea.bottom; y++) {

            for(int x=collisionArea.left; x<collisionArea.right; x++) {

                int pixel1 = birdImage.getPixel(x-birdRect.left, y-birdRect.top);
                int pixel2 = mImage.getPixel(x-wallRect.left, y-wallRect.top);

                if(pixel1 != Color.TRANSPARENT && pixel2 != Color.TRANSPARENT) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getScore() {
        return mScore;
    }

    public void resetScore() {
        mScore = 0;
    }

    public void reset() {

        setUpWalls();
        resetScore();
    }
}