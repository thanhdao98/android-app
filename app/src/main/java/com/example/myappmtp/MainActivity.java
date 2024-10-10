package com.example.myappmtp;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity {

    private Button btnComplete; // Button to complete the action
    private LinearLayout topMenu; // Top menu layout
    private LinearLayout bottomMenu; // Bottom menu layout
    private boolean isMenuVisible = true; // Flag to check if menus are visible
    private DrawingView mDrawingView; // Custom drawing view
    private ImageView imgPen, imgErase; // ImageViews for pen and eraser icons

    private ImageButton btnUndo; // Button for undo action
    private ImageButton btnRedo; // Button for redo action

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge layout
        setContentView(R.layout.activity_main); // Set the content view

        initializeObject(); // Initialize UI components
        eventListeners(); // Set up event listeners

        btnComplete = findViewById(R.id.buttonComplete);
        topMenu = findViewById(R.id.topMenu);
        bottomMenu = findViewById(R.id.bottomMenu);

        btnUndo = findViewById(R.id.imageButtonUndo);
        btnRedo = findViewById(R.id.imageButtonRedo);

        // Set padding to accommodate system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set click listener for the undo button
        btnUndo.setOnClickListener(v -> {
            mDrawingView.undo(); // Call undo method
        });

        // Set click listener for the redo button
        btnRedo.setOnClickListener(v -> {
            mDrawingView.redo(); // Call redo method
        });

        // Set click listener for the main view to toggle menus
        findViewById(R.id.main).setOnClickListener(view -> toggleMenus());

        // Set click listener for the complete button
        btnComplete.setOnClickListener(v -> {
            ConfirmationDialog.show(MainActivity.this, (dialog, which) -> {
                // Handle user's response to the confirmation dialog
                // Example: Send image or perform other actions
            });
        });
    }
    private void updateButtonStates() {
        // Update button states based on the drawing view's stack
        btnUndo.setEnabled(!mDrawingView.isUndoStackEmpty()); // Enable/disable undo button
        btnRedo.setEnabled(!mDrawingView.isRedoStackEmpty()); // Enable/disable redo button
    }

    private void initializeObject() {
        // Initialize the drawing view and pen/eraser icons
        mDrawingView = findViewById(R.id.drawingView);
        imgPen = findViewById(R.id.imageButtonPencil);
        imgErase = findViewById(R.id.imageButtonEraser);
    }

    private void eventListeners() {
        // Set up click listeners for the pen and eraser
        imgPen.setOnClickListener(v -> selectPen());
        imgErase.setOnClickListener(v -> selectEraser());
    }

    private void selectPen() {
        // Select the pen tool
        imgPen.setImageResource(R.drawable.pencil);
        imgErase.setImageResource(R.drawable.eraser);
        mDrawingView.setErase(false); // Set drawing mode to false

        // Start shake animation on the pen icon
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        imgPen.startAnimation(shake);
    }

    private void selectEraser() {
        // Select the eraser tool
        imgErase.setImageResource(R.drawable.eraser);
        imgPen.setImageResource(R.drawable.pencil);
        mDrawingView.setErase(true); // Set drawing mode to true

        // Start shake animation on the eraser icon
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        imgErase.startAnimation(shake);
    }

    private void toggleMenus() {
        // Toggle the visibility of the menus
        if (isMenuVisible) {
            topMenu.setVisibility(View.GONE); // Hide the menus
            bottomMenu.setVisibility(View.GONE);
        } else {
            topMenu.setVisibility(View.VISIBLE); // Show the menus
            bottomMenu.setVisibility(View.VISIBLE);
        }
        isMenuVisible = !isMenuVisible; // Update the visibility flag
    }
}
