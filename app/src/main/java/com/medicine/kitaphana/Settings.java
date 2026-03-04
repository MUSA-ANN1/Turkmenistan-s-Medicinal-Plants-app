package com.medicine.kitaphana;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

public class Settings extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView burgerIcon;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getWindow().setStatusBarColor(getColor(R.color.main_green));
        getWindow().setNavigationBarColor(getColor(R.color.main_green));

        CheckBox checkboxSeasonIcon = findViewById(R.id.checkboxSeasonIcon);
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Load saved state
        checkboxSeasonIcon.setChecked(prefs.getBoolean("auto_season_icon", false));

        // Listen for changes
        checkboxSeasonIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_season_icon", isChecked).apply();
            scheduleSeasonWork(isChecked);
        });

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
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (item.getItemId() == R.id.nav_saved) {
                startActivity(new Intent(this, Saved.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_aboutapp) {
                startActivity(new Intent(this, AboutApp.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_aboutus) {
                startActivity(new Intent(this, AboutUs.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else {
                return false;
            }
        });

        int resId1 = this.getResources().getIdentifier(("settings" + MainActivity.currentLanguage), "string", this.getPackageName());
        String hint1 = this.getString(resId1);
        TextView t = findViewById(R.id.kitap);
        t.setText(hint1);

        updateDrawerMenuTitles();

        findViewById(R.id.language).setOnClickListener(v -> showLanguagePopup());
    }


    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    private void showLanguagePopup() {
        ViewGroup root = findViewById(R.id.main);

        View overlay = new View(this);
        overlay.setBackgroundColor(Color.parseColor("#80000000"));
        root.addView(overlay, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Change background to another color
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.blured_main_green));
        getWindow().setStatusBarColor(getColor(R.color.blured_main_green));
        getWindow().setNavigationBarColor(getColor(R.color.blured_main_green));

        View popupView = getLayoutInflater().inflate(R.layout.popup_language, null);

        float density = getResources().getDisplayMetrics().density;
        int widthInPx = (int) (200 * density);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                widthInPx,
                RecyclerView.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(12);
        popupWindow.showAtLocation(root, Gravity.CENTER, 0, 0);

        TextView textView = popupView.findViewById(R.id.languages);
        CheckBox checkTurkmen = popupView.findViewById(R.id.turkmen);
        CheckBox checkEnglish = popupView.findViewById(R.id.english);
        CheckBox checkRussian = popupView.findViewById(R.id.russian);
        CheckBox checkJapan = popupView.findViewById(R.id.japan);
        CheckBox checkSpain = popupView.findViewById(R.id.spain);

        int idtext = this.getResources().getIdentifier(("Diller" + MainActivity.currentLanguage), "string", this.getPackageName());
        String text = this.getString(idtext);
        textView.setText(text);

        int idTM = this.getResources().getIdentifier(("Türkmen_dili" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textTM = this.getString(idTM);
        checkTurkmen.setText(textTM);

        int idEN = this.getResources().getIdentifier(("Iňlis_dili" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textEN = this.getString(idEN);
        checkEnglish.setText(textEN);

        int idRU = this.getResources().getIdentifier(("Rus_dili" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textRU = this.getString(idRU);
        checkRussian.setText(textRU);

        int idJP = this.getResources().getIdentifier(("Ýapon_dili" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textJP = this.getString(idJP);
        checkJapan.setText(textJP);

        int idES = this.getResources().getIdentifier(("Ispan_dili" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textES = this.getString(idES);
        checkSpain.setText(textES);

        Typeface framd = ResourcesCompat.getFont(this, R.font.framd);
        checkTurkmen.setTypeface(framd);
        checkEnglish.setTypeface(framd);
        checkRussian.setTypeface(framd);
        checkJapan.setTypeface(framd);
        checkSpain.setTypeface(framd);

        // Set default check state
        switch (MainActivity.currentLanguage) {
            case "EN":
                checkEnglish.setChecked(true);
                break;
            case "RU":
                checkRussian.setChecked(true);
                break;
            case "JP":
                checkJapan.setChecked(true);
                break;
            case "ES":
                checkSpain.setChecked(true);
                break;
            default:
                checkTurkmen.setChecked(true);
                break;
        }

        // Single-selection logic
        View.OnClickListener listener = v -> {
            checkTurkmen.setChecked(v == checkTurkmen);
            checkEnglish.setChecked(v == checkEnglish);
            checkRussian.setChecked(v == checkRussian);
            checkJapan.setChecked(v == checkJapan);
            checkSpain.setChecked(v == checkSpain);

            if (v == checkTurkmen) MainActivity.currentLanguage = "TM";
            if (v == checkEnglish) MainActivity.currentLanguage = "EN";
            if (v == checkRussian) MainActivity.currentLanguage = "RU";
            if (v == checkJapan) MainActivity.currentLanguage = "JP";
            if (v == checkSpain) MainActivity.currentLanguage = "ES";

            // Save instantly
            SharedPreferences.Editor editor = getSharedPreferences(MainActivity.LANG_PREFS, MainActivity.MODE_PRIVATE).edit();
            editor.putString(MainActivity.KEY_LANGUAGE, MainActivity.currentLanguage);
            editor.apply();

            TextView t = findViewById(R.id.kitap);
            int resId1 = this.getResources().getIdentifier(("settings" + MainActivity.currentLanguage), "string", this.getPackageName());
            String hint1 = this.getString(resId1);
            t.setText(hint1);

            updateDrawerMenuTitles();

            popupWindow.dismiss();
            root.removeView(overlay);


            // Change background to another color
            root.setBackgroundColor(ContextCompat.getColor(this, R.color.main_green));
            getWindow().setStatusBarColor(getColor(R.color.main_green));
            getWindow().setNavigationBarColor(getColor(R.color.main_green));
        };

        checkTurkmen.setOnClickListener(listener);
        checkEnglish.setOnClickListener(listener);
        checkRussian.setOnClickListener(listener);
        checkJapan.setOnClickListener(listener);
        checkSpain.setOnClickListener(listener);

        popupWindow.setOnDismissListener(() -> {
            root.removeView(overlay);


            // Change background to another color
            root.setBackgroundColor(ContextCompat.getColor(this, R.color.main_green));
            getWindow().setStatusBarColor(getColor(R.color.main_green));
            getWindow().setNavigationBarColor(getColor(R.color.main_green));
        });
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

    private void scheduleSeasonWork(boolean enabled) {
        if (enabled) {
            String season = SeasonIconWorker.getCurrentSeason();
            SeasonIconWorker.switchIcon(this, season);

            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                    SeasonIconWorker.class,
                    1, TimeUnit.MINUTES)
                    .build();

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "season_icon_work",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
            );
        } else {
            WorkManager.getInstance(this).cancelUniqueWork("season_icon_work");

            // ✅ Small delay before switching back to default
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                SeasonIconWorker.switchIcon(this, "default");
            }, 100); // 0.1 second delay
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}