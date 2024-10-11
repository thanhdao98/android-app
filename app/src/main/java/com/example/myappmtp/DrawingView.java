package com.example.myappmtp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Stack;

public class DrawingView extends View {

    // Global Variables
    private Path drawPath;
    private Paint drawPaint, erasePaint, canvasPaint;
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    private float brushSize = 10;  // Size for drawing
    private float eraserSize = 50; // Size for erasing
    private static final Stack<Bitmap> undoStack = new Stack<>();
    private static final Stack<Bitmap> redoStack = new Stack<>();
    private boolean erase = false;

    // Initialization
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();

        // Setup draw paint for drawing
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        // Setup erase paint for erasing
        erasePaint = new Paint();
        erasePaint.setAntiAlias(true);
        erasePaint.setStrokeWidth(brushSize);
        erasePaint.setStyle(Paint.Style.STROKE);
        erasePaint.setStrokeJoin(Paint.Join.ROUND);
        erasePaint.setStrokeCap(Paint.Cap.ROUND);
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));  // Set to erase

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, erase ? erasePaint : drawPaint);  // Use erase paint if erase mode is on
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (erase) {
                    // Save the current state before erasing
                    saveCanvasState();
                    // Directly use erase paint for erasing
                    drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                } else {
                    saveCanvasState();  // Save current canvas state for drawing
                    drawPath.moveTo(touchX, touchY);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (erase) {
                    // Continue erasing
                    drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                } else {
                    drawPath.lineTo(touchX, touchY);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (erase) {
                    // End erasing
                    drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                } else {
                    drawPath.lineTo(touchX, touchY);
                    drawCanvas.drawPath(drawPath, drawPaint);  // Draw with appropriate paint
                }
                drawPath.reset();
                break;

            default:
                return false;
        }

        invalidate();
        return true;
    }

    // Save canvas state before changes (used for undo/redo)
    private void saveCanvasState() {
        Bitmap saveBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
        undoStack.push(saveBitmap);
        redoStack.clear();  // Clear redo stack when a new action occurs
    }

    // Undo method
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true));
            canvasBitmap = undoStack.pop();
            drawCanvas.setBitmap(canvasBitmap);  // Update drawCanvas with new bitmap
            invalidate();
        }
    }

    // Redo method
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true));
            canvasBitmap = redoStack.pop();
            drawCanvas.setBitmap(canvasBitmap);  // Update drawCanvas with new bitmap
            invalidate();
        }
    }

    // Set erase mode
    public void setErase(boolean isErase) {
        erase = isErase;
    }

    // Set brush size
    public void setBrushSize(float newSize) {
        brushSize = newSize;
        drawPaint.setStrokeWidth(brushSize);
        erasePaint.setStrokeWidth(brushSize);  // Update erase paint size
    }

    // Set eraser size
    public void setEraserSize(float newSize) {
        eraserSize = newSize;
    }
}
