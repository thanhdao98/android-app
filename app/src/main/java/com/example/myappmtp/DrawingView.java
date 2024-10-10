package com.example.myappmtp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.Stack;

public class DrawingView extends View {
    private Path drawPath, circlePath, mPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint, circlePaint, mBitmapPaint;
    //initial color
    private int paintColor = 0xFF000000, paintAlpha = 255;
    //canvas
    private static Canvas drawCanvas;

    //canvas bitmap
    private static Bitmap canvasBitmap;
    private Bitmap im;
    //brush sizes
    private float brushSize, lastBrushSize;
    //erase flag
    private boolean erase = false;

    // Stack for undo and redo
    private static final Stack<Bitmap> undoStack = new Stack<>();
    private static final Stack<Bitmap> redoStack = new Stack<>();
    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }
    //setup drawing
    private void setupDrawing(){
        //prepare for drawing and setup paint stroke properties
        brushSize = 5;
        lastBrushSize = brushSize;
        drawPath = new Path();
        mPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.parseColor("#f4802b"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(1f);
    }
    //size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvasBitmap.eraseColor(Color.TRANSPARENT);
        drawCanvas = new Canvas(canvasBitmap);
        //  drawCanvas.drawColor(Color.BLACK);
    }
    //draw the view - will be called after touch event
    @Override
    protected void onDraw(Canvas canvas) {
        if(erase) {
            canvas.drawBitmap(canvasBitmap, 0, 0, mBitmapPaint);
            //                  canvas.drawPath(mPath, drawPaint);
            canvas.drawPath(circlePath, circlePaint);
        } else {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
            canvas.drawPath(drawPath, drawPaint);
        }
    }
    float mX, mY;
    private static final float TOUCH_TOLERANCE = 1;
    //register user touches as drawing action

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Save the canvas state before drawing
                saveCanvasState();  // Save the current canvas state into undoStack

                if (erase) {
                    mPath.reset();
                    mPath.moveTo(touchX, touchY);
                    mX = touchX;
                    mY = touchY;
                    drawPaint.setStyle(Paint.Style.FILL);
                    drawPaint.setColor(Color.BLACK);
                    drawCanvas.drawCircle(touchX, touchY, 25, drawPaint);
                } else {
                    drawPath.moveTo(touchX, touchY);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (erase) {
                    float dx = Math.abs(touchX - mX);
                    float dy = Math.abs(touchY - mY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mPath.quadTo(mX, mY, (touchX + mX) / 2, (touchY + mY) / 2);
                        mX = touchX;
                        mY = touchY;
                        circlePath.reset();
                        circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
                    }
                    drawPaint.setStyle(Paint.Style.FILL);
                    drawPaint.setColor(Color.BLACK);
                    drawCanvas.drawCircle(touchX, touchY, 25, drawPaint);
                } else {
                    drawPath.lineTo(touchX, touchY);
                }
                break;

            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                circlePath.reset();
                // Save the canvas state after finishing drawing
                saveCanvasState(); // You can add saving the state here if needed
                drawPath.reset();
                break;

            default:
                return false;
        }
        //redraw
        invalidate();
        return true;
    }


    // Method to check if undoStack is empty
    public boolean isUndoStackEmpty() {
        return undoStack.isEmpty();
    }

    // Method to check if redoStack is empty
    public boolean isRedoStackEmpty() {
        return redoStack.isEmpty();
    }

    // Save the current canvas state into undoStack
    private void saveCanvasState() {
        Bitmap saveBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true);
        undoStack.push(saveBitmap);
        redoStack.clear();  // Clear redoStack when there is a new operation
    }

    // Undo function
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true));
            canvasBitmap = undoStack.pop();
            drawCanvas = new Canvas(canvasBitmap);
            invalidate();
        }
    }

    // Redo function
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true));
            canvasBitmap = redoStack.pop();
            drawCanvas = new Canvas(canvasBitmap);
            invalidate();
        }
    }

    //update color
    public void setColor(String newColor){
        invalidate();
        //check whether color value or pattern name
        if(newColor.startsWith("#")){
            paintColor = Color.parseColor(newColor);
            drawPaint.setColor(paintColor);
            drawPaint.setShader(null);
        } else {
            //pattern
            int patternID = getResources().getIdentifier(newColor, "drawable", "com.thots");
            //decode
            Bitmap patternBMP = BitmapFactory.decodeResource(getResources(), patternID);
            //create shader
            BitmapShader patternBMPshader = new BitmapShader(patternBMP,Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            //color and shader
            drawPaint.setColor(0xFFFFFFFF);
            drawPaint.setShader(patternBMPshader);
        }
    }
    //set brush size
    public void setBrushSize(float newSize){
        brushSize=newSize;
        drawPaint.setStrokeWidth(brushSize);
    }
    //get and set last brush size
    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }
    //set erase true or false
    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
            drawPaint.setColor(paintColor);
            drawPaint.setStyle(Paint.Style.STROKE);
        }
    }
    //start new drawing
    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
    //return current alpha
    public int getPaintAlpha(){
        return Math.round((float)paintAlpha/255*100);
    }
    //set alpha
    public void setPaintAlpha(int newAlpha){
        paintAlpha=Math.round((float)newAlpha/100*255);
        drawPaint.setColor(paintColor);
        drawPaint.setAlpha(paintAlpha);
    }
}
