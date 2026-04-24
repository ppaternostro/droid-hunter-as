package com.pasquasoft.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import com.pasquasoft.android.util.Util;
import com.pasquasoft.android.view.CanvasView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;

public class DroidHunter extends ComponentActivity
{
  private static final String PREFS = "DroidHunter";

  private static final int SINGLE = 0;
  private static final int IRON = 10;
  private static final int UNMUTE = 0;
  private static final int MUTE = 1;

  private CanvasView canvasView;

  private TextView timer;
  private TextView count;
  private TextView ratio;

  private SharedPreferences prefs;

  private Handler gameTimerHandler;
  private Handler statusAreaHandler;

  private Timer statusTimer;
  private Timer gameTimer;

  private Toast toast;

  private DroidHunterApplication application;

  private int mode;
  private int model;

  private long gameTimeLimit;

  private boolean timedOut;
  private boolean muted;
  private boolean keyCodeBack;

  private final Runnable statusAreaRunnable = this::updateStatusArea;
  private final Runnable gameTimerRunnable = this::displayMessage;

  private final List<AlertDialog> alerts = new ArrayList<>();

  private final OnClickListener dialogListener = (dialog, which) -> {
    dialog.dismiss();
    finish();
  };

  @Override
  public void onConfigurationChanged(@NonNull Configuration config)
  {
    super.onConfigurationChanged(config);

    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE
        || config.orientation == Configuration.ORIENTATION_PORTRAIT)
    {
      new Handler(Looper.getMainLooper()).postDelayed(() -> canvasView.reorientDroids(), 100);
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();

    /*
     * Unmute a muted stream on pause event.
     */
    if (muted)
    {
      /*
       * Delay the unmute in case any sound is still playing.
       */
      new Handler(Looper.getMainLooper()).postDelayed(this::unmute, 2000);
    }

    if (isFinishing())
    {
      // Prevent window leaks
      alerts.forEach(dialog -> {
        if (dialog.isShowing())
        {
          dialog.dismiss();
        }
      });

      if (!canvasView.isStopped())
      {
        stopGame();
      }

      application.resetLevel();
      canvasView.freeResources();
    }
    else
    {
      pauseGame();
    }
  }

  @SuppressLint("ShowToast")
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        keyCodeBack = true;

        /*
         * Normally it's unnecessary to cancel as toast will disappear after
         * appropriate duration. However, if the user clicks the back button while
         * the toast is still visible this call will close the view immediately
         * versus waiting for the duration to complete.
         */
        toast.cancel();

        if (canvasView.isRunning())
        {
          stopGame();
        }

        // "Super" equivalent: disable this callback and re-trigger
        setEnabled(false);
        getOnBackPressedDispatcher().onBackPressed();

