package com.medicine.kitaphana;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Saved extends AppCompatActivity {

    RecyclerView recyclerView;
    CardAdapter adapter;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView burgerIcon;

    List<CardItem> savedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved);

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
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSaved(this);

        adapter = new CardAdapter(savedItems, this::acyl);
        Typeface font = ResourcesCompat.getFont(this, R.font.framd);
        adapter.setFont(font);

        recyclerView.setAdapter(adapter);
    }

    private void loadSaved(Context context) {
        Set<String> keys = SaveManager.getAll(context);
        savedItems.clear();

        int index = 1;
        while (true) {
            int headerId = context.getResources().getIdentifier(
                    "K" + MainActivity.kitap + "T" + index + "H" + MainActivity.currentLanguage,
                    "string", context.getPackageName()
            );
            int topicId = context.getResources().getIdentifier(
                    "K" + MainActivity.kitap + "T" + index + MainActivity.currentLanguage,
                    "string", context.getPackageName()
            );
            if (headerId == 0 || topicId == 0) break;

            String key = "K" + MainActivity.kitap + "T" + index;
            if (keys.contains(key)) {
                savedItems.add(new CardItem(
                        key,
                        context.getString(headerId),
                        context.getString(topicId)
                ));
            }
            index++;
        }
    }

    public void acyl(String key) {
        MainActivity.tema = key;
        startActivity(new Intent(this, Tema.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSaved(this);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        loadSaved(this);
        adapter.notifyDataSetChanged();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
}
