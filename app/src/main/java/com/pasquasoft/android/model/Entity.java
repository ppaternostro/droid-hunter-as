package com.pasquasoft.android.model;

import java.util.Random;

import com.pasquasoft.android.sprite.Sprite;
import com.pasquasoft.android.sprite.SpriteCache;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Entity
{
  private static final Random randomGenerator = new Random();

  private final int increment;
  private final int boundsWidth;
  private final int boundsHeight;
  private final int spriteWidth;
  private final int spriteHeight;

  private int xIncrement;
  private int yIncrement;

  private final Sprite sprite;

  private final Rect rectangle;

  public Entity(int ref, int boundsWidth, int boundsHeight, int increment, Context context)
  {
    int derivedX = randomGenerator.nextInt(boundsWidth + 1);
    int derivedY = randomGenerator.nextInt(boundsHeight + 1);

    this.boundsWidth = boundsWidth;
    this.boundsHeight = boundsHeight;
    this.xIncrement = this.yIncrement = this.increment = increment;

    sprite = SpriteCache.getInstance().getSprite(ref, context);

    spriteWidth = sprite.getWidth();
    spriteHeight = sprite.getHeight();

    derivedX = derivedX + spriteWidth > boundsWidth ? derivedX - spriteWidth : derivedX;
    derivedY = derivedY + spriteHeight > boundsHeight ? derivedY - spriteHeight : derivedY;

    rectangle = new Rect(derivedX, derivedY, derivedX + spriteWidth, derivedY + spriteHeight);
  }

  public void draw(Canvas canvas)
  {
    sprite.draw(canvas, rectangle.left, rectangle.top);
  }

  public void move()
  {
    int derivedX = rectangle.left + xIncrement;
    int derivedY = rectangle.top + yIncrement;

    if (derivedX <= 0)
    {
      rectangle.left = 0;
      xIncrement = increment;
    }
    else if (derivedX + spriteWidth > boundsWidth)
    {
      rectangle.left = boundsWidth - spriteWidth;
      xIncrement = -increment;
    }
    else
    {
      rectangle.left += xIncrement;
    }

    rectangle.right = rectangle.left + spriteWidth;

    if (derivedY <= 0)
    {
      rectangle.top = 0;
      yIncrement = increment;
    }
    else if (derivedY + spriteHeight > boundsHeight)
    {
      rectangle.top = boundsHeight - spriteHeight;
      yIncrement = -increment;
    }
    else
    {
      rectangle.top += yIncrement;
    }

    rectangle.bottom = rectangle.top + spriteHeight;
  }

  public boolean isHit(int x, int y)
  {
    return rectangle.contains(x, y);
  }
}
