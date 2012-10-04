/*
 * Copyright (C) 2012 OBN-soft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obnsoft.app;

import java.text.DecimalFormat;
import java.text.ParseException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class DigitsDialogBuilder extends AlertDialog.Builder {

    private static final int FP = LayoutParams.FILL_PARENT;
    private static final int WC = LayoutParams.WRAP_CONTENT;
    private static final int MAX_DIGITS = 8;
    private static final String[] LAYOUT_PORT = {"123D", "456.", "7890"};
    private static final String[] LAYOUT_LAND = {"12345D", "67890."};

    private final DecimalFormat mFormat = new DecimalFormat("0.########");
    private final char      mPointChar = mFormat.getDecimalFormatSymbols().getDecimalSeparator();
    private final String    mPointCharStr = String.valueOf(mPointChar);

    private AlertDialog     mDialog;
    private TextView        mTextViewMsg;
    private TextView        mTextViewValue;
    private Button          mButtonPoint;
    private ImageButton     mButtonDelete;
    private StringBuffer    mStrBufValue = new StringBuffer();
    private OnClickListener mOKButtonListener;
    private OnKeyListener   mKeyListener;

    private int mDigits = 8;
    private int mFractions = 0;

    /*----------------------------------------------------------------------*/

    public DigitsDialogBuilder(Context context) {
        super(context);
        super.setView(createEntryView(context));
        super.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (mKeyListener != null && mKeyListener.onKey(dialog, keyCode, event)) {
                    return true;
                }
                boolean ret = false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        mStrBufValue.deleteCharAt(mStrBufValue.length() - 1);
                        ret = true;
                    } else if (keyCode == KeyEvent.KEYCODE_PERIOD ||
                            keyCode == KeyEvent.KEYCODE_COMMA ||
                            keyCode == KeyEvent.KEYCODE_STAR) {
                        mStrBufValue.append(mPointChar);
                        ret = true;
                    } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                        mStrBufValue.append(keyCode - KeyEvent.KEYCODE_0);
                        ret = true;
                    }
                    if (ret) {
                        setTextViewValue();
                    }
                }
                return ret;
            }
        });
    }

    @Override
    public AlertDialog create() {
        mDialog = super.create();
        return mDialog;
    }

    @Override
    public Builder setMessage(CharSequence message) {
        if (message.length() > 0) {
            mTextViewMsg.setText(message);
            mTextViewMsg.setVisibility(View.VISIBLE);
        } else {
            mTextViewMsg.setVisibility(View.GONE);
        }
        return this;
    }

    @Override
    public Builder setMessage(int messageId) {
        if (messageId != 0) {
            mTextViewMsg.setText(messageId);
            mTextViewMsg.setVisibility(View.VISIBLE);
        } else {
            mTextViewMsg.setVisibility(View.GONE);
        }
        return this;
    }

    @Override
    public Builder setOnKeyListener(OnKeyListener onKeyListener) {
        mKeyListener = onKeyListener;
        return this;
    }

    @Override
    public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
        mOKButtonListener = listener;
        return super.setPositiveButton(text, listener);
    }

    @Override
    public Builder setPositiveButton(int textId, OnClickListener listener) {
        mOKButtonListener = listener;
        return super.setPositiveButton(textId, listener);
    }

    @Override
    public Builder setView(View view) {
        return this;    // Do nothing
    }

    /*----------------------------------------------------------------------*/

    public void setDigits(int digits, int fractions) {
        mDigits = (digits >= 1 && digits <= MAX_DIGITS) ? digits : 1;
        mFractions = (fractions >= 0 && fractions <= MAX_DIGITS) ? fractions : 0;
        mButtonPoint.setVisibility((mFractions > 0) ? View.VISIBLE : View.INVISIBLE);
    }

    public void setValue(int value) {
        setValue(mFormat.format(value));
    }

    public void setValue(double value) {
        setValue(mFormat.format(value));
    }

    public void setValue(String value) {
        mStrBufValue = new StringBuffer(value);
        setTextViewValue();
    }

    /*----------------------------------------------------------------------*/

    public int getIntValue() {
        try {
            return mFormat.parse(getStringValue()).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getDoubleValue() {
        try {
            return mFormat.parse(getStringValue()).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public String getStringValue() {
        return mStrBufValue.toString();
    }

    /*----------------------------------------------------------------------*/

    private View createEntryView(Context context) {
        LinearLayout ret = new LinearLayout(context);
        ret.setOrientation(LinearLayout.VERTICAL);
        LayoutParams lp = new LayoutParams(FP, WC);
        lp.weight = 1;

        mTextViewMsg = new TextView(context);
        mTextViewMsg.setPadding(8, 0, 8, 0);
        mTextViewMsg.setVisibility(View.GONE);
        ret.addView(mTextViewMsg, lp);

        mTextViewValue = new TextView(context, null, android.R.attr.editTextStyle);
        mTextViewValue.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
        mTextViewValue.setGravity(Gravity.CENTER);
        mTextViewValue.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (mOKButtonListener != null) {
                        mOKButtonListener.onClick(mDialog, AlertDialog.BUTTON_POSITIVE);
                    }
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        ret.addView(mTextViewValue, lp);

        int orientation = context.getResources().getConfiguration().orientation;
        String[] layout =
            (orientation == Configuration.ORIENTATION_LANDSCAPE) ? LAYOUT_LAND : LAYOUT_PORT;
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == mButtonDelete) {
                    mStrBufValue.deleteCharAt(mStrBufValue.length() - 1);
                } else {
                    Button btn = (Button) view;
                    mStrBufValue.append(btn.getText());
                }
                setTextViewValue();
            }
        };

        for (String line : layout) {
            LinearLayout ll = new LinearLayout(context);
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                View view;
                if (c == 'D') {
                    mButtonDelete = new ImageButton(context, null, android.R.attr.buttonStyleSmall);
                    mButtonDelete.setImageResource(android.R.drawable.ic_input_delete);
                    mButtonDelete.setColorFilter(Color.argb(192, 0, 0, 0));
                    view = mButtonDelete;
                } else {
                    Button btn = new Button(context, null, android.R.attr.buttonStyleSmall);
                    btn.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
                    btn.setText(String.valueOf(c));
                    if (c == '.') {
                        mButtonPoint = btn;
                        mButtonPoint.setText(mPointCharStr);
                        mButtonPoint.setVisibility(View.INVISIBLE);
                    }
                    view = btn;
                }
                view.setPadding(0, 0, 0, 0);
                view.setOnClickListener(clickListener);
                ll.addView(view, lp);
            }
            ret.addView(ll);
        }
        return ret;
    }

    private void setTextViewValue() {
        int len = mStrBufValue.length();
        int pointPos = mStrBufValue.indexOf(mPointCharStr);
        if (pointPos == -1) {
            while (len > 0 && mStrBufValue.charAt(0) == '0') {
                mStrBufValue.deleteCharAt(0);
                len--;
            }
            if (len == 0) {
                mStrBufValue.append('0');
            } else if (len > mDigits) {
                mStrBufValue.setLength(mDigits);
            }
        } else {
            int maxLen = pointPos + mFractions + 1;
            if (pointPos < len - 1 && mStrBufValue.charAt(len - 1) == mPointChar) len--;
            if (mFractions == 0) maxLen--;
            if (len > maxLen) len = maxLen;
            mStrBufValue.setLength(len);
            if (pointPos > mDigits) {
                mStrBufValue.delete(mDigits, pointPos);
            }
        }
        mTextViewValue.setText(mStrBufValue.toString());
    }
}
