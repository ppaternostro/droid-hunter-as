package com.pasquasoft.android;

public class Droid
{
  private int textResourceId;
  private int imageResourceId;

  public Droid(int textResourceId, int imageResourceId)
  {
    this.textResourceId = textResourceId;
    this.imageResourceId = imageResourceId;
  }

  public void setImageResourceId(int imageResourceId)
  {
    this.imageResourceId = imageResourceId;
  }

  public int getImageResourceId()
  {
    return imageResourceId;
  }

  public void setTextResourceId(int textResourceId)
  {
    this.textResourceId = textResourceId;
  }

  public int getTextResourceId()
  {
    return textResourceId;
  }
}
