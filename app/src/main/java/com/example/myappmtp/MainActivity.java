package com.example.myappmtp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private CustomDrawingView drawingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy tham chiếu đến nút 完了
        Button buttonComplete = findViewById(R.id.buttonComplete);

        // Thiết lập sự kiện nhấn nút cho nút 完了
        buttonComplete.setOnClickListener(v -> {
            // Hiển thị thông báo khi nút được nhấn
            Toast.makeText(MainActivity.this, "完了が選択されました", Toast.LENGTH_SHORT).show();
        });
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawingView = findViewById(R.id.drawingView);
        ImageButton buttonPencil = findViewById(R.id.imageButtonPencil);
        ImageButton buttonColorPen = findViewById(R.id.imageButtonColorPen);
        ImageButton buttonEraser = findViewById(R.id.imageButtonEraser);


        // Thiết lập màu cho bút chì
        buttonPencil.setOnClickListener(v -> drawingView.setBrushColor(Color.BLACK)); // Màu đen cho bút chì

        // Thiết lập màu cho bút màu
        buttonColorPen.setOnClickListener(v -> drawingView.setBrushColor(Color.RED)); // Màu đỏ cho bút màu

        // Thiết lập màu cho tẩy
        buttonEraser.setOnClickListener(v -> drawingView.setBrushColor(Color.WHITE)); // Màu trắng cho tẩy

        // Nút hoàn tất có thể làm gì đó, ví dụ lưu hình ảnh
        buttonComplete.setOnClickListener(v -> {
            // Logic hoàn tất - ví dụ lưu hoặc kết thúc ứng dụng
            // Bạn có thể thêm logic mà bạn muốn thực hiện khi nhấn nút hoàn tất
        });
    }
}