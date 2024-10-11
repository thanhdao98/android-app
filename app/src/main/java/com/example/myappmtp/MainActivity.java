package com.example.myappmtp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private static final int PICK_IMAGE = 1;
    private Button btnComplete; // Button to complete the action
    private LinearLayout topMenu, bottomMenu; // Layouts for top and bottom menus
    private ImageView imgPen, imgErase; // Icons for pen and eraser tools
    private ImageButton btnUndo, btnRedo; // Buttons for undo and redo actions

    // Custom drawing view
    private DrawingView mDrawingView;
    private boolean isMenuVisible = true; // Flag to check if menus are visible

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge layout
        setContentView(R.layout.activity_main); // Set content view

        initializeUIComponents(); // Initialize UI components
        setupEventListeners(); // Setup event listeners

        // Set padding to accommodate system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // Call loadImage method to load the image
                            mDrawingView.loadImage(imageUri);
                        }
                    }
                }
        );
    }

    // Open image chooser
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Set image to DrawingView
    private void setImageToDrawingView(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            mDrawingView.setBackgroundBitmap(bitmap); // Call method to set background in DrawingView
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes UI components for the activity
     */
    private void initializeUIComponents() {
        mDrawingView = findViewById(R.id.drawingView);
        imgPen = findViewById(R.id.imageButtonPencil);
        imgErase = findViewById(R.id.imageButtonEraser);
        btnUndo = findViewById(R.id.imageButtonUndo);
        btnRedo = findViewById(R.id.imageButtonRedo);
        btnComplete = findViewById(R.id.buttonComplete);
        topMenu = findViewById(R.id.topMenu);
        bottomMenu = findViewById(R.id.bottomMenu);

        // Set up the image select button
        Button selectImageButton = findViewById(R.id.buttonSelectImage);
        selectImageButton.setOnClickListener(v -> openImageChooser());
    }

    /**
     * Sets up event listeners for UI components
     */
    private void setupEventListeners() {
        // Event listeners for pen and eraser tools
        imgPen.setOnClickListener(v -> selectPen());
        imgErase.setOnClickListener(v -> selectEraser());

        // Event listeners for undo and redo actions
        btnUndo.setOnClickListener(v -> mDrawingView.undo());
        btnRedo.setOnClickListener(v -> mDrawingView.redo());

        // Event listener to toggle the visibility of the menus
        findViewById(R.id.main).setOnClickListener(v -> toggleMenus());

        // Event listener for the complete button
        btnComplete.setOnClickListener(v -> {
            ConfirmationDialog.show(MainActivity.this, (dialog, which) -> {
                // Handle user response in the confirmation dialog
            });
        });
    }

    /**
     * Selects the pen tool and applies the relevant settings
     */
    private void selectPen() {
        imgPen.setImageResource(R.drawable.pencil);
        imgErase.setImageResource(R.drawable.eraser);
        mDrawingView.setErase(false); // Set drawing mode

        // Start shake animation on the pen icon
        startAnimation(imgPen);
    }

    /**
     * Selects the eraser tool and applies the relevant settings
     */
    private void selectEraser() {
        imgErase.setImageResource(R.drawable.eraser);
        imgPen.setImageResource(R.drawable.pencil);
        mDrawingView.setErase(true); // Set erase mode

        // Start shake animation on the eraser icon
        startAnimation(imgErase);
    }

    /**
     * Starts the shake animation on the given view
     */
    private void startAnimation(View view) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shake);
    }

    /**
     * Toggles the visibility of the top and bottom menus
     */
    private void toggleMenus() {
        if (isMenuVisible) {
            topMenu.setVisibility(View.GONE); // Hide menus
            bottomMenu.setVisibility(View.GONE);
        } else {
            topMenu.setVisibility(View.VISIBLE); // Show menus
            bottomMenu.setVisibility(View.VISIBLE);
        }
        isMenuVisible = !isMenuVisible; // Update visibility flag
    }
}
