package com.pasquasoft.android.adapter;

import com.pasquasoft.android.Droid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class DroidArrayAdapter extends ArrayAdapter<Droid>
{
  private final LayoutInflater inflater;
  private final int resource;
  private final int textViewResourceId;
  private final Droid[] droids;

  public DroidArrayAdapter(Context context, int resource, int textViewResourceId, Droid[] droids)
  {
    super(context, resource, textViewResourceId, droids);

    this.resource = resource;
    this.textViewResourceId = textViewResourceId;
    this.droids = droids;

    inflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public View getView(int position, View convertView, @NonNull ViewGroup parent)
  {
    return getCustomView(position, parent);
  }

  @Override
  public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
  {
    return getCustomView(position, parent);
  }

  private View getCustomView(int position, ViewGroup parent)
  {
    View row = inflater.inflate(resource, parent, false);

    TextView textView = row.findViewById(textViewResourceId);

    textView.setText(droids[position].getTextResourceId());
    textView.setCompoundDrawablesWithIntrinsicBounds(droids[position].getImageResourceId(), 0, 0, 0);

    return row;
  }
}
