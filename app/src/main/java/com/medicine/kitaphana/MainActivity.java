package com.medicine.kitaphana;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static String kitap = "";
    public static String tema = "";
    public static String searchedText = "";

    public static final String SEARCH_PREFS = "search_prefs";
    public static final String KEY_SEARCH_TEXT = "saved_search_text";

    public static final String LANG_PREFS = "language_prefs";
    public static final String KEY_LANGUAGE = "current_language";

    public static EditText searchEditText;
    ConstraintLayout main;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView burgerIcon;

    public static String currentLanguage = "TM"; // default

    private List<ImageView> kitapImages = new ArrayList<>();
    private List<CardView> kitapImagesCheckBox = new ArrayList<>();

    @SuppressLint({"MissingInflatedId", "LocalSuppress", "ScheduleExactAlarm", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*requestExactAlarmPermission();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 2001);
            }
        }

        scheduleNewYearAlarm();*/

        getWindow().setStatusBarColor(getColor(R.color.main_green));
        getWindow().setNavigationBarColor(getColor(R.color.main_green));


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        burgerIcon = findViewById(R.id.burger_icon);
        Methods.setDrawerMenuFont(navigationView, this);

        // Open drawer when burger icon is clicked
        burgerIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // Handle drawer menu item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                /*startActivity(new Intent(this, MainActivity.class));*/
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (item.getItemId() == R.id.nav_saved) {
                startActivity(new Intent(this, Saved.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (item.getItemId() == R.id.nav_aboutapp) {
                startActivity(new Intent(this, AboutApp.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (item.getItemId() == R.id.nav_aboutus) {
                startActivity(new Intent(this, AboutUs.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else {
                return false;
            }
        });


        searchEditText = findViewById(R.id.editText);
        main = findViewById(R.id.main);

        // Load saved search text
        SharedPreferences prefs = getSharedPreferences(SEARCH_PREFS, MODE_PRIVATE);
        searchedText = prefs.getString(KEY_SEARCH_TEXT, "");
        if (!searchedText.isEmpty()) {
            searchEditText.setText(searchedText);
        }

        // Load saved language
        SharedPreferences langPrefs = getSharedPreferences(LANG_PREFS, MODE_PRIVATE);
        currentLanguage = langPrefs.getString(KEY_LANGUAGE, "TM");

        int resId = this.getResources().getIdentifier(("Gözle" + MainActivity.currentLanguage), "string", this.getPackageName());
        String hint = this.getString(resId);
        searchEditText.setHint(hint);

        TextView t = findViewById(R.id.kitap);
        resId = this.getResources().getIdentifier(("Kitaplar" + MainActivity.currentLanguage), "string", this.getPackageName());
        hint = this.getString(resId);
        t.setText(hint);

        updateDrawerMenuTitles();

        // Collect kitap images
        int[] bookIds = {
                R.id.book1, R.id.book2, R.id.book3, R.id.book4,
                R.id.book5, R.id.book6, R.id.book7, R.id.book8,
                R.id.book9, R.id.book10, R.id.book11, R.id.book12,
                R.id.book13, R.id.book14, R.id.book15, R.id.book16
        };
        int[] bookchIds = {
                R.id.book1c, R.id.book2c, R.id.book3c, R.id.book4c,
                R.id.book5c, R.id.book6c, R.id.book7c, R.id.book8c,
                R.id.book9c, R.id.book10c, R.id.book11c, R.id.book12c,
                R.id.book13c, R.id.book14c, R.id.book15c, R.id.book16c
        };

        kitapImages.clear();
        for (int id : bookIds) {
            ImageView iv = findViewById(id);
            if (iv != null) {
                kitapImages.add(iv);
            }
        }
        kitapImagesCheckBox.clear();
        for (int ids : bookchIds) {
            CardView ivs = findViewById(ids);
            if (ivs != null) {
                kitapImagesCheckBox.add(ivs);
            }
        }



        setupSearchListener();

        // Apply initial search filter if there's saved text
        if (!searchedText.isEmpty()) {
            applySearchFilter(searchedText);
        }
    }

    // When a kitap is clicked
    public void kitap(View v) {
        kitap = getResources().getResourceEntryName(v.getId()).replace("book", "");
        saveSearchText();
        startActivity(new Intent(MainActivity.this, ici.class));
    }


    private String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{L}\\p{N}\\s]", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }


    private void updateDrawerMenuTitles() {
        // Assuming you have a NavigationView with id nav_view
        NavigationView navigationView = findViewById(R.id.navigation_view);
        if (navigationView == null) return; // safety

        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_home).setTitle(getString(
                getResources().getIdentifier("home" + MainActivity.currentLanguage, "string", getPackageName())));
        menu.findItem(R.id.nav_settings).setTitle(getString(
                getResources().getIdentifier("settings" + MainActivity.currentLanguage, "string", getPackageName())));
        menu.findItem(R.id.nav_saved).setTitle(getString(
                getResources().getIdentifier("saved" + MainActivity.currentLanguage, "string", getPackageName())));
        menu.findItem(R.id.nav_aboutapp).setTitle(getString(
                getResources().getIdentifier("about_app" + MainActivity.currentLanguage, "string", getPackageName())));
        menu.findItem(R.id.nav_aboutus).setTitle(getString(
                getResources().getIdentifier("about_us" + MainActivity.currentLanguage, "string", getPackageName())));
    }



    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            private long lastChange = 0;
            private final long debounceDelay = 100;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lastChange = System.currentTimeMillis();
            }

            @Override
            public void afterTextChanged(Editable s) {
                main.postDelayed(() -> {
                    if (System.currentTimeMillis() - lastChange >= debounceDelay) {
                        searchedText = s.toString();
                        applySearchFilter(searchedText);
                        saveSearchText();
                    }
                }, debounceDelay);
            }
        });
    }

    public void saveSearchText() {
        SharedPreferences.Editor editor = getSharedPreferences(SEARCH_PREFS, MODE_PRIVATE).edit();
        editor.putString(KEY_SEARCH_TEXT, searchedText);
        editor.apply();
    }




