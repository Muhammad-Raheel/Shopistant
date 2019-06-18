package com.example.zar.shopistant;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Zar on 10/12/2017.
 */

public class RouteView extends View {

    Paint paint;
    Path path;
    int framesPerSecond = 60;
    long animationDuration = 10000;
    Matrix matrix = new Matrix();
    long startTime;
    public RouteView(Context context) {
        super(context);
        init();
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();

    }

    public RouteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();
        init();
    }

    public RouteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);

        path = new Path();
        path.moveTo(50, 50);
        path.lineTo(50, 500);
        path.lineTo(200, 500);
        path.lineTo(200, 300);
        path.lineTo(350, 300);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long elapsedTime = System.currentTimeMillis() - startTime;

        /*matrix.postRotate(30 * elapsedTime/1000);        // rotate 30° every second
        matrix.postTranslate(100 * elapsedTime/1000, 0); // move 100 pixels to the right
        // other transformations...

        canvas.concat(matrix);*/
        canvas.drawPath(path, paint);
        if(elapsedTime < animationDuration)
            this.postInvalidateDelayed( 1000 / framesPerSecond);

    }
}
