package com.pasquasoft.android;

import static com.pasquasoft.android.view.CanvasView.EASY_FPS;
import static com.pasquasoft.android.view.CanvasView.HARD_FPS;
import static com.pasquasoft.android.view.CanvasView.MEDIUM_FPS;
import static com.pasquasoft.android.view.CanvasView.RIDICULOUS_FPS;

import android.app.Application;

import com.pasquasoft.android.model.Level;

import java.util.HashMap;

public class DroidHunterApplication extends Application
{
  private static HashMap<Integer, Level> levels = new HashMap<>(20);

  private static final Droid[] droids = new Droid[16];

  private int currentLevel = 1;

  @Override
  public void onCreate()
  {
    super.onCreate();

    levels.put(1, new Level(5, 5, EASY_FPS, getString(R.string.label_level_1), "00:05"));
    levels.put(2, new Level(10, 5, EASY_FPS, getString(R.string.label_level_2), "00:10"));
    levels.put(3, new Level(15, 5, EASY_FPS, getString(R.string.label_level_3), "00:15"));
    levels.put(4, new Level(20, 5, EASY_FPS, getString(R.string.label_level_4), "00:20"));
    levels.put(5, new Level(25, 5, EASY_FPS, getString(R.string.label_level_5), "00:25"));
    levels.put(6, new Level(30, 10, MEDIUM_FPS, getString(R.string.label_level_6), "00:35"));
    levels.put(7, new Level(35, 10, MEDIUM_FPS, getString(R.string.label_level_7), "00:40"));
    levels.put(8, new Level(40, 10, MEDIUM_FPS, getString(R.string.label_level_8), "00:45"));
    levels.put(9, new Level(45, 10, MEDIUM_FPS, getString(R.string.label_level_9), "00:50"));
    levels.put(10, new Level(50, 10, MEDIUM_FPS, getString(R.string.label_level_10), "00:55"));
    levels.put(11, new Level(55, 15, HARD_FPS, getString(R.string.label_level_11), "01:05"));
    levels.put(12, new Level(60, 15, HARD_FPS, getString(R.string.label_level_12), "01:10"));
    levels.put(13, new Level(65, 15, HARD_FPS, getString(R.string.label_level_13), "01:15"));
    levels.put(14, new Level(70, 15, HARD_FPS, getString(R.string.label_level_14), "01:20"));
    levels.put(15, new Level(75, 20, HARD_FPS, getString(R.string.label_level_15), "01:25"));
    levels.put(16, new Level(80, 20, HARD_FPS, getString(R.string.label_level_16), "01:30"));
    levels.put(17, new Level(85, 20, HARD_FPS, getString(R.string.label_level_17), "01:35"));
    levels.put(18, new Level(90, 20, HARD_FPS, getString(R.string.label_level_18), "01:40"));
    levels.put(19, new Level(95, 30, RIDICULOUS_FPS, getString(R.string.label_level_19), "01:45"));
    levels.put(20, new Level(100, 30, RIDICULOUS_FPS, getString(R.string.label_level_20), "02:00"));

    droids[0] = new Droid(R.string.droid_android, R.drawable.android);
    droids[1] = new Droid(R.string.droid_armed, R.drawable.armed);
    droids[2] = new Droid(R.string.droid_atomic, R.drawable.atomic);
    droids[3] = new Droid(R.string.droid_blue, R.drawable.blue);
    droids[4] = new Droid(R.string.droid_classic, R.drawable.classic);
    droids[5] = new Droid(R.string.droid_crawler, R.drawable.crawler);
    droids[6] = new Droid(R.string.droid_dancing, R.drawable.dancing);
    droids[7] = new Droid(R.string.droid_dog, R.drawable.dog);
    droids[8] = new Droid(R.string.droid_flying, R.drawable.flying);
    droids[9] = new Droid(R.string.droid_happy, R.drawable.happy);
    droids[10] = new Droid(R.string.droid_insect, R.drawable.insect);
    droids[11] = new Droid(R.string.droid_iron, R.drawable.iron);
    droids[12] = new Droid(R.string.droid_little, R.drawable.little);
    droids[13] = new Droid(R.string.droid_military, R.drawable.military);
    droids[14] = new Droid(R.string.droid_paranoid, R.drawable.paranoid);
    droids[15] = new Droid(R.string.droid_sad, R.drawable.sad);
  }

  @Override
  public void onTerminate()
  {
    super.onTerminate();

    levels = null;
  }

  public Level getCurrentLevel()
  {
    return levels.get(currentLevel);
  }

  public void nextLevel()
  {
    ++currentLevel;
  }

  public void resetLevel()
  {
    currentLevel = 1;
  }

  public Droid[] getDroids()
  {
    return droids;
  }
}
