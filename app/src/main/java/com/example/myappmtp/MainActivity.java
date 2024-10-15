package com.example.myappmtp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Spinner;
import android.widget.Toast;

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
    private static final String TAG = "MainActivity";
    private SocketClient socketClient;
    private Spinner numberSpinner; // Declare numberSpinner here
    private int currentNumber;
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
// Initialize the Spinner
        numberSpinner = findViewById(R.id.numberSpinner);

        // Create an array of numbers from 90 to 99
        String[] numbers = new String[]{"90", "91", "92", "93", "94", "95", "96", "97", "98", "99"};

        // Use CustomSpinnerAdapter instead of ArrayAdapter
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, numbers);

        // Apply the adapter to the spinner
        numberSpinner.setAdapter(adapter);

        // Set an item selected listener on the spinner
        numberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedNumber = (String) parent.getItemAtPosition(position);
                // Do nothing here, the Spinner will display the selected number
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if no item is selected
            }
        });

        String serverIp = "10.0.2.2"; // Thay đổi địa chỉ IP của server
        int serverPort = 8080; // Cổng mà server đang lắng nghe
        socketClient = new SocketClient(serverIp, serverPort);

        // Kết nối đến server
        try {
            socketClient.connect();
        } catch (Exception e) { // Thay đổi từ IOException sang Exception nếu cần thiết
            e.printStackTrace();
            Toast.makeText(this, "Lỗi kết nối đến server", Toast.LENGTH_SHORT).show();
        }


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
            // Hiển thị ConfirmationDialog khi người dùng nhấn vào nút 完了
            ConfirmationDialog.show(MainActivity.this, (dialog, which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    // Nếu người dùng chọn Yes
                    // Lấy số thứ tự hình ảnh hiện tại
                    int imageNumber = getCurrentImageNumber(); // Đảm bảo phương thức này đã được định nghĩa

                    // Lấy Bitmap của ảnh đang chỉnh sửa dựa trên số thứ tự hình ảnh
                    Bitmap bitmap = getCurrentEditingBitmap(); // Truyền vào imageNumber

                    // Gửi ảnh đến server
                    sendImageToServer(bitmap, imageNumber);

                    // Thực hiện reset drawing view và tăng số thứ tự ảnh
                    resetDrawingViewAndIncreaseNumber();
                }
            });
        });



    }

    // Phương thức để lấy bitmap hiện tại của canvas
    public Bitmap getCurrentEditingBitmap() {
        return  mDrawingView.getCurrentEditingBitmap(); // Gọi phương thức từ DrawingView
    }

    // Phương thức để lấy số thứ tự của hình ảnh hiện tại
    public int getCurrentImageNumber() {
        return mDrawingView.getCurrentImageNumber(); // Gọi phương thức từ DrawingView
    }


    private void resetDrawingViewAndIncreaseNumber() {
        // Reset lại DrawingView (xóa nội dung hiện tại)
        mDrawingView.clear(); // Giả sử bạn đã định nghĩa hàm clear() trong DrawingView để xóa nội dung

        // Tăng giá trị Spinner
        currentNumber = Integer.parseInt((String) numberSpinner.getSelectedItem());

        // Kiểm tra nếu giá trị hiện tại đã đạt tối đa
        if (currentNumber >= 99) {
            // Nếu đạt tối đa, quay lại giá trị tối thiểu
            currentNumber = 90; // Đặt lại giá trị về 90
        } else {
            // Nếu không, tăng giá trị lên 1
            currentNumber++;
        }

        // Cập nhật giá trị mới trong Spinner
        int position = currentNumber - 90; // Lấy vị trí tương ứng trong Spinner (giả sử Spinner bắt đầu từ 90)
        numberSpinner.setSelection(position); // Cập nhật giá trị Spinner
    }



    // Phương thức gửi ảnh đến server
    private void sendImageToServer(Bitmap bitmap, int imageNumber) {
        // Kiểm tra xem socketClient có được khởi tạo không
        if (socketClient != null) {
            // Kiểm tra xem kết nối đã được thiết lập chưa
            if (socketClient.isConnected()) {
                // Gửi ảnh và số thứ tự ảnh
                socketClient.sendImage(bitmap, imageNumber);
            } else {
                Toast.makeText(this, "Socket không được kết nối", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Socket client chưa được khởi tạo", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketClient != null) {
            socketClient.disconnect(); // Đảm bảo đóng kết nối khi Activity bị hủy
        }
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
