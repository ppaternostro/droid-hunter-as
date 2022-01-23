package com.pasquasoft.android.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * A class that defines the attributes and behavior of animation.
 * 
 * @author Pat Paternostro
 * @version 1.0
 */
public class Animation
{
  private final int x;
  private final int y;
  private int index;

  /**
   * Constructs an <code>Animation</code> with the specified coordinates.
   * 
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public Animation(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  /**
   * Draws the current frame of the specified animation sequence.
   * 
   * @param canvas a <code>Canvas</code> object
   * @param animationSequence an array of <code>Bitmap</code> objects
   * @return <code>true</code> if end of sequence; <code>false</code> otherwise
   */
  public boolean draw(Canvas canvas, Bitmap[] animationSequence)
  {
    Bitmap bitmap = animationSequence[index++];

    int width = bitmap.getWidth();
    int height = bitmap.getHeight();

    /* Center the bitmap on the specified x and y coordinate */
    canvas.drawBitmap(bitmap, (float) (x - width / 2), (float) (y - height / 2), null);

    return index > (animationSequence.length - 1);
  }
}
