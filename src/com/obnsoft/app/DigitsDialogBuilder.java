package com.obnsoft.app;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class DigitsDialogBuilder extends AlertDialog.Builder {

    private static final int FP = LayoutParams.FILL_PARENT;
    private static final int WC = LayoutParams.WRAP_CONTENT;
    private static final String[] LAYOUT_PORT = {"123D", "456.", "7890"};
    private static final String[] LAYOUT_LAND = {"12345D", "67890."};

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
        mDigits = (digits > 0 ) ? digits : 0;
        mFractions = (fractions > 0) ? fractions : 0;
        mButtonPoint.setVisibility((mFractions > 0) ? View.VISIBLE : View.INVISIBLE);
    }

    public void setValue(int value) {
        setValue(String.valueOf(value));
    }

    public void setValue(double value) {
        setValue(String.valueOf(value));
    }

    public void setValue(String value) {
        mTextViewValue.setText(controlValue(value));
    }

    /*----------------------------------------------------------------------*/

    public int getIntValue() {
        return Integer.parseInt(getStringValue());
    }

    public double getDoubleValue() {
        return Double.parseDouble(getStringValue());
    }

    public String getStringValue() {
        return mTextViewValue.getText().toString();
    }

    /*----------------------------------------------------------------------*/

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            CharSequence chr = mTextViewValue.getText();
            if (view == mButtonDelete) {
                chr = chr.subSequence(0, chr.length() - 1);
            } else {
                Button btn = (Button) view;
                chr = new StringBuffer(chr).append(btn.getText());
            }
            mTextViewValue.setText(controlValue(chr));
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
        mTextViewValue.setTextAppearance(context, android.R.style.TextAppearance_Large);
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
                    mButtonDelete = new ImageButton(context);
                    mButtonDelete.setImageResource(android.R.drawable.ic_input_delete);
                    mButtonDelete.setColorFilter(Color.argb(192, 0, 0, 0));
                    view = mButtonDelete;
                } else {
                    Button btn = new Button(context);
                    btn.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
                    btn.setText(String.valueOf(c));
                    if (c == '.') {
                        mButtonPoint = btn;
                        mButtonPoint.setVisibility(View.INVISIBLE);
                    }
                    view = btn;
                }
                view.setOnClickListener(mClickListener);
                //view.setOnKeyListener(null);
                ll.addView(view, lp);
            }
            ret.addView(ll);
        }
        return ret;
    }

    private CharSequence controlValue(CharSequence chr) {
        int len = chr.length();
        int pointPos = chr.toString().indexOf('.');
        if (pointPos == -1) {
            while (len > 0 && chr.charAt(0) == '0') {
                chr = chr.subSequence(1, chr.length());
                len--;
            }
            if (len == 0) {
                chr = "0";
            } else if (len > mDigits) {
                chr = chr.subSequence(0, mDigits);
            }
        } else {
            int maxLen = pointPos + mFractions + 1;
            if (pointPos < len - 1 && chr.charAt(len - 1) == '.') len--;
            if (mFractions == 0) maxLen--;
            if (len > maxLen) len = maxLen;
            chr = chr.subSequence(0, len);
            if (pointPos > mDigits) {
                chr = new StringBuffer(chr.subSequence(0, mDigits))
                        .append(chr.subSequence(pointPos, len));
            }
        }
        return chr;
    }
}
