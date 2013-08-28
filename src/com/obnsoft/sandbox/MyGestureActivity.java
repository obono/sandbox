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

import com.obnsoft.view.ScaleRotateGestureDetector;
import com.obnsoft.view.ScaleRotateGestureDetector.OnScaleRotateGestureListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MyGestureActivity extends Activity implements OnScaleRotateGestureListener {

    private boolean mIsDetecting = false;
    private float mCurrX;
    private float mCurrY;
    private float mCurrSpan;
    private double mCurrAngle;
    private MyView mView;
    private ScaleRotateGestureDetector mDetector;

    /*-----------------------------------------------------------------------*/

    class MyView extends View {

        private Paint mPaint;
        private Path mWorkPath = new Path();

        public MyView(Context context) {
            super(context);
            mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
            mPaint.setAntiAlias(true);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return mDetector.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mIsDetecting) {
                float vx = (float) (Math.cos(mCurrAngle) * mCurrSpan / 2.0);
                float vy = (float) (-Math.sin(mCurrAngle) * mCurrSpan / 2.0);
                mWorkPath.reset();
                mWorkPath.moveTo(mCurrX + vx, mCurrY + vy);
                mWorkPath.lineTo(mCurrX + vy, mCurrY - vx);
                mWorkPath.lineTo(mCurrX - vx, mCurrY - vy);
                mWorkPath.lineTo(mCurrX - vy, mCurrY + vx);
                mWorkPath.close();
                canvas.drawPath(mWorkPath, mPaint);
            }
        }
    }

    /*-----------------------------------------------------------------------*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new MyView(this);
        setContentView(mView);
        mDetector = new ScaleRotateGestureDetector(this, this);
    }

    @Override
    public boolean onScaleRotate(ScaleRotateGestureDetector detector) {
        mCurrX = detector.getFocusX();
        mCurrY = detector.getFocusY();
        mCurrSpan = detector.getCurrentSpan();
        mCurrAngle = detector.getCurrentAngle();
        mView.invalidate();
        return true;
    }

    @Override
    public boolean onScaleRotateBegin(ScaleRotateGestureDetector detector) {
        mIsDetecting = true;
        return true;
    }

    @Override
    public void onScaleRotateEnd(ScaleRotateGestureDetector detector) {
        mIsDetecting = false;
        mView.invalidate();
    }
}
