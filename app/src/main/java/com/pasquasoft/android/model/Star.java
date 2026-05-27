package com.pasquasoft.android.model;

import java.util.Random;

import com.pasquasoft.android.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Star
{
  private static final Random random = new Random();
  private static final int SPEED_DIVISOR = 3;

  private float x;
  private float y;
  private float xIncrement;
  private float yIncrement;
  private int boundsWidth;
  private int boundsHeight;
  private final Bitmap bitmap;
  private final int starSize;
  private final Paint alphaPaint;
  private float rotation;
  private float pulse;
  private boolean active;

  public Star(int boundsWidth, int boundsHeight, int droidIncrement, Context context)
  {
    this.boundsWidth = boundsWidth;
    this.boundsHeight = boundsHeight;

    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.star);
    starSize = Math.max(bitmap.getWidth(), bitmap.getHeight());

    int edge = random.nextInt(4);

    switch (edge)
    {
      case 0 -> {
        x = random.nextInt(boundsWidth);
        y = -starSize;
      }
      case 1 -> {
        x = boundsWidth + starSize;
        y = random.nextInt(boundsHeight);
      }
      case 2 -> {
        x = random.nextInt(boundsWidth);
        y = boundsHeight + starSize;
      }
      default -> {
        x = -starSize;
        y = random.nextInt(boundsHeight);
      }
    }

    int inc = Math.max(1, droidIncrement / SPEED_DIVISOR);

    float dx = boundsWidth / 2f - x;
    float dy = boundsHeight / 2f - y;
    float len = (float) Math.sqrt(dx * dx + dy * dy);

    if (len > 0)
    {
      xIncrement = (dx / len) * inc;
      yIncrement = (dy / len) * inc;
    }
    else
    {
      xIncrement = yIncrement = inc;
    }

    alphaPaint = new Paint();
    active = true;
  }

  public void move()
  {
    if (!active)
    {
      return;
    }

    x += xIncrement;
    y += yIncrement;
    rotation += 6f;

    if (rotation >= 360f)
    {
      rotation -= 360f;
    }

    pulse += 0.08f;

    if (pulse >= Math.PI * 2)
    {
      pulse -= (float) (Math.PI * 2);
    }

    if (x < -starSize * 2 || x > boundsWidth + starSize * 2 || y < -starSize * 2 || y > boundsHeight + starSize * 2)
    {
      active = false;
    }
  }

  public void draw(Canvas canvas)
  {
    if (!active)
    {
      return;
    }

    float alpha = 0.3f + 0.15f * (float) Math.sin(pulse);
    alphaPaint.setAlpha((int) (alpha * 255));

    canvas.save();
    canvas.translate(x + starSize / 2f, y + starSize / 2f);
    canvas.rotate(rotation);
    canvas.drawBitmap(bitmap, -starSize / 2f, -starSize / 2f, alphaPaint);
    canvas.restore();
  }

  public boolean isHit(float touchX, float touchY)
  {
    return active && touchX >= x && touchX <= x + starSize && touchY >= y && touchY <= y + starSize;
  }

  public boolean isActive()
  {
    return active;
  }

  public void deactivate()
  {
    active = false;
  }

  public void reorient(int newWidth, int newHeight)
  {
    if (boundsWidth == 0 || boundsHeight == 0)
    {
      return;
    }

    x = x * newWidth / boundsWidth;
    y = y * newHeight / boundsHeight;

    boundsWidth = newWidth;
    boundsHeight = newHeight;

    float dx = boundsWidth / 2f - x;
    float dy = boundsHeight / 2f - y;
    float len = (float) Math.sqrt(dx * dx + dy * dy);

    if (len > 0)
    {
      float speed = (float) Math.sqrt(xIncrement * xIncrement + yIncrement * yIncrement);
      xIncrement = (dx / len) * speed;
      yIncrement = (dy / len) * speed;
    }
  }
}
