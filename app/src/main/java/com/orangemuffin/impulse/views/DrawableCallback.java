package com.orangemuffin.impulse.views;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import java.lang.ref.WeakReference;


/* Created by OrangeMuffin on 2019-04-10 */
public class DrawableCallback implements Drawable.Callback {
    private WeakReference<TextView> mViewWeakReference;

    public DrawableCallback(TextView textView) {
        mViewWeakReference = new WeakReference<>(textView);
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        if (mViewWeakReference.get() != null) {
            mViewWeakReference.get().invalidate();
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (mViewWeakReference.get() != null) {
            mViewWeakReference.get().postDelayed(what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (mViewWeakReference.get() != null) {
            mViewWeakReference.get().removeCallbacks(what);
        }
    }
}
