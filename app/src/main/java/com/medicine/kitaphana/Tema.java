package com.medicine.kitaphana;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.view.ViewGroup;
import android.view.ScaleGestureDetector;

import java.text.Normalizer;
import java.util.Locale;

public class Tema extends AppCompatActivity {

    private Toast scaleToast;
    private ScaleGestureDetector scaleDetector;
    private float textScale = 1.0f; // 100% default
    private static final String PREFS_NAME = "ui_prefs";
    private static final String KEY_TEXT_SCALE = "text_scale";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tema);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(getColor(R.color.main_green));
        getWindow().setNavigationBarColor(getColor(R.color.main_green));

        TextView t = findViewById(R.id.topic);
        TextView h = findViewById(R.id.header);

        // Load saved text scale
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        textScale = prefs.getFloat(KEY_TEXT_SCALE, 1.0f);

        // Construct the name
        String resourceName = MainActivity.tema + MainActivity.currentLanguage;
        int resId = getResources().getIdentifier(resourceName, "string", getPackageName());

        String topicText;
        if (resId != 0) {
            topicText = getString(resId);
        } else {
            // Fallback: Use a default string or an empty string to prevent the crash
            topicText = "Resource not found: " + resourceName;
        }
        t.setText(topicText);

        // Load header text
        int resId_h = getResources().getIdentifier((MainActivity.tema + "H" + MainActivity.currentLanguage), "string", getPackageName());
        String headerText = getString(resId_h);
        h.setText(headerText);

        // Highlight search text if available
        if (MainActivity.searchedText != null && !MainActivity.searchedText.isEmpty()) {
            t.setText(highlightText(topicText, MainActivity.searchedText));
            h.setText(highlightText(headerText, MainActivity.searchedText));
        }

        // Apply saved scale immediately
        View root = findViewById(R.id.main);
        scaleViewText(root, textScale);

        // Initialize pinch detector
        scaleDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        textScale *= detector.getScaleFactor();

                        // Limit min/max zoom
                        textScale = Math.max(0.7f, Math.min(textScale, 1.6f));

                        // Apply scale
                        scaleViewText(root, textScale);

                        // Show toast with current %
                        if (scaleToast != null) scaleToast.cancel(); // cancel previous toast
                        int percent = (int) (textScale * 100);
                        scaleToast = Toast.makeText(Tema.this, percent + "%", Toast.LENGTH_SHORT);
                        scaleToast.show();

                        // Save scale
                        prefs.edit().putFloat(KEY_TEXT_SCALE, textScale).apply();
                        return true;
                    }
                }
        );
    }

    // Dispatch touch events to scale detector
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    // Recursive method to scale all TextViews inside a view
    private void scaleViewText(View view, float scale) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;

            if (tv.getTag() == null) {
                tv.setTag(tv.getTextSize()); // save original size once
            }

            float originalSize = (float) tv.getTag();
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalSize * scale);

        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                scaleViewText(vg.getChildAt(i), scale);
            }
        }
    }

    /**
     * Highlights occurrences of the search term in the text.
     * Works with accented characters (ž, ä, ü, ç, ý, ň, ö, etc.)
     */
    private CharSequence highlightText(String fullText, String query) {
        SpannableString spannable = new SpannableString(fullText);

        String normalizedFull = Normalizer.normalize(fullText, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        String normalizedQuery = Normalizer.normalize(query, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        int start = normalizedFull.indexOf(normalizedQuery);
        while (start >= 0) {
            int end = start + normalizedQuery.length();
            spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = normalizedFull.indexOf(normalizedQuery, end);
        }

        return spannable;
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}