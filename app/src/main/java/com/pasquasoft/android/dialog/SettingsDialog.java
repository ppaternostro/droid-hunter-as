package com.pasquasoft.android.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.pasquasoft.android.DroidHunterApplication;
import com.pasquasoft.android.R;
import com.pasquasoft.android.adapter.DroidArrayAdapter;
import com.pasquasoft.android.util.Util;

import java.util.Locale;

public class SettingsDialog extends Dialog implements OnClickListener
{
  private static final String PREFS = "DroidHunter";

  private static final int IRON = 10;
  private static final int CANNON = 0;
  private static final int ON = 0;
  private static final int EASY = 0;
  private static final int SINGLE = 0;

  private final EditText droids;
  private final EditText timeLimit;

  private final Spinner droidModel;
  private final Spinner gameMode;
  private final Spinner soundMode;
  private final Spinner weaponSound;
  private final Spinner difficultyLevel;

  private final Button save;
  private final Button cancel;

  private final SharedPreferences prefs;

  private final Context context;

  public SettingsDialog(Context context)
  {
    super(context);

    this.context = context;

    setContentView(R.layout.settings);

    /* Retrieve preferences reference */
    prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

    int model;
    int mode;
    int count;
    int sound;
    int weapon;
    int difficulty;
    String limit;

    /* Retrieve references to widgets */
    droidModel = findViewById(R.id.droidModel);
    gameMode = findViewById(R.id.gameMode);
    droids = findViewById(R.id.droids);
    timeLimit = findViewById(R.id.timeLimit);
    soundMode = findViewById(R.id.soundMode);
    weaponSound = findViewById(R.id.weaponSound);
    difficultyLevel = findViewById(R.id.difficultyLevel);
    save = findViewById(R.id.save);
    cancel = findViewById(R.id.cancel);

    /* Set 'model' spinner values */
    DroidArrayAdapter modelAdapter = new DroidArrayAdapter(context, R.layout.row, R.id.row_text, R.id.row_icon,
        ((DroidHunterApplication) ((Activity) context).getApplication()).getDroids());
    modelAdapter.setDropDownViewResource(R.layout.row);
    droidModel.setAdapter(modelAdapter);

    /* Set 'mode' spinner values */
    ArrayAdapter<?> modeAdapter = ArrayAdapter.createFromResource(context, R.array.modes,
        android.R.layout.simple_spinner_item);
    modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    gameMode.setAdapter(modeAdapter);

    /* Set 'sound' spinner values */
    ArrayAdapter<?> soundAdapter = ArrayAdapter.createFromResource(context, R.array.sounds,
        android.R.layout.simple_spinner_item);
    soundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    soundMode.setAdapter(soundAdapter);

    /* Set 'weapon' spinner values */
    ArrayAdapter<?> weaponAdapter = ArrayAdapter.createFromResource(context, R.array.weapons,
        android.R.layout.simple_spinner_item);
    weaponAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    weaponSound.setAdapter(weaponAdapter);

    /* Set 'difficulty' spinner values */
    ArrayAdapter<?> difficultyAdapter = ArrayAdapter.createFromResource(context, R.array.difficulties,
        android.R.layout.simple_spinner_item);
    difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    difficultyLevel.setAdapter(difficultyAdapter);

    /* Set button listeners */
    save.setOnClickListener(this);
    cancel.setOnClickListener(this);

    /* Retrieve game preferences */
    model = prefs.getInt(context.getString(R.string.prefs_droid_model_key), IRON);
    mode = prefs.getInt(context.getString(R.string.prefs_game_mode_key), SINGLE);
    count = prefs.getInt(context.getString(R.string.prefs_droid_number_key), 10);
    limit = prefs.getString(context.getString(R.string.prefs_time_limit_key),
        context.getString(R.string.prefs_time_limit_default));
    sound = prefs.getInt(context.getString(R.string.prefs_sound_mode_key), ON);
    weapon = prefs.getInt(context.getString(R.string.prefs_weapon_sound_key), CANNON);
    difficulty = prefs.getInt(context.getString(R.string.prefs_difficulty_level_key), EASY);

    /* Set values to widgets */
    droidModel.setSelection(model);
    gameMode.setSelection(mode);
    droids.setText(String.format(Locale.getDefault(), "%d", count));
    timeLimit.setText(limit);
    soundMode.setSelection(sound);
    weaponSound.setSelection(weapon);
    difficultyLevel.setSelection(difficulty);

    /* Set selection listener */
    gameMode.setOnItemSelectedListener(new OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
      {
        boolean single = parent.getItemAtPosition(pos).toString()
            .equals(SettingsDialog.this.context.getString(R.string.mode_single));

        /* Enable/disable the appropriate controls */
        droids.setEnabled(single);
        timeLimit.setEnabled(single);
        difficultyLevel.setEnabled(single);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent)
      {
      }
    });

    setTitle(R.string.title_settings);

    setCancelable(false);

    show();
  }

  @Override
  public void onClick(View view)
  {
    if (view == save)
    {
      /*
       * Determine if valid droids number and time limit matches regular
       * expression
       */
      if (isValid(droids.getEditableText().toString())
          && timeLimit.getEditableText().toString().matches("^([0-5][0-9]):([0-5][0-9])$"))
      {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(context.getString(R.string.prefs_droid_model_key), droidModel.getSelectedItemPosition());
        editor.putInt(context.getString(R.string.prefs_game_mode_key), gameMode.getSelectedItemPosition());
        editor.putInt(context.getString(R.string.prefs_droid_number_key),
            Integer.parseInt(droids.getEditableText().toString()));
        editor.putString(context.getString(R.string.prefs_time_limit_key), timeLimit.getEditableText().toString());
        editor.putInt(context.getString(R.string.prefs_sound_mode_key), soundMode.getSelectedItemPosition());
        editor.putInt(context.getString(R.string.prefs_weapon_sound_key), weaponSound.getSelectedItemPosition());
        editor.putInt(context.getString(R.string.prefs_difficulty_level_key),
            difficultyLevel.getSelectedItemPosition());

        /* Commit the changes - apply persists in the background */
        editor.apply();

        dismiss();
      }
      else
      {
        Util.messageDialog(context, context.getString(R.string.title_error_dialog),
            context.getString(R.string.message_time_limit_error), null);
      }
    }
    else if (view == cancel)
    {
      dismiss();
    }
  }

  private boolean isValid(String droids)
  {
    return !droids.equals("0") && !droids.equals("00") && !droids.equals("");
  }
}