        // Re-enable to catch the next back press
        setEnabled(true);
      }
    };

    getOnBackPressedDispatcher().addCallback(this,  callback);

    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    /* Make full screen by removing action bar */
    Objects.requireNonNull(getActionBar()).hide();

    toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);

    application = (DroidHunterApplication) getApplication();

    gameTimerHandler = new Handler(Looper.getMainLooper());
    statusAreaHandler = new Handler(Looper.getMainLooper());

    prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

    mode = prefs.getInt(getString(R.string.prefs_game_mode_key), SINGLE);
    muted = prefs.getInt(getString(R.string.prefs_sound_mode_key), UNMUTE) == MUTE;
    model = prefs.getInt(getString(R.string.prefs_droid_model_key), IRON);

    setContentView(R.layout.droid);

    timer = findViewById(R.id.timer);
    count = findViewById(R.id.count);
    ratio = findViewById(R.id.ratio);
    canvasView = findViewById(R.id.canvasView);
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    /*
     * If the game is in a muted state we mute it since mute was removed on
     * pause event.
     */
    if (muted)
    {
      /* Mute sound */
      mute();
    }

    if (canvasView.isPaused())
    {
      resumeGame();
    }
    else
    {
      startGame();
    }
  }

  /**
   * Starts the game play.
   */
  private void startGame()
  {
    toast.setText(mode == SINGLE ? getString(R.string.label_ready) : application.getCurrentLevel().name());

    toast.show();

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      /*
       * This code is scheduled to run after a delay. In the meantime the user
       * may have hit the back button. In that situation we don't start the game
       * since the user's intent is to maneuver back to the main screen.
       */
      if (!keyCodeBack)
      {
        if (mode == SINGLE)
        {
          canvasView.start(gameDroids(), application.getDroids()[model].imageResourceId());
        }
        else
        {
          canvasView.start(application.getCurrentLevel(), application.getDroids()[model].imageResourceId());
        }

        statusTimer = new Timer();
        statusTimer.schedule(new StatusTask(), 0, 1000);

        gameTimer = new Timer();
        gameTimer.schedule(new GameTask(), new Date(System.currentTimeMillis() + (gameTimeLimit = gameTimeLimit())));
      }
    }, 200);
  }

  /**
   * Stops the game play.
   */
  private void stopGame()
  {
    if (gameTimer != null)
    {
      gameTimer.cancel();
    }

    if (statusTimer != null)
    {
      statusTimer.cancel();
    }

    gameTimerHandler.removeCallbacks(gameTimerRunnable);
    statusAreaHandler.removeCallbacks(statusAreaRunnable);

    canvasView.stop();

    if (!timedOut)
    {
      String timerReset = getString(R.string.label_time) + " " + getString(R.string.time_reset);
      String countReset = getString(R.string.label_droids) + " 0";

      /* Reset the status area */
      timer.setText(timerReset);
      count.setText(countReset);
    }
  }

  /**
   * Pause the game play.
   */
  private void pauseGame()
  {
    /* Cancel timers */
    if (gameTimer != null)
    {
      gameTimer.cancel();
    }

    if (statusTimer != null)
    {
      statusTimer.cancel();
    }

    /* Pause the game */
    canvasView.pause();
  }

  /**
   * Resume the game play.
   */
  private void resumeGame()
  {
    /* Resume the game */
    canvasView.resume();

    /* Canceled timers cannot schedule new tasks */
    statusTimer = new java.util.Timer();
    statusTimer.schedule(new StatusTask(), 0, 1000);

    gameTimer = new java.util.Timer();
    gameTimer.schedule(new GameTask(), new Date(System.currentTimeMillis() + gameTimeLimit));
  }

  /**
   * Retrieve the number of droids.
   * 
   * @return the number of droids
   */
  private int gameDroids()
  {
    return prefs.getInt(getString(R.string.prefs_droid_number_key), 10);
  }

  /**
   * Retrieve the game's time limit.
   * 
   * @return the game's time limit
   */
  private long gameTimeLimit()
  {
    String timeLimit = mode == SINGLE
        ? prefs.getString(getString(R.string.prefs_time_limit_key), getString(R.string.prefs_time_limit_default))
        : application.getCurrentLevel().timeLimit();

    /* Parse the string */
    String[] parts = timeLimit.split(getString(R.string.time_separator));

    /* Convert parts to milliseconds */
    int minutes = Integer.parseInt(parts[0]) * 60 * 1000;
    int seconds = Integer.parseInt(parts[1]) * 1000;

    return minutes + seconds;
  }

  /**
   * Update the game's status area.
   */
  private void updateStatusArea()
  {
    if (gameTimeLimit > 0 && canvasView.getDroidCount() == 0)
    {
      stopGame();

      if (mode == SINGLE || application.getCurrentLevel().name().equals(getString(R.string.label_level_20)))
      {
        alerts.add(Util.messageDialog(DroidHunter.this, getString(R.string.droid_hunter),
            getString(R.string.message_success), dialogListener));
      }
      else
      {
        application.nextLevel();

        startGame();
      }
    }

    long minutes = gameTimeLimit / 1000 / 60;
    long seconds = gameTimeLimit / 1000 % 60;

    String timerReset = getString(R.string.label_time) + " " + (minutes < 10 ? "0" + minutes : "" + minutes)
        + getString(R.string.time_separator) + (seconds < 0 ? "00" : (seconds < 10 ? "0" + seconds : "" + seconds));
    String countReset = getString(R.string.label_droids) + ": " + canvasView.getDroidCount();
    String ratioReset = getString(R.string.label_hit) + " " + canvasView.getHitPercentage() + "%";

    /* Update the status area */
    timer.setText(timerReset);
    count.setText(countReset);
    ratio.setText(ratioReset);
  }

  private void displayMessage()
  {
    timedOut = true;

    stopGame();

    alerts.add(Util.messageDialog(DroidHunter.this, getString(R.string.droid_hunter),
        getString(R.string.message_failed), dialogListener));
  }

  private void unmute()
  {
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    // Get max volume
    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    // Set volume to half max
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0);
  }

  private void mute()
  {
    ((AudioManager) getSystemService(AUDIO_SERVICE)).setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
  }

  private class GameTask extends TimerTask
  {
    public void run()
    {
      /*
       * UI related code won't work (and may produce anomalous results) when
       * executed in a worker thread. The UI must be updated on the UI (main)
       * thread. Handlers are bound to the creating thread/message queue (in
       * this case the UI thread since it's created in an activity).
       */
      gameTimerHandler.post(gameTimerRunnable);
    }
  }

  private class StatusTask extends TimerTask
  {
    public void run()
    {
      gameTimeLimit -= 1000;

      /* The above comment explains it all. Enough said! */
      statusAreaHandler.post(statusAreaRunnable);
    }
  }
}
