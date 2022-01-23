package com.pasquasoft.android.util;

import com.pasquasoft.android.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class Util
{
  public static void messageDialog(Context context, String title, String message, OnClickListener listener)
  {
    Builder builder = new AlertDialog.Builder(context);

    builder.setCancelable(false); // Don't allow back button to dismiss dialog
    builder.setTitle(title);
    builder.setIcon(R.drawable.iron);
    builder.setMessage(message);
    builder.setPositiveButton(context.getString(R.string.label_ok), listener);

    builder.show();
  }
}