private void applySearchFilter(String query) {
        String normalizedQuery = normalize(query);

        for (int i = 0; i < kitapImages.size(); i++) {
            ImageView kitapImage = kitapImages.get(i);
            CardView kitapImageCheckBox = kitapImagesCheckBox.get(i);
            if (kitapImage == null) continue;

            int viewId = kitapImage.getId();
            String kitapName = getResources().getResourceEntryName(viewId);

            String kitapNumber = kitapName.replaceAll("[^0-9]", "");
            String normalizedKitapName = normalize(kitapName);

            boolean matchesTopicOrHeader = false;
            try {
                for (int j = 1; j <= 10; j++) {
                    int topicId = getResources().getIdentifier("K" + kitapNumber + "T" + j + currentLanguage, "string", getPackageName());
                    int headerId = getResources().getIdentifier("K" + kitapNumber + "T" + j + "H" + currentLanguage, "string", getPackageName());

                    if (topicId != 0) {
                        String topicText = getString(topicId);
                        if (normalize(topicText).contains(normalizedQuery)) {
                            matchesTopicOrHeader = true;
                            break;
                        }
                    }

                    if (headerId != 0) {
                        String headerText = getString(headerId);
                        if (normalize(headerText).contains(normalizedQuery)) {
                            matchesTopicOrHeader = true;
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            boolean matches = normalizedKitapName.contains(normalizedQuery)
                    || normalize(kitapNumber).contains(normalizedQuery)
                    || matchesTopicOrHeader;

            kitapImageCheckBox.setVisibility(matches || normalizedQuery.isEmpty() ? View.VISIBLE : View.GONE);
            kitapImage.setVisibility(matches || normalizedQuery.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSearchText();
    }
}