package com.example.myappmtp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomDrawingView extends View {
    private Paint drawPaint;
    private Path drawPath;
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;

    public CustomDrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPaint = new Paint();
        drawPaint.setColor(0xFF000000); // Màu đen
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(10);
        drawPaint.setAntiAlias(true);
        drawPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, null);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setBrushColor(int color) {
        drawPaint.setColor(color);
    }

    public void clearCanvas() {
        drawCanvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
        invalidate();
    }
}