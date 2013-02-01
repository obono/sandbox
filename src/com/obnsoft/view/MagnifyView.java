/*
 * Copyright (C) 2013 OBN-soft
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

package com.obnsoft.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MagnifyView extends View {

    private int     mUnit = 2;
    private boolean mDotted;
    private Bitmap  mBitmap;
    private Rect    mBitmapRect = new Rect();
    private Rect    mDrawRect = new Rect();
    private Paint   mGridPaint = new Paint();

    private EventHandler mHandler;

    /*-----------------------------------------------------------------------*/

    public interface EventHandler {
        public boolean onTouchEventUnit(MotionEvent ev, int unitX, int unitY);
    }

    /*-----------------------------------------------------------------------*/

    public MagnifyView(Context context) {
        this(context, null);
    }

    public MagnifyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagnifyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGridPaint.setAntiAlias(false);
        mGridPaint.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap == null) {
            return;
        }
        canvas.drawBitmap(mBitmap, mBitmapRect, mDrawRect, null);
        if (mGridPaint.getColor() != Color.TRANSPARENT) {
            Rect clip = canvas.getClipBounds();
            int cl = Math.max(clip.left, mDrawRect.left);
            int cr = Math.min(clip.right, mDrawRect.right + 1);
            int ct = Math.max(clip.top, mDrawRect.top);
            int cb = Math.min(clip.bottom, mDrawRect.bottom + 1);
            if (mDotted) {
                for (int x = mDrawRect.left; x <= cr; x += mUnit) {
                    for (int y = mDrawRect.top; y <= cb; y += mUnit) {
                        if (x >= cl && y >= ct) {
                            canvas.drawPoint(x, y, mGridPaint);
                        }
                    }
                }
            } else {
                for (int x = mDrawRect.left; x <= cr; x += mUnit) {
                    if (x >= cl) {
                        canvas.drawLine(x, ct, x, cb, mGridPaint);
                    }
                }
                for (int y = mDrawRect.top; y <= cb; y += mUnit) {
                    if (y >= ct) {
                        canvas.drawLine(cl, y, cr, y, mGridPaint);
                    }
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calcCoords();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mHandler == null) {
            return false;
        }
        int x = ((int) event.getX() - mDrawRect.left) / mUnit;
        int y = ((int) event.getY() - mDrawRect.top) / mUnit;
        return mHandler.onTouchEventUnit(event, x, y);
    }

    /*-----------------------------------------------------------------------*/

    public void setBitmap(Bitmap bmp) {
        mBitmap = bmp;
        calcCoords();
        invalidate();
    }

    public void setGridColor(int color, boolean dotted) {
        mGridPaint.setColor(color);
        mDotted = dotted;
        invalidate();
    }

    public void setEventHandler(EventHandler handler) {
        mHandler = handler;
    }

    public void invalidateUnit(int x, int y) {
        int dx = mDrawRect.left;
        int dy = mDrawRect.top;
        invalidate(dx + x * mUnit, dy + y * mUnit, dx + (x + 1) * mUnit, dy + (y + 1) * mUnit);
    }

    public void invalidateUnit(int l, int t, int r, int b) {
        if (l > r) {
            invalidateUnit(r, t, l, b);
        } else if (t > b) {
            invalidateUnit(l, b, r, t);
        } else {
            int dx = mDrawRect.left;
            int dy = mDrawRect.top;
            invalidate(dx + l * mUnit, dy + t * mUnit, dx + (r + 1) * mUnit, dy + (b + 1) * mUnit);
        }
    }

    /*-----------------------------------------------------------------------*/

    private void calcCoords() {
        if (mBitmap == null) {
            mBitmapRect.set(0, 0, 0, 0);
            mDrawRect.set(0, 0, 0, 0);
            return;
        }
        int sw = mBitmap.getWidth();
        int sh = mBitmap.getHeight();
        mBitmapRect.set(0, 0, sw, sh);
        int dw = getWidth();
        int dh = getHeight();
        if (sw == 0 || sh == 0 || dw == 0 || dh == 0) {
            mDrawRect.set(0, 0, 0, 0);
            return;
        }
        mUnit = Math.min((dw - 1) / sw, (dh - 1) / sh);
        if (mUnit < 2) {
            mUnit = 2;
        }
        int dx = (dw - sw * mUnit) / 2;
        int dy = (dh - sh * mUnit) / 2;
        mDrawRect.set(dx, dy, dx + sw * mUnit, dy + sh * mUnit);
    }
}
