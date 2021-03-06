package de.avalax.fitbuddy.presentation.workout.swipeBar;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class SwipeBarOnGestureListener extends GestureDetector.SimpleOnGestureListener {
    private View view;
    private int maxIncrement;
    private int minDistance;
    private int velocity;

    public SwipeBarOnGestureListener(int maxIncrement, View view, int minDistance, int velocity) {
        this.maxIncrement = maxIncrement;
        this.view = view;
        this.minDistance = minDistance;
        this.velocity = velocity;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        onFlingEvent(1);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent startEvent, MotionEvent endEvent, float x, float y) {
        float absY = Math.abs(y);
        float distance = startEvent.getY() - endEvent.getY();
        int scaledBarHeight = view.getHeight() / 2;

        if (distance > minDistance && absY > velocity) {
            onFlingEvent(calculateMoved(distance, scaledBarHeight));
            return true;
        } else if (-distance > minDistance && absY > minDistance) {
            onFlingEvent(-calculateMoved(distance, scaledBarHeight));
            return true;
        }
        return false;
    }


    private int calculateMoved(float moved, int height) {
        if (Math.abs(moved) + minDistance >= height) {
            return maxIncrement;
        }
        return 1;
    }

    public abstract void onFlingEvent(int moved);
}
