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

package com.obnsoft.sandbox;

import com.obnsoft.view.MagnifyView;
import com.obnsoft.view.MagnifyView.EventHandler;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;

public class MyMagnifyActivity extends Activity implements EventHandler {

    float[] mHSV = {0f, 1f, 1f};
    Bitmap  mBitmap;
    Canvas  mCanvas;
    Paint   mPaint = new Paint();

    MagnifyView mMgView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        //mBitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        mMgView = new MagnifyView(this);
        mMgView.setBitmap(mBitmap);
        mMgView.setGridColor(Color.GRAY, false);
        //mMgView.setEventHandler(this);
        setContentView(mMgView);
    }

    @Override
    protected void onDestroy() {
        mBitmap.recycle();
        mBitmap = null;
        super.onDestroy();
    }

    @Override
    public boolean onTouchEventUnit(MotionEvent ev, int x, int y) {
        mPaint.setColor(Color.HSVToColor(mHSV));
        mCanvas.drawPoint(x, y, mPaint);
        if (++mHSV[0] >= 360) mHSV[0] = 0;
        mMgView.invalidateUnit(x, y);
        return true;
    }
}
