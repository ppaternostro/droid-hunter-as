package com.pasquasoft.android.sprite;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Sprite
{
  private final Bitmap bitmap;

  public Sprite(Bitmap bitmap)
  {
    this.bitmap = bitmap;
  }

  public void draw(Canvas canvas, int x, int y)
  {
    canvas.drawBitmap(bitmap, x, y, null);
  }

  public int getWidth()
  {
    return bitmap.getWidth();
  }

  public int getHeight()
  {
    return bitmap.getHeight();
  }
}
