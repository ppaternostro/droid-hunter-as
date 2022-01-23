package com.pasquasoft.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.pasquasoft.android.util.Util;
import com.pasquasoft.android.view.CanvasView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DroidHunter extends Activity
{
  private static final String PREFS = "DroidHunter";

  /* Zero offset, positional indices - see menu.xml */
  private static final int RESUME = 0;
  private static final int PAUSE = 1;
  private static final int SOUND_OFF = 3;
  private static final int SOUND_ON = 4;

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

  private Menu menu;

  private DroidHunterApplication application;

  private int mode;
  private int model;

  private long gameTimeLimit;

  private boolean timedOut;
  private boolean paused;
  private boolean muted;
  private boolean keyCodeBack;

  private final Runnable statusAreaRunnable = this::updateStatusArea;
  private final Runnable gameTimerRunnable = this::displayMessage;
  private final OnClickListener dialogListener = (dialog, which) -> finish();

  @SuppressLint("ShowToast")
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    /* Make full screen by removing title and action bar */
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    toast = Toast.makeText(this, null, Toast.LENGTH_SHORT);

    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);

    application = (DroidHunterApplication) getApplication();

    gameTimerHandler = new Handler();
    statusAreaHandler = new Handler();

    prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);

    mode = prefs.getInt(getString(R.string.prefs_game_mode_key), SINGLE);
    muted = prefs.getInt(getString(R.string.prefs_sound_mode_key), UNMUTE) == MUTE;
    model = prefs.getInt(getString(R.string.prefs_droid_model_key), IRON);

    setContentView(R.layout.droid);

    timer = findViewById(R.id.timer);
    count = findViewById(R.id.count);
    ratio = findViewById(R.id.ratio);
    canvasView = findViewById(R.id.canvasView);

    startGame();
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
      /* Set mute off */
      ((AudioManager) getSystemService(AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_MUSIC, false);
    }
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
      /* Set mute on */
      ((AudioManager) getSystemService(AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_MUSIC, true);
    }
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();

    if (!isFinishing())
    {
      stopGame();
    }
    else
    {
      application.resetLevel();
    }

    canvasView.freeResources();
  }

  @Override
  protected void onRestart()
  {
    super.onRestart();

    if (!paused)
    {
      resumeGame();
    }
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    if (!isFinishing())
    {
      pauseGame();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();

    inflater.inflate(R.menu.menu, menu);

    this.menu = menu;

    if (muted)
    {
      menu.getItem(SOUND_OFF).setVisible(false);
      menu.getItem(SOUND_ON).setVisible(true);
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    boolean consume = false;

    switch (item.getItemId())
    {
      case R.id.stop:
        stopGame();
        finish();
        consume = true;
        break;
      case R.id.pause:
        pauseGame();
        item.setVisible(false);
        menu.getItem(RESUME).setVisible(true);
        paused = true;
        consume = true;
        break;
      case R.id.resume:
        resumeGame();
        item.setVisible(false);
        menu.getItem(PAUSE).setVisible(true);
        paused = false;
        consume = true;
        break;
      case R.id.sound_off:
        ((AudioManager) getSystemService(AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_MUSIC, true);
        item.setVisible(false);
        menu.getItem(SOUND_ON).setVisible(true);
        muted = true;
        consume = true;
        break;
      case R.id.sound_on:
        ((AudioManager) getSystemService(AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_MUSIC, false);
        item.setVisible(false);
        menu.getItem(SOUND_OFF).setVisible(true);
        muted = false;
        consume = true;
        break;
    }

    return consume;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    if (keyCode == KeyEvent.KEYCODE_BACK)
    {
      keyCodeBack = true;

      /*
       * Normally it's unnecessary to cancel as toast will disappear after
       * appropriate duration. However if the user clicks the back button while
       * the toast is still visible this call will close the view immediately
       * versus waiting for the duration to complete.
       */
      toast.cancel();

      if (canvasView.isRunning())
      {
        stopGame();
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * Starts the game play.
   */
  private void startGame()
  {
    toast.setText(mode == SINGLE ? getString(R.string.label_ready) : application.getCurrentLevel().getName());

    toast.show();

    new Handler().postDelayed(() -> {
      /*
       * This code is scheduled to run after a two second delay. In the meantime
       * the user may have hit the back button. In that situation we don't start
       * the game since the user's intent is to maneuver back to the main
       * screen.
       */
      if (!keyCodeBack)
      {
        if (mode == SINGLE)
        {
          canvasView.start(gameDroids(), application.getDroids()[model].getImageResourceId());
        }
        else
        {
          canvasView.start(application.getCurrentLevel(), application.getDroids()[model].getImageResourceId());
        }

        statusTimer = new Timer();
        statusTimer.scheduleAtFixedRate(new StatusTask(), 0, 1000);

        gameTimer = new Timer();
        gameTimer.schedule(new GameTask(), new Date(System.currentTimeMillis() + (gameTimeLimit = gameTimeLimit())));
      }
    }, 2000);
  }

  /**
   * Stops the game play.
   */
  private void stopGame()
  {
    gameTimer.cancel();
    statusTimer.cancel();

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
    gameTimer.cancel();
    statusTimer.cancel();

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
    statusTimer.scheduleAtFixedRate(new StatusTask(), 0, 1000);

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
        : application.getCurrentLevel().getTimeLimit();

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

      if (mode == SINGLE || application.getCurrentLevel().getName().equals(getString(R.string.label_level_20)))
      {
        Util.messageDialog(DroidHunter.this, getString(R.string.droid_hunter), getString(R.string.message_success),
            dialogListener);
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
    String countReset = getString(R.string.label_droids) + " " + canvasView.getDroidCount();
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

    Util.messageDialog(DroidHunter.this, getString(R.string.droid_hunter), getString(R.string.message_failed),
        dialogListener);
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

      /* The above comment explains it all. Nuff said! */
      statusAreaHandler.post(statusAreaRunnable);
    }
  }
}
