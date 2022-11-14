package com.pasquasoft.android;

import com.pasquasoft.android.dialog.SettingsDialog;
import com.pasquasoft.android.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Main extends Activity implements OnClickListener
{
  private Intent droidIntent;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    setContentView(R.layout.main);

    findViewById(R.id.newGame).setOnClickListener(this);
    findViewById(R.id.settings).setOnClickListener(this);
    findViewById(R.id.about).setOnClickListener(this);
    findViewById(R.id.howTo).setOnClickListener(this);

    droidIntent = new Intent(this, DroidHunter.class);
  }

  @Override
  public void onClick(View view)
  {
    int id = view.getId();

    if (id == R.id.newGame)
    {
      startActivity(droidIntent);
    }
    else if (id == R.id.settings)
    {
      new SettingsDialog(this);
    }
    else if (id == R.id.about || id == R.id.howTo)
    {
      Util.messageDialog(this, id == R.id.about ? getString(R.string.title_about) : getString(R.string.label_how_to),
          id == R.id.about ? getString(R.string.message_about) : getString(R.string.message_how_to), null);
    }
  }
}
