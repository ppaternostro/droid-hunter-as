package com.pasquasoft.android.sprite;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * A class that caches <code>Sprite</code> references.
 * 
 * @author Pat Paternostro
 * @version v1.0
 * @see Sprite
 */
public final class SpriteCache
{
  /**
   * A singleton reference to this object.
   */
  private static final SpriteCache INSTANCE = new SpriteCache();

  /**
   * The sprite cache.
   */
  private final HashMap<Integer, Sprite> sprites = new HashMap<>();

  /**
   * Prevent instantiation outside of this class.
   */
  private SpriteCache()
  {
  }

  /**
   * Retrieves a singleton reference to this object.
   * 
   * @return a singleton reference to this object
   */
  public static SpriteCache getInstance()
  {
    return INSTANCE;
  }

  /**
   * Retrieves a sprite.
   * 
   * @param ref the sprite's reference
   * @param context the sprite's context
   * @return a sprite instance
   */
  public synchronized Sprite getSprite(int ref, Context context)
  {
    /* Check cache first */
    Sprite sprite = sprites.get(ref);

    /* Load the sprite */
    if (sprite == null)
    {
      Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), ref);

      /* Create the sprite */
      sprite = new Sprite(bitmap);

      /* Add to the cache */
      sprites.put(ref, sprite);
    }

    return sprite;
  }
}
