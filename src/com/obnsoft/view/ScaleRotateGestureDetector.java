/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obnsoft.view;

import android.content.Context;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Detects transformation gestures involving more than one pointer ("multitouch")
 * using the supplied {@link MotionEvent}s. The OnScaleRotateGestureListener
 * callback will notify users when a particular gesture event has occurred.
 * This class should only be used with {@link MotionEvent}s reported via touch.
 *
 * To use this class:
 * <ul>
 *  <li>Create an instance of the {@code ScaleRotateGestureDetector} for your
 *      {@link View}
 *  <li>In the {@link View#onTouchEvent(MotionEvent)} method ensure you call
 *          {@link #onTouchEvent(MotionEvent)}. The methods defined in your
 *          callback will be executed when the events occur.
 * </ul>
 */
public class ScaleRotateGestureDetector {
    private static final String TAG = "ScaleRotateGestureDetector";

    /**
     * The listener for receiving notifications when gestures occur.
     * If you want to listen for all the different gestures then implement
     * this interface. If you only want to listen for a subset it might
     * be easier to extend {@link SimpleOnScaleRotateGestureListener}.
     *
     * An application will receive events in the following order:
     * <ul>
     *  <li>One {@link OnScaleGestureListener#onScaleRotateBegin(ScaleRotateGestureDetector)}
     *  <li>Zero or more {@link OnScaleGestureListener#onScaleRotate(ScaleRotateGestureDetector)}
     *  <li>One {@link OnScaleGestureListener#onScaleRotateEnd(ScaleRotateGestureDetector)}
     * </ul>
     */
    public interface OnScaleRotateGestureListener {
        /**
         * Responds to scaling and rotating events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *          retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         *          as handled. If an event was not handled, the detector
         *          will continue to accumulate movement until an event is
         *          handled. This can be useful if an application, for example,
         *          only wants to update scaling factors if the change is
         *          greater than 0.01.
         */
        public boolean onScaleRotate(ScaleRotateGestureDetector detector);

        /**
         * Responds to the beginning of a scaling  and rotating gesture.
         * Reported by new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *          retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         *          this gesture. For example, if a gesture is beginning
         *          with a focal point outside of a region where it makes
         *          sense, onScaleBegin() may return false to ignore the
         *          rest of the gesture.
         */
        public boolean onScaleRotateBegin(ScaleRotateGestureDetector detector);

        /**
         * Responds to the end of a scale and rotating gesture. Reported by
         * existing pointers going up.
         *
         * Once a scale has ended, {@link ScaleRotateGestureDetector#getFocusX()}
         * and {@link ScaleRotateGestureDetector#getFocusY()} will return the location
         * of the pointer remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *          retrieve extended info about event state.
         */
        public void onScaleRotateEnd(ScaleRotateGestureDetector detector);
    }

    /**
     * A convenience class to extend when you only want to listen for a subset
     * of scaling-related events. This implements all methods in
     * {@link OnScaleGestureListener} but does nothing.
     * {@link OnScaleGestureListener#onScaleRotate(ScaleRotateGestureDetector)} returns
     * {@code false} so that a subclass can retrieve the accumulated scale
     * factor in an overridden onScaleEnd.
     * {@link OnScaleGestureListener#onScaleRotateBegin(ScaleRotateGestureDetector)} returns
     * {@code true}.
     */
    public static class SimpleOnScaleRotateGestureListener implements OnScaleRotateGestureListener {

        public boolean onScaleRotate(ScaleRotateGestureDetector detector) {
            return false;
        }

        public boolean onScaleRotateBegin(ScaleRotateGestureDetector detector) {
            return true;
        }

        public void onScaleRotateEnd(ScaleRotateGestureDetector detector) {
            // Intentionally empty
        }
    }

    /**
     * This value is the threshold ratio between our previous combined pressure
     * and the current combined pressure. We will only fire an onScale event if
     * the computed ratio between the current and previous event pressures is
     * greater than this value. When pressure decreases rapidly between events
     * the position values can often be imprecise, as it usually indicates
     * that the user is in the process of lifting a pointer off of the device.
     * Its value was tuned experimentally.
     */
    private static final float PRESSURE_THRESHOLD = 0.67f;
    private static final double ANGLE_UNDEFINED = -100.0;

    //private final Context mContext;
    private final OnScaleRotateGestureListener mListener;
    private boolean mGestureInProgress;

    private MotionEvent mPrevEvent;
    private MotionEvent mCurrEvent;

    private float mFocusX;
    private float mFocusY;
    private float mPrevFingerDiffX;
    private float mPrevFingerDiffY;
    private float mCurrFingerDiffX;
    private float mCurrFingerDiffY;
    private float mCurrLen;
    private float mPrevLen;
    private double mCurrAng;
    private double mPrevAng;
    private float mScaleFactor;
    private double mAngleDelta;
    private float mCurrPressure;
    private float mPrevPressure;
    private long mTimeDelta;

    private boolean mInvalidGesture;

    // Pointer IDs currently responsible for the two fingers controlling the gesture
    private int mActiveId0;
    private int mActiveId1;
    private boolean mActive0MostRecent;

    public ScaleRotateGestureDetector(Context context, OnScaleRotateGestureListener listener) {
        //mContext = context;
        mListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            reset(); // Start fresh
        }

        boolean handled = true;
        if (mInvalidGesture) {
            handled = false;
        } else if (!mGestureInProgress) {
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mActiveId0 = event.getPointerId(0);
                    mActive0MostRecent = true;
                }
                break;

                case MotionEvent.ACTION_UP:
                    reset();
                    break;

                case MotionEvent.ACTION_POINTER_DOWN: {
                    // We have a new multi-finger gesture
                    if (mPrevEvent != null) mPrevEvent.recycle();
                    mPrevEvent = MotionEvent.obtain(event);
                    mTimeDelta = 0;

                    int index1 = event.getActionIndex();
                    int index0 = event.findPointerIndex(mActiveId0);
                    mActiveId1 = event.getPointerId(index1);
                    if (index0 < 0 || index0 == index1) {
                        // Probably someone sending us a broken event stream.
                        index0 = findNewActiveIndex(event, mActiveId1, -1);
                        mActiveId0 = event.getPointerId(index0);
                    }
                    mActive0MostRecent = false;

                    setContext(event);

                    mGestureInProgress = mListener.onScaleRotateBegin(this);
                    break;
                }
            }
        } else {
            // Transform gesture in progress - attempt to handle it
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN: {
                    // End the old gesture and begin a new one with the most recent two fingers.
                    mListener.onScaleRotateEnd(this);
                    final int oldActive0 = mActiveId0;
                    final int oldActive1 = mActiveId1;
                    reset();

                    mPrevEvent = MotionEvent.obtain(event);
                    mActiveId0 = mActive0MostRecent ? oldActive0 : oldActive1;
                    mActiveId1 = event.getPointerId(event.getActionIndex());
                    mActive0MostRecent = false;

                    int index0 = event.findPointerIndex(mActiveId0);
                    if (index0 < 0 || mActiveId0 == mActiveId1) {
                        // Probably someone sending us a broken event stream.
                        Log.e(TAG, "Got action " + action +
                                " with bad state while a gesture was in progress. " +
                                "Did you forget to pass an event to " +
                                "ScaleRotateGestureDetector#onTouchEvent?");
                        index0 = findNewActiveIndex(event, mActiveId1, -1);
                        mActiveId0 = event.getPointerId(index0);
                    }

                    setContext(event);

                    mGestureInProgress = mListener.onScaleRotateBegin(this);
                }
                break;

                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerCount = event.getPointerCount();
                    final int actionIndex = event.getActionIndex();
                    final int actionId = event.getPointerId(actionIndex);

                    boolean gestureEnded = false;
                    if (pointerCount > 2) {
                        if (actionId == mActiveId0) {
                            final int newIndex = findNewActiveIndex(event, mActiveId1, actionIndex);
                            if (newIndex >= 0) {
                                mListener.onScaleRotateEnd(this);
                                mActiveId0 = event.getPointerId(newIndex);
                                mActive0MostRecent = true;
                                mPrevEvent = MotionEvent.obtain(event);
                                setContext(event);
                                mGestureInProgress = mListener.onScaleRotateBegin(this);
                            } else {
                                gestureEnded = true;
                            }
                        } else if (actionId == mActiveId1) {
                            final int newIndex = findNewActiveIndex(event, mActiveId0, actionIndex);
                            if (newIndex >= 0) {
                                mListener.onScaleRotateEnd(this);
                                mActiveId1 = event.getPointerId(newIndex);
                                mActive0MostRecent = false;
                                mPrevEvent = MotionEvent.obtain(event);
                                setContext(event);
                                mGestureInProgress = mListener.onScaleRotateBegin(this);
                            } else {
                                gestureEnded = true;
                            }
                        }
                        mPrevEvent.recycle();
                        mPrevEvent = MotionEvent.obtain(event);
                        setContext(event);
                    } else {
                        gestureEnded = true;
                    }

                    if (gestureEnded) {
                        // Gesture ended
                        setContext(event);

                        // Set focus point to the remaining finger
                        final int activeId = actionId == mActiveId0 ? mActiveId1 : mActiveId0;
                        final int index = event.findPointerIndex(activeId);
                        mFocusX = event.getX(index);
                        mFocusY = event.getY(index);

                        mListener.onScaleRotateEnd(this);
                        reset();
                        mActiveId0 = activeId;
                        mActive0MostRecent = true;
                    }
                }
                break;

                case MotionEvent.ACTION_CANCEL:
                    mListener.onScaleRotateEnd(this);
                    reset();
                    break;

                case MotionEvent.ACTION_UP:
                    reset();
                    break;

                case MotionEvent.ACTION_MOVE: {
                    setContext(event);

                    // Only accept the event if our relative pressure is within
                    // a certain limit - this can help filter shaky data as a
                    // finger is lifted.
                    if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                        final boolean updatePrevious = mListener.onScaleRotate(this);

                        if (updatePrevious) {
                            mPrevEvent.recycle();
                            mPrevEvent = MotionEvent.obtain(event);
                        }
                    }
                }
                break;
            }
        }

        return handled;
    }

    private int findNewActiveIndex(MotionEvent ev, int otherActiveId, int removedPointerIndex) {
        final int pointerCount = ev.getPointerCount();

        // It's ok if this isn't found and returns -1, it simply won't match.
        final int otherActiveIndex = ev.findPointerIndex(otherActiveId);

        // Pick a new id and update tracking state.
        for (int i = 0; i < pointerCount; i++) {
            if (i != removedPointerIndex && i != otherActiveIndex) {
                return i;
            }
        }
        return -1;
    }

    private void setContext(MotionEvent curr) {
        if (mCurrEvent != null) {
            mCurrEvent.recycle();
        }
        mCurrEvent = MotionEvent.obtain(curr);

        mCurrLen = -1;
        mCurrAng = ANGLE_UNDEFINED;
        mPrevLen = -1;
        mPrevAng = ANGLE_UNDEFINED;
        mScaleFactor = -1;

        final MotionEvent prev = mPrevEvent;

        final int prevIndex0 = prev.findPointerIndex(mActiveId0);
        final int prevIndex1 = prev.findPointerIndex(mActiveId1);
        final int currIndex0 = curr.findPointerIndex(mActiveId0);
        final int currIndex1 = curr.findPointerIndex(mActiveId1);

        if (prevIndex0 < 0 || prevIndex1 < 0 || currIndex0 < 0 || currIndex1 < 0) {
            mInvalidGesture = true;
            Log.e(TAG, "Invalid MotionEvent stream detected.", new Throwable());
            if (mGestureInProgress) {
                mListener.onScaleRotateEnd(this);
            }
            return;
        }

        final float px0 = prev.getX(prevIndex0);
        final float py0 = prev.getY(prevIndex0);
        final float px1 = prev.getX(prevIndex1);
        final float py1 = prev.getY(prevIndex1);
        final float cx0 = curr.getX(currIndex0);
        final float cy0 = curr.getY(currIndex0);
        final float cx1 = curr.getX(currIndex1);
        final float cy1 = curr.getY(currIndex1);

        final float pvx = px1 - px0;
        final float pvy = py1 - py0;
        final float cvx = cx1 - cx0;
        final float cvy = cy1 - cy0;
        mPrevFingerDiffX = pvx;
        mPrevFingerDiffY = pvy;
        mCurrFingerDiffX = cvx;
        mCurrFingerDiffY = cvy;

        mFocusX = cx0 + cvx * 0.5f;
        mFocusY = cy0 + cvy * 0.5f;
        mTimeDelta = curr.getEventTime() - prev.getEventTime();
        mCurrPressure = curr.getPressure(currIndex0) + curr.getPressure(currIndex1);
        mPrevPressure = prev.getPressure(prevIndex0) + prev.getPressure(prevIndex1);
    }

    private void reset() {
        if (mPrevEvent != null) {
            mPrevEvent.recycle();
            mPrevEvent = null;
        }
        if (mCurrEvent != null) {
            mCurrEvent.recycle();
            mCurrEvent = null;
        }
        mGestureInProgress = false;
        mActiveId0 = -1;
        mActiveId1 = -1;
        mInvalidGesture = false;
    }

    /**
     * Returns {@code true} if a two-finger scale gesture is in progress.
     * @return {@code true} if a scale gesture is in progress, {@code false} otherwise.
     */
    public boolean isInProgress() {
        return mGestureInProgress;
    }

    /**
     * Get the X coordinate of the current gesture's focal point.
     * If a gesture is in progress, the focal point is directly between
     * the two pointers forming the gesture.
     * If a gesture is ending, the focal point is the location of the
     * remaining pointer on the screen.
     * If {@link #isInProgress()} would return false, the result of this
     * function is undefined.
     *
     * @return X coordinate of the focal point in pixels.
     */
    public float getFocusX() {
        return mFocusX;
    }

    /**
     * Get the Y coordinate of the current gesture's focal point.
     * If a gesture is in progress, the focal point is directly between
     * the two pointers forming the gesture.
     * If a gesture is ending, the focal point is the location of the
     * remaining pointer on the screen.
     * If {@link #isInProgress()} would return false, the result of this
     * function is undefined.
     *
     * @return Y coordinate of the focal point in pixels.
     */
    public float getFocusY() {
        return mFocusY;
    }

    /**
     * Return the current distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Distance between pointers in pixels.
     */
    public float getCurrentSpan() {
        if (mCurrLen == -1) {
            final float cvx = mCurrFingerDiffX;
            final float cvy = mCurrFingerDiffY;
            mCurrLen = FloatMath.sqrt(cvx*cvx + cvy*cvy);
        }
        return mCurrLen;
    }

    /**
     * Return the current x distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Distance between pointers in pixels.
     */
    public float getCurrentSpanX() {
        return mCurrFingerDiffX;
    }

    /**
     * Return the current y distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Distance between pointers in pixels.
     */
    public float getCurrentSpanY() {
        return mCurrFingerDiffY;
    }

    /**
     * Return the current angle between the two pointers forming the
     * gesture in progress.
     *
     * @return Angle between pointers.
     */
    public double getCurrentAngle() {
        if (mCurrAng == ANGLE_UNDEFINED) {
            mCurrAng = Math.atan2(mCurrFingerDiffX, mCurrFingerDiffY);
        }
        return mCurrAng;
    }

    /**
     * Return the previous distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Previous distance between pointers in pixels.
     */
    public float getPreviousSpan() {
        if (mPrevLen == -1) {
            final float pvx = mPrevFingerDiffX;
            final float pvy = mPrevFingerDiffY;
            mPrevLen = FloatMath.sqrt(pvx*pvx + pvy*pvy);
        }
        return mPrevLen;
    }

    /**
     * Return the previous x distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Previous distance between pointers in pixels.
     */
    public float getPreviousSpanX() {
        return mPrevFingerDiffX;
    }

    /**
     * Return the previous y distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Previous distance between pointers in pixels.
     */
    public float getPreviousSpanY() {
        return mPrevFingerDiffY;
    }

    /**
     * Return the previous angle between the two pointers forming the
     * gesture in progress.
     *
     * @return Previous angle between pointers.
     */
    public double getPreviousAngle() {
        if (mPrevAng == ANGLE_UNDEFINED) {
            mPrevAng = Math.atan2(mPrevFingerDiffX, mPrevFingerDiffY);
        }
        return mPrevAng;
    }

    /**
     * Return the scaling factor from the previous scale event to the current
     * event. This value is defined as
     * ({@link #getCurrentSpan()} / {@link #getPreviousSpan()}).
     *
     * @return The current scaling factor.
     */
    public float getScaleFactor() {
        if (mScaleFactor == -1) {
            mScaleFactor = getCurrentSpan() / getPreviousSpan();
        }
        return mScaleFactor;
    }

    /**
     * Return the angle difference from the previous rotating event to the current
     * event.
     *
     * @return Angle difference since the last rotating event in milliseconds.
     */
    public double getAngleDelta() {
        if (mAngleDelta == ANGLE_UNDEFINED) {
            mAngleDelta = getCurrentAngle() - getPreviousAngle();
            if (mAngleDelta > Math.PI)  mAngleDelta -= Math.PI * 2.0;
            if (mAngleDelta < -Math.PI) mAngleDelta += Math.PI * 2.0;
        }
        return mAngleDelta; // TODO
    }

    /**
     * Return the time difference in milliseconds between the previous
     * accepted scaling event and the current scaling event.
     *
     * @return Time difference since the last scaling event in milliseconds.
     */
    public long getTimeDelta() {
        return mTimeDelta;
    }

    /**
     * Return the event time of the current event being processed.
     *
     * @return Current event time in milliseconds.
     */
    public long getEventTime() {
        return mCurrEvent.getEventTime();
    }
}
