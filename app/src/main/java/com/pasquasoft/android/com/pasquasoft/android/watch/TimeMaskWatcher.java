package com.pasquasoft.android.com.pasquasoft.android.watch;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

import androidx.annotation.NonNull;

public class TimeMaskWatcher implements TextWatcher
{
  private static final String mask = "##:##";
  private boolean isRunning;

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after)
  {
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count)
  {
  }

  @Override
  public void afterTextChanged(Editable s)
  {
    if (isRunning)
      return;

    isRunning = true;

    // Get raw digits and apply "0-5" rule for specific positions
    String rawInput = s.toString().replaceAll("\\D", "");
    String cleanDigits = getCleanDigits(rawInput);

    // Track cursor based on validated digits
    int initialCursor = Selection.getSelectionStart(s);
    String beforeCursor = s.toString().substring(0, Math.min(initialCursor, s.length())).replaceAll("\\D", "");

    // We only care about how many VALID digits were before the cursor
    int digitsBeforeCursor = 0;
    int countValid = 0;

    for (int i = 0; i < beforeCursor.length(); i++)
    {
      if (countValid == 0 || countValid == 2)
      {
        if (beforeCursor.charAt(i) <= '5')
        {
          digitsBeforeCursor++;
          countValid++;
        }
      }
      else
      {
        digitsBeforeCursor++;
        countValid++;
      }
    }

    // Apply the mask
    StringBuilder formatted = new StringBuilder();
    int digitCount = 0;
    int newCursorPos = -1;

    for (char m : mask.toCharArray())
    {
      if (digitCount >= cleanDigits.length())
        break;

      if (m == '#')
      {
        formatted.append(cleanDigits.charAt(digitCount));
        digitCount++;
      }
      else
      {
        formatted.append(m);
      }

      if (digitCount == digitsBeforeCursor && newCursorPos == -1)
      {
        newCursorPos = formatted.length();
      }
    }

    // Update UI
    s.replace(0, s.length(), formatted.toString());

    Selection.setSelection(s, newCursorPos != -1 ? Math.min(newCursorPos, s.length()) : s.length());

    isRunning = false;
  }

  @NonNull
  private String getCleanDigits(String rawInput)
  {
    StringBuilder validatedDigits = new StringBuilder();

    for (int i = 0; i < rawInput.length(); i++)
    {
      char digit = rawInput.charAt(i);

      // Position 0 (1st digit) and position 2 (3rd digit) must be <= '5'
      if ((validatedDigits.length() == 0 || validatedDigits.length() == 2))
      {
        if (digit <= '5')
        {
          validatedDigits.append(digit);
        }
      }
      else
      {
        validatedDigits.append(digit);
      }
    }

    return validatedDigits.toString();
  }
}
