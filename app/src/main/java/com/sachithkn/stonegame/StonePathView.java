package com.sachithkn.stonegame;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class StonePathView extends View {

    Context context;

    public StonePathView(Context context) {
        super(context);
        this.context = context;
    }

    public StonePathView(Context context,AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public StonePathView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }
/*

    public StonePathView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }
*/

    @Override
    protected void onDraw(Canvas canvas) {
        ((MainActivity)context).onDraw(canvas);
        super.onDraw(canvas);
    }
}
