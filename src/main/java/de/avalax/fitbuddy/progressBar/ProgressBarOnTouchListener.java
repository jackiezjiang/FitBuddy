package de.avalax.fitbuddy.progressBar;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public abstract class ProgressBarOnTouchListener implements View.OnTouchListener {
    private final GestureDetector gdt;

    public ProgressBarOnTouchListener(WindowManager windowManager) {
        gdt = new GestureDetector(new ProgressBarGestureListener(windowManager));
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        gdt.onTouchEvent(event);
        return true;
    }

    private final class ProgressBarGestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 60;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;
        private final WindowManager windowManager;

        public ProgressBarGestureListener(WindowManager windowManager) {
            this.windowManager = windowManager;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float swipeDistance = e1.getY() - e2.getY();
            Log.d("e1 size", String.valueOf(windowManager.getDefaultDisplay().getHeight()));
            Log.d("swipeDistance", String.valueOf(swipeDistance));
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                onFlingEvent(1);
                return true;
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                onFlingEvent(-1);
                return true;
            }
            return false;
        }


    }

    public abstract void onFlingEvent(int moved);
}