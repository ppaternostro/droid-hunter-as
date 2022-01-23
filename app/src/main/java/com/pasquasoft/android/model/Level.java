package com.pasquasoft.android.model;

/**
 * A class that defines the attributes and behavior of a level.
 * 
 * @author Pat Paternostro
 * @version 1.0
 */
public class Level
{
  private final int entities;
  private final int increment;
  private final int framesPerSecond;
  private final String name;
  private final String timeLimit;

  /**
   * Constructs a <code>Level</code> object with the specified parameters.
   * 
   * @param entities the number of entities
   * @param increment the increment
   * @param framesPerSecond the frames per second
   * @param name the level name
   * @param timeLimit the time limit in mm:ss string format
   */
  public Level(int entities, int increment, int framesPerSecond, String name, String timeLimit)
  {
    this.entities = entities;
    this.increment = increment;
    this.framesPerSecond = framesPerSecond;
    this.name = name;
    this.timeLimit = timeLimit;
  }

  public int getEntities()
  {
    return entities;
  }

  public int getIncrement()
  {
    return increment;
  }

  public int getFramesPerSecond()
  {
    return framesPerSecond;
  }

  public String getTimeLimit()
  {
    return timeLimit;
  }

  public String getName()
  {
    return name;
  }
}
