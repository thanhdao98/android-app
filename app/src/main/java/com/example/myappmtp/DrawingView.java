package com.example.myappmtp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Stack;

public class DrawingView extends View {

    // Global Variables
    private Path drawPath; // Path for drawing
    private Paint drawPaint, erasePaint, canvasPaint; // Paint objects for drawing, erasing, and canvas
    private Bitmap canvasBitmap; // Bitmap for the canvas
    private Canvas drawCanvas; // Canvas to draw on the bitmap
    private float brushSize = 10;  // Size for drawing brush
    private float eraserSize = 50; // Size for eraser
    private static final Stack<Bitmap> undoStack = new Stack<>(); // Stack for undo operations
    private static final Stack<Bitmap> redoStack = new Stack<>(); // Stack for redo operations
    private boolean erase = false; // Flag to check if erase mode is active

    // Bitmap for background image
    private Bitmap backgroundBitmap;

    // Initialization
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing(); // Call method to setup drawing properties
    }

    // Setup drawing properties
    private void setupDrawing() {
        drawPath = new Path(); // Initialize drawing path

        // Setup draw paint for drawing
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK); // Set color for drawing
        drawPaint.setAntiAlias(true); // Enable anti-aliasing for smooth edges
        drawPaint.setStrokeWidth(brushSize); // Set brush size
        drawPaint.setStyle(Paint.Style.STROKE); // Set paint style to stroke
        drawPaint.setStrokeJoin(Paint.Join.ROUND); // Set stroke join style
        drawPaint.setStrokeCap(Paint.Cap.ROUND); // Set stroke cap style

        // Setup erase paint for erasing
        erasePaint = new Paint();
        erasePaint.setAntiAlias(true); // Enable anti-aliasing for erase paint
        erasePaint.setStrokeWidth(brushSize); // Set brush size for eraser
        erasePaint.setStyle(Paint.Style.STROKE); // Set paint style to stroke
        erasePaint.setStrokeJoin(Paint.Join.ROUND); // Set stroke join style
        erasePaint.setStrokeCap(Paint.Cap.ROUND); // Set stroke cap style
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));  // Set mode to clear for erasing

        canvasPaint = new Paint(Paint.DITHER_FLAG); // Paint for canvas
    }

    // Called when the size of the view changes
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888); // Create a bitmap for the canvas
        drawCanvas = new Canvas(canvasBitmap); // Create a canvas from the bitmap
    }

    // Draw the canvas and paths
    @Override
    protected void onDraw(Canvas canvas) {
        if (backgroundBitmap != null) {
            // Get dimensions of the background bitmap
            int bitmapWidth = backgroundBitmap.getWidth();
            int bitmapHeight = backgroundBitmap.getHeight();
            float viewWidth = getWidth();
            float viewHeight = getHeight();

            // Calculate scale to fit the bitmap in the view
            float scale = Math.min(viewWidth / bitmapWidth, viewHeight / bitmapHeight);
            int newWidth = (int) (bitmapWidth * scale);
            int newHeight = (int) (bitmapHeight * scale);

            // Calculate position to center the bitmap
            int left = (int) ((viewWidth - newWidth) / 2);
            int top = (int) ((viewHeight - newHeight) / 2);

            // Draw the background bitmap on the canvas
            canvas.drawBitmap(backgroundBitmap, null, new Rect(left, top, left + newWidth, top + newHeight), null);
        }

        // Draw the canvas bitmap and current path
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, erase ? erasePaint : drawPaint); // Use erasePaint if in erase mode
    }

    // Handle touch events for drawing and erasing
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX(); // Get touch X coordinate
        float touchY = event.getY(); // Get touch Y coordinate

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (erase) {
                    // Save the current state before erasing
                    saveCanvasState();
                    // Directly use erase paint for erasing
                    drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                } else {
                    saveCanvasState();  // Save current canvas state for drawing
                    drawPath.moveTo(touchX, touchY); // Move to touch position
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (erase) {
                    // Continue erasing
                    drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                } else {
                    drawPath.lineTo(touchX, touchY); // Draw line to touch position
                }
                break;

            case MotionEvent.ACTION_UP:
                if (erase) {
                    // End erasing
                    drawCanvas.drawCircle(touchX, touchY, eraserSize, erasePaint);
                } else {
                    drawPath.lineTo(touchX, touchY); // Finalize the line
                    drawCanvas.drawPath(drawPath, drawPaint);  // Draw the path with appropriate paint
                }
                drawPath.reset(); // Reset the path for the next drawing
                break;

            default:
                return false; // Handle unrecognized touch events
        }

        invalidate(); // Request to redraw the view
        return true; // Indicate that the event was handled
    }

    // Save canvas state before changes (used for undo/redo)
    private void saveCanvasState() {
        Bitmap saveBitmap = canvasBitmap.copy(Bitmap.Config.ARGB_8888, true); // Copy current canvas bitmap
        undoStack.push(saveBitmap); // Push current state to undo stack
        redoStack.clear();  // Clear redo stack when a new action occurs
    }

    // Undo method
    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true)); // Save current state to redo stack
            canvasBitmap = undoStack.pop(); // Get the last saved state from undo stack
            drawCanvas.setBitmap(canvasBitmap);  // Update drawCanvas with new bitmap
            invalidate(); // Request to redraw the view
        }
    }

    // Redo method
    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(canvasBitmap.copy(Bitmap.Config.ARGB_8888, true)); // Save current state to undo stack
            canvasBitmap = redoStack.pop(); // Get the last saved state from redo stack
            drawCanvas.setBitmap(canvasBitmap);  // Update drawCanvas with new bitmap
            invalidate(); // Request to redraw the view
        }
    }

    // Set erase mode
    public void setErase(boolean isErase) {
        erase = isErase; // Set erase flag
    }

    // Set brush size
    public void setBrushSize(float newSize) {
        brushSize = newSize; // Update brush size
        drawPaint.setStrokeWidth(brushSize); // Update drawing paint size
        erasePaint.setStrokeWidth(brushSize);  // Update eraser paint size
    }

    // Set eraser size
    public void setEraserSize(float newSize) {
        eraserSize = newSize; // Update eraser size
    }

    // Method to set background bitmap
    public void setBackgroundBitmap(Bitmap bitmap) {
        this.backgroundBitmap = bitmap; // Set background bitmap
        invalidate(); // Request to redraw the view to display the background
    }

    // Method to load an image from Uri
    public void loadImage(Uri imageUri) {
        try {
            // Convert URI to Bitmap
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Get the size of the DrawingView
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            // Calculate the aspect ratio of the image and the aspect ratio of the DrawingView
            float imageRatio = (float) bitmap.getWidth() / bitmap.getHeight();
            float viewRatio = (float) viewWidth / viewHeight;

            int newWidth, newHeight;

            // If the image ratio is greater than the DrawingView ratio, use the width of the DrawingView
            if (imageRatio > viewRatio) {
                newWidth = viewWidth;
                newHeight = Math.round(viewWidth / imageRatio);
            } else { // If the DrawingView ratio is greater than or equal to the image ratio, use the height of the DrawingView
                newHeight = viewHeight;
                newWidth = Math.round(viewHeight * imageRatio);
            }

            // Resize the image to fit the DrawingView without distortion
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // Set the background
            setBackgroundBitmap(resizedBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
