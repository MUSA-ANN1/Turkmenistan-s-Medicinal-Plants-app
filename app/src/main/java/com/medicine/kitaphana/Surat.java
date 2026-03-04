package com.medicine.kitaphana;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Surat extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_surat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(getColor(R.color.main_green));
        getWindow().setNavigationBarColor(getColor(R.color.main_green));

        previewView = findViewById(R.id.previewView);
        Button btnCapture = findViewById(R.id.btnCapture);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }

        btnCapture.setOnClickListener(v -> capturePhoto());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                provider.unbindAll();
                provider.bindToLifecycle(this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("Surat", "Camera init failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(cameraExecutor,
                new ImageCapture.OnImageCapturedCallback() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        // Convert ImageProxy → Bitmap
                        Bitmap original = imageProxyToBitmap(image);
                        image.close();

                        if (original == null) {
                            runOnUiThread(() ->
                                    Toast.makeText(Surat.this, "Capture failed", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        // Resize to 224x224 (required by TFLite / Teachable Machine)
                        Bitmap resized = Bitmap.createScaledBitmap(original, 224, 224, true);

                        // Compress to WebP bytes
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, baos);
                        byte[] imageBytes = baos.toByteArray();

                        // Launch ResultActivity with the image bytes
                        Intent intent = new Intent(Surat.this, ResultActivity.class);
                        intent.putExtra("image_bytes", imageBytes);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Log.e("Surat", "Capture error", e);
                        runOnUiThread(() ->
                                Toast.makeText(Surat.this, "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        // ImageProxy with JPEG format → decode to Bitmap
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        java.nio.ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}