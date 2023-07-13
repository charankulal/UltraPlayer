package com.example.videoplayer;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class OnSwipeListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    public OnSwipeListener(Context context){
        gestureDetector=new GestureDetector(context,new GestureListener());
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public final class GestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            onSingleTouch();
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            onDoubleTouch();
            return super.onDoubleTap(e);
        }
    }
    public void onDoubleTouch()
    {

    }
    public void onSingleTouch(){

    }

}
