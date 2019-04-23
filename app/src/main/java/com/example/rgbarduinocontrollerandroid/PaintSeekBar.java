package com.example.rgbarduinocontrollerandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class PaintSeekBar extends SeekBar {

    public PaintSeekBar (Context context) {
        super(context);
    }

    public PaintSeekBar (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PaintSeekBar (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        int thumb_x = (int) (( (double)this.getProgress()/this.getMax() ) * (double)this.getWidth());
        if(this.getProgress() < this.getMax()/4)
            thumb_x += 20;
        else if(this.getProgress() > (this.getMax()/4)*3)
            thumb_x -= 100;

        float middle = (float) (this.getHeight());

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(60);
        c.drawText(""+this.getProgress(), thumb_x, middle, paint);
    }
}
