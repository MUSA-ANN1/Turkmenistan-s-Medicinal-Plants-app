package com.medicine.kitaphana;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    // ── Change this to match your exported model filename in assets/ ──
    private static final String MODEL_FILE   = "model.tflite";
    private static final String LABELS_FILE  = "labels.txt";
    private static final int    IMAGE_SIZE   = 224;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        getWindow().setStatusBarColor(getColor(R.color.main_green));
        getWindow().setNavigationBarColor(getColor(R.color.main_green));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main) != null
                        ? findViewById(R.id.main)
                        : getWindow().getDecorView(),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                });

        ImageView  ivPlant      = findViewById(R.id.ivPlant);
        TextView   tvLabel      = findViewById(R.id.tvLabel);
        TextView   tvConfidence = findViewById(R.id.tvConfidence);
        Button     btnBack      = findViewById(R.id.btnBack);

        // Decode image bytes passed from Surat
        byte[] imageBytes = getIntent().getByteArrayExtra("image_bytes");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        ivPlant.setImageBitmap(bitmap);

        // Run inference
        try {
            List<String> labels   = loadLabels();
            Interpreter  tflite   = new Interpreter(loadModelFile());
            float[][]    output   = new float[1][labels.size()];
            ByteBuffer   inputBuf = bitmapToByteBuffer(bitmap);

            tflite.run(inputBuf, output);
            tflite.close();

            // Find highest confidence class
            int   bestIdx   = 0;
            float bestScore = 0f;
            for (int i = 0; i < output[0].length; i++) {
                if (output[0][i] > bestScore) {
                    bestScore = output[0][i];
                    bestIdx   = i;
                }
            }

            String label      = labels.size() > bestIdx ? labels.get(bestIdx) : "Unknown";
            int    percentage = Math.round(bestScore * 100);

            tvLabel.setText(percentage + "% " + label);

        } catch (IOException e) {
            tvLabel.setText("Model error");
            tvConfidence.setText(e.getMessage());
        }

        btnBack.setOnClickListener(v -> finish());
    }

    /** Load model.tflite from assets/ */
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fd = getAssets().openFd(MODEL_FILE);
        FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = fis.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY,
                fd.getStartOffset(), fd.getDeclaredLength());
    }

    /** Load labels.txt from assets/ — one label per line */
    private List<String> loadLabels() throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(getAssets().open(LABELS_FILE)));
        String line;
        while ((line = br.readLine()) != null) {
            // Teachable Machine prefixes lines with "0 Rose", "1 Tulip" etc.
            String trimmed = line.trim();
            if (trimmed.contains(" ")) {
                trimmed = trimmed.substring(trimmed.indexOf(' ') + 1);
            }
            if (!trimmed.isEmpty()) labels.add(trimmed);
        }
        br.close();
        return labels;
    }

    /** Convert 224×224 Bitmap → ByteBuffer normalized to [0,1] float */
    private ByteBuffer bitmapToByteBuffer(Bitmap bitmap) {
        // 4 bytes per float × 3 channels × 224 × 224
        ByteBuffer buf = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
        buf.order(ByteOrder.nativeOrder());

        int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

        for (int pixel : pixels) {
            buf.putFloat(((pixel >> 16) & 0xFF) / 255.0f); // R
            buf.putFloat(((pixel >> 8)  & 0xFF) / 255.0f); // G
            buf.putFloat(( pixel        & 0xFF) / 255.0f); // B
        }
        return buf;
    }
}