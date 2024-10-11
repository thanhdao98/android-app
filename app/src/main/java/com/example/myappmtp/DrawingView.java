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
    private Path drawPath;
    private Paint drawPaint, erasePaint, canvasPaint;
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    private float brushSize = 10;  // Size for drawing
    private float eraserSize = 50; // Size for erasing
    private static final Stack<Bitmap> undoStack = new Stack<>();
    private static final Stack<Bitmap> redoStack = new Stack<>();
    private boolean erase = false;

    // Bitmap for background image
    private Bitmap backgroundBitmap;

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
        if (backgroundBitmap != null) {
            int bitmapWidth = backgroundBitmap.getWidth();
            int bitmapHeight = backgroundBitmap.getHeight();
            float viewWidth = getWidth();
            float viewHeight = getHeight();

            // Tính toán tỷ lệ
            float scale = Math.min(viewWidth / bitmapWidth, viewHeight / bitmapHeight);
            int newWidth = (int) (bitmapWidth * scale);
            int newHeight = (int) (bitmapHeight * scale);

            // Tính toán vị trí để căn giữa
            int left = (int) ((viewWidth - newWidth) / 2);
            int top = (int) ((viewHeight - newHeight) / 2);

            // Vẽ hình ảnh với kích thước mới
            canvas.drawBitmap(backgroundBitmap, null, new Rect(left, top, left + newWidth, top + newHeight), null);
        }

        // Vẽ canvas và path
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, erase ? erasePaint : drawPaint);
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

    // Phương thức để thiết lập hình nền
    public void setBackgroundBitmap(Bitmap bitmap) {
        this.backgroundBitmap = bitmap;
        invalidate(); // Vẽ lại view để hiển thị hình nền
    }


    // Phương thức để tải hình ảnh từ Uri
    public void loadImage(Uri imageUri) {
        try {
            // Chuyển đổi URI thành Bitmap
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Lấy kích thước của DrawingView
            int viewWidth = getWidth();
            int viewHeight = getHeight();

            // Tính tỷ lệ ảnh và tỷ lệ của DrawingView
            float imageRatio = (float) bitmap.getWidth() / bitmap.getHeight();
            float viewRatio = (float) viewWidth / viewHeight;

            int newWidth, newHeight;

            // Nếu tỷ lệ ảnh lớn hơn tỷ lệ của DrawingView, sử dụng chiều rộng của DrawingView
            if (imageRatio > viewRatio) {
                newWidth = viewWidth;
                newHeight = Math.round(viewWidth / imageRatio);
            } else { // Nếu tỷ lệ của DrawingView lớn hơn hoặc bằng tỷ lệ ảnh, sử dụng chiều cao của DrawingView
                newHeight = viewHeight;
                newWidth = Math.round(viewHeight * imageRatio);
            }

            // Thay đổi kích thước ảnh để phù hợp với DrawingView mà không bị méo
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // Đặt hình nền
            setBackgroundBitmap(resizedBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    }


