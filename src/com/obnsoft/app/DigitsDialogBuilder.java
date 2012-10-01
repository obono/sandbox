package com.obnsoft.app;

import java.text.DecimalFormat;
import java.text.ParseException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
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
    private final char  mPointChar = mFormat.getDecimalFormatSymbols().getDecimalSeparator();

    private TextView    mTextViewMsg;
    private TextView    mTextViewValue;
    private Button      mButtonPoint;
    private ImageButton mButtonDelete;

    private int mDigits = 8;
    private int mFractions = 0;

    /*----------------------------------------------------------------------*/

    public DigitsDialogBuilder(Context context) {
        super(context);
        super.setView(createEntryView(context));
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
    public AlertDialog.Builder setMessage(CharSequence message) {
        if (message.length() > 0) {
            mTextViewMsg.setText(message);
            mTextViewMsg.setVisibility(View.VISIBLE);
        } else {
            mTextViewMsg.setVisibility(View.GONE);
        }
        return this;
    }

    @Override
    public AlertDialog.Builder setView(View view) {
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
        mTextViewValue.setText(controlValue(value));
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
        return mTextViewValue.getText().toString();
    }

    /*----------------------------------------------------------------------*/

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            CharSequence chrs = mTextViewValue.getText();
            if (view == mButtonDelete) {
                chrs = chrs.subSequence(0, chrs.length() - 1);
            } else {
                Button btn = (Button) view;
                chrs = new StringBuffer(chrs).append(btn.getText());
            }
            mTextViewValue.setText(controlValue(chrs));
        }
    };

    private OnKeyListener mKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            boolean ret = false;
            CharSequence chrs = mTextViewValue.getText();
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                chrs = chrs.subSequence(0, chrs.length() - 1);
                ret = true;
            } else if (keyCode == KeyEvent.KEYCODE_PERIOD || keyCode == KeyEvent.KEYCODE_COMMA) {
                chrs = new StringBuffer(chrs).append(mPointChar);
                ret = true;
            } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                chrs = new StringBuffer(chrs).append(keyCode - KeyEvent.KEYCODE_0);
                ret = true;
            }
            if (ret) {
                mTextViewValue.setText(controlValue(chrs));
            }
            return ret;
        }
    };

    private View createEntryView(Context context) {
        LinearLayout ret = new LinearLayout(context);
        ret.setOrientation(LinearLayout.VERTICAL);
        LayoutParams lp = new LayoutParams(FP, WC);
        lp.weight = 1;

        mTextViewMsg = new TextView(context);
        mTextViewMsg.setPadding(8, 0, 8, 0);
        mTextViewMsg.setVisibility(View.GONE);
        ret.addView(mTextViewMsg, lp);

        mTextViewValue = new TextView(context);
        mTextViewValue.setBackgroundResource(android.R.drawable.edit_text);
        mTextViewValue.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
        mTextViewValue.setGravity(Gravity.CENTER);
        ret.addView(mTextViewValue, lp);

        int orientation = context.getResources().getConfiguration().orientation;
        String[] layout =
            (orientation == Configuration.ORIENTATION_LANDSCAPE) ? LAYOUT_LAND : LAYOUT_PORT;

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
                        mButtonPoint.setText(String.valueOf(mPointChar));
                        mButtonPoint.setVisibility(View.INVISIBLE);
                    }
                    view = btn;
                }
                view.setPadding(0, 0, 0, 0);
                view.setOnClickListener(mClickListener);
                view.setOnKeyListener(mKeyListener);
                ll.addView(view, lp);
            }
            ret.addView(ll);
        }
        return ret;
    }

    private CharSequence controlValue(CharSequence chrs) {
        int len = chrs.length();
        int pointPos = chrs.toString().indexOf(mPointChar);
        if (pointPos == -1) {
            while (len > 0 && chrs.charAt(0) == '0') {
                chrs = chrs.subSequence(1, chrs.length());
                len--;
            }
            if (len == 0) {
                chrs = "0";
            } else if (len > mDigits) {
                chrs = chrs.subSequence(0, mDigits);
            }
        } else {
            int maxLen = pointPos + mFractions + 1;
            if (pointPos < len - 1 && chrs.charAt(len - 1) == mPointChar) len--;
            if (mFractions == 0) maxLen--;
            if (len > maxLen) len = maxLen;
            chrs = chrs.subSequence(0, len);
            if (pointPos > mDigits) {
                chrs = new StringBuffer(chrs.subSequence(0, mDigits))
                        .append(chrs.subSequence(pointPos, len));
            }
        }
        return chrs;
    }
}
