package com.example.flappobird;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set Full Screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        gameView = new GameView(this);
        setContentView(gameView);

        // disable rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        gameView.beginGameLoop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        gameView.endGameLoop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        gameView.mGamePaused = false;
        System.out.println("MainActivity.onResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();

        gameView.mGamePaused = true;
        System.out.println("MainActivity.onPause() called");
    }

    @Override
    public void onBackPressed() {
        // do nothing
        // so back key is practically disables
    }
}
