package com.example.myappmtp;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketClient {
    private static final String TAG = "SocketClient";
    private String serverIp;
    private int serverPort;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    public SocketClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public void connect() {
        new Thread(() -> {
            try {
                socket = new Socket(serverIp, serverPort);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                Log.d(TAG, "Connected to server");
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to server: " + e.getMessage(), e);
            }
        }).start();
    }

    // Phương thức kiểm tra kết nối
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void sendImage(Bitmap bitmap, int imageNumber) {
        new Thread(() -> {
            if (isConnected()) {
                try {
                    // Chuyển đổi bitmap thành byte array
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] imageData = stream.toByteArray();

                    // Gửi số ảnh
                    byte[] numberData = intToByteArray(imageNumber);
                    outputStream.write(numberData);

                    // Gửi độ dài của ảnh
                    int imageLength = imageData.length;
                    byte[] lengthData = intToByteArray(imageLength);
                    outputStream.write(lengthData);

                    // Gửi dữ liệu ảnh
                    outputStream.write(imageData);
                    outputStream.flush();

                    Log.d(TAG, "Image and number sent to server");

                    // Đọc phản hồi từ server (tùy chọn)
                    byte[] responseBuffer = new byte[1024];
                    int bytesRead = inputStream.read(responseBuffer);
                    if (bytesRead != -1) {
                        String response = new String(responseBuffer, 0, bytesRead);
                        Log.d(TAG, "Server response: " + response);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error sending image and number", e);

                    disconnect(); // Đóng kết nối khi có lỗi
                }
            } else {
                Log.e(TAG, "Socket is not connected");
            }
        }).start();
    }

    // Chuyển đổi số nguyên thành mảng byte
    private byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                Log.d(TAG, "Socket closed");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket", e);
        }
    }
}
