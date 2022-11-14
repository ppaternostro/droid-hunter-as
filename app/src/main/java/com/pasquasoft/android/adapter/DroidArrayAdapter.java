package com.pasquasoft.android.adapter;

import com.pasquasoft.android.Droid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class DroidArrayAdapter extends ArrayAdapter<Droid>
{
  private final LayoutInflater inflater;
  private final int resource;
  private final int textViewResourceId;
  private final int imageViewResourceId;
  private final Droid[] droids;

  public DroidArrayAdapter(Context context, int resource, int textViewResourceId, int imageViewResourceId,
      Droid[] droids)
  {
    super(context, resource, textViewResourceId, droids);

    this.resource = resource;
    this.textViewResourceId = textViewResourceId;
    this.imageViewResourceId = imageViewResourceId;
    this.droids = droids;

    inflater = LayoutInflater.from(context);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
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
    ImageView imageView = row.findViewById(imageViewResourceId);

    textView.setText(droids[position].getTextResourceId());

    imageView.setImageResource(droids[position].getImageResourceId());

    return row;
  }
}
