package com.pasquasoft.android.model;

/**
 * A record that defines the attributes of a level.
 *
 * @param entities the number of entities
 * @param increment the increment
 * @param framesPerSecond the frames per second
 * @param name the level name
 * @param timeLimit the time limit in mm:ss string format
 *
 * @author Pat Paternostro
 * @version 1.0
 */
public record Level(int entities, int increment, int framesPerSecond, String name, String timeLimit)
{

}
