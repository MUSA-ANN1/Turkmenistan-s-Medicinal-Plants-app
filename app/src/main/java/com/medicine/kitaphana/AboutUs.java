package com.medicine.kitaphana;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class AboutUs extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView burgerIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        getWindow().setStatusBarColor(getColor(R.color.main_green));
        getWindow().setNavigationBarColor(getColor(R.color.main_green));

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        burgerIcon = findViewById(R.id.burger_icon);

        updateDrawerMenuTitles();

        burgerIcon.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
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
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else {
                return false;
            }
        });


        TextView moses = findViewById(R.id.moses_ac);
        /*TextView smile = findViewById(R.id.smile_ac);*/

        String text_m = moses.getText().toString();
        SpannableString string_m = new SpannableString(text_m);

        ClickableSpan clickableSpan_m = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/MUSA-ANN1"));
                startActivity(browser);
            }
        };

        string_m.setSpan(clickableSpan_m, 0, text_m.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        moses.setText(string_m);
        moses.setMovementMethod(LinkMovementMethod.getInstance());



        /*String text_s = smile.getText().toString();
        SpannableString string_s = new SpannableString(text_s);

        ClickableSpan clickableSpan_s = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/smile-web-tech"));
                startActivity(browser);
            }
        };

        string_s.setSpan(clickableSpan_s, 0, text_s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        smile.setText(string_s);
        smile.setMovementMethod(LinkMovementMethod.getInstance());*/
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

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}