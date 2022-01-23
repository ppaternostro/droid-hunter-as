package com.pasquasoft.android.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.pasquasoft.android.R;
import com.pasquasoft.android.model.Animation;
import com.pasquasoft.android.model.Entity;
import com.pasquasoft.android.model.Level;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View implements Runnable
{
  private static final String PREFS = "DroidHunter";

  private static final int EASY = 0;
  private static final int MEDIUM = 1;
  private static final int HARD = 2;
  private static final int SINGLE = 0;
  private static final int CANNON = 0;
  private static final int PHASER = 1;
  public static final int EASY_FPS = 20;
  public static final int MEDIUM_FPS = 40;
  public static final int HARD_FPS = 60;
  public static final int RIDICULOUS_FPS = 80;

  private final int explosionId;
  private final int fireId;
  private int period;
  private int width;
  private int height;
  private int increment;

  private double attempts;
  private double hits;

  private boolean running;

  /* Performance tip: favor virtual over interface */
  private final ConcurrentLinkedQueue<Animation> animationQueue = new ConcurrentLinkedQueue<>();

  private ArrayList<Entity> droids;

  private Thread thread;

  private final Context context;

  private final Bitmap[] animationSequence = new Bitmap[4];

  private SoundPool soundPool;

  /**
   * Constructs a <code>CanvasView</code> object with the specified parameters.
   * 
   * @param context a <code>Context</code> object
   * @param as an <code>AttributeSet</code> object
   */
  public CanvasView(Context context, AttributeSet as)
  {
    super(context, as);

    AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

    soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(20).build();

    this.context = context;

    /* Retrieve game preferences */
    SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

    /* Retrieve mode */
    int mode = prefs.getInt(context.getString(R.string.prefs_game_mode_key), SINGLE);

    /* Retrieve weapon */
    int weapon = prefs.getInt(context.getString(R.string.prefs_weapon_sound_key), CANNON);

    if (mode == SINGLE)
    {
      /* Retrieve level */
      int difficulty = prefs.getInt(context.getString(R.string.prefs_difficulty_level_key), EASY);

      increment = (difficulty + 1) * 10;

      /* Set the period value */
      period = 1000 / (difficulty == EASY ? EASY_FPS
          : (difficulty == MEDIUM ? MEDIUM_FPS : (difficulty == HARD ? HARD_FPS : RIDICULOUS_FPS)));
    }

    explosionId = soundPool.load(context, R.raw.explosion, 1);
    fireId = soundPool.load(context, weapon == CANNON ? R.raw.cannon : (weapon == PHASER ? R.raw.phaser : R.raw.laser),
        1);

    loadAnimationSequence();
  }

  private void loadAnimationSequence()
  {
    Resources resources = context.getResources();

    animationSequence[0] = BitmapFactory.decodeResource(resources, R.drawable.explode1);
    animationSequence[1] = BitmapFactory.decodeResource(resources, R.drawable.explode2);
    animationSequence[2] = BitmapFactory.decodeResource(resources, R.drawable.explode3);
    animationSequence[3] = BitmapFactory.decodeResource(resources, R.drawable.explode4);
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);

    if (droids != null)
    {
      /* Draw the droids */
      for (Entity droid : droids)
      {
        droid.draw(canvas);
      }

      /* Animate the explosion */
      Iterator<Animation> iterator = animationQueue.iterator();

      while (iterator.hasNext())
      {
        Animation animation = iterator.next();

        boolean remove = animation.draw(canvas, animationSequence);

        if (remove)
        {
          iterator.remove();
        }
      }

      if (running)
      {
        synchronized (this)
        {
          notifyAll();
        }
      }
    }
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
  {
    super.onSizeChanged(width, height, oldWidth, oldHeight);

    this.width = width;
    this.height = height;
  }

  public boolean isRunning()
  {
    return running;
  }

  public void start(Level level, int imageResourceId)
  {
    increment = level.getIncrement();

    period = 1000 / level.getFramesPerSecond();

    start(level.getEntities(), imageResourceId);
  }

  public void start(int entities, int imageResourceId)
  {
    droids = new ArrayList<>(entities);

    /* Load the entities */
    for (int i = 0; i < entities; i++)
    {
      Entity entity = new Entity(imageResourceId, width, height, increment, context);

      droids.add(entity);
    }

    resume();
  }

  public void stop()
  {
    droids.clear();

    pause();

    drainQueue();
  }

  private void drainQueue()
  {
    new Thread() {
      public void run()
      {
        long timeDiff;
        long sleepTime;
        long beforeTime;

        while (animationQueue.size() != 0)
        {
          beforeTime = System.currentTimeMillis();

          postInvalidate();

          timeDiff = System.currentTimeMillis() - beforeTime;

          sleepTime = period - timeDiff;

          if (sleepTime <= 0)
          {
            sleepTime = 5;
          }

          try
          {
            /* Don't hog the CPU! Can't we all just get along? */
            Thread.sleep(sleepTime);
          }
          catch (InterruptedException ie)
          {
            /* Nothing to see here. Move along. */
          }
        }
      }
    }.start();
  }

  public void freeResources()
  {
    for (Bitmap bitmap : animationSequence)
    {
      bitmap.recycle();
    }

    soundPool.release();

    soundPool = null;
  }

  public int getHitPercentage()
  {
    return (int) (hits != 0 ? (hits / attempts) * 100 : 0);
  }

  public double getHits()
  {
    return hits;
  }

  public void setHits(double hits)
  {
    this.hits = hits;
  }

  public double getAttempts()
  {
    return attempts;
  }

  public void setAttempts(double attempts)
  {
    this.attempts = attempts;
  }

  public void pause()
  {
    if (thread != null)
    {
      thread.interrupt();
    }

    thread = null;

    running = false;
  }

  public void resume()
  {
    if (thread == null)
    {
      running = true;

      thread = new Thread(this);

      thread.start();
    }
  }

  @Override
  public synchronized void run()
  {
    long timeDiff;
    long sleepTime;
    long beforeTime;

    while (running)
    {
      beforeTime = System.currentTimeMillis();

      moveDroids();

      postInvalidate();

      timeDiff = System.currentTimeMillis() - beforeTime;

      sleepTime = period - timeDiff;

      if (sleepTime <= 0)
      {
        sleepTime = 5;
      }

      try
      {
        /* Synchronize threads */
        wait();

        /* Don't hog the CPU! Can't we all just get along? */
        Thread.sleep(sleepTime);
      }
      catch (InterruptedException ie)
      {
        return;
      }
    }
  }

  private void moveDroids()
  {
    for (Entity droid : droids)
    {
      droid.move();
    }
  }

  public int getDroidCount()
  {
    return droids.size();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    if (running)
    {
      soundPool.play(fireId, 1.0f, 1.0f, 1, 0, 1.0f);

      attempts++;

      /* Remove droid if hit */
      for (Entity droid : droids)
      {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (droid.isHit(x, y))
        {
          droids.remove(droid);

          hits++;

          /* Initiate the explosion sound */
          soundPool.play(explosionId, 1.0f, 1.0f, 1, 0, 1.0f);

          /*
           * Create an animation object, set its coordinates to the 'hit'
           * coordinates, and add to the queue.
           */
          Animation animation = new Animation(x, y);
          animationQueue.add(animation);

          break;
        }
      }
    }

    return false;
  }
}
