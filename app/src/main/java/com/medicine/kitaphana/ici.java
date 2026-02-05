package com.medicine.kitaphana;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ici extends AppCompatActivity {

    private boolean filterHeader = true;
    private boolean filterTopic = true;
    private boolean filterKey = true;

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private EditText searchEditText;

    private final List<CardItem> allItems = new ArrayList<>();
    private final List<CardItem> filteredItems = new ArrayList<>();

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView burgerIcon;

    private static final String PREFS_NAME = "filter_prefs";
    private static final String PREF_HEADER = "filterHeader";
    private static final String PREF_TOPIC = "filterTopic";
    private static final String PREF_KEY = "filterKey";
    private static final String SEARCH_PREFS = "search_prefs";
    private static final String KEY_SEARCH_TEXT = "saved_search_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ici);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        burgerIcon = findViewById(R.id.burger_icon);

        updateDrawerMenuTitles();

        burgerIcon.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_saved) startActivity(new Intent(this, Saved.class));
            else if (id == R.id.nav_settings) startActivity(new Intent(this, Settings.class));
            else if (id == R.id.nav_aboutapp) startActivity(new Intent(this, AboutApp.class));
            else if (id == R.id.nav_aboutus) startActivity(new Intent(this, AboutUs.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        searchEditText = findViewById(R.id.editText_ici);

        int resId = getResources().getIdentifier(
                "Gözle" + MainActivity.currentLanguage,
                "string",
                getPackageName()
        );
        searchEditText.setHint(getString(resId));

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        filterHeader = prefs.getBoolean(PREF_HEADER, true);
        filterTopic = prefs.getBoolean(PREF_TOPIC, true);
        filterKey = prefs.getBoolean(PREF_KEY, true);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData(this);
        filteredItems.addAll(allItems);

        adapter = new CardAdapter(filteredItems, this::acyl);
        Typeface font = ResourcesCompat.getFont(this, R.font.framd);
        adapter.setFont(font);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.imageViewFilter).setOnClickListener(v -> showFilterPopup());

        setupSearchListener();
        loadSavedSearch();
    }

    // ---------------- DATA ----------------

    private void loadData(Context context) {
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

            allItems.add(new CardItem(
                    "K" + MainActivity.kitap + "T" + index,
                    context.getString(headerId),
                    context.getString(topicId)
            ));
            index++;
        }
    }

    // ---------------- SEARCH (FIXED) ----------------

    private Runnable searchRunnable;

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {

            private final long debounceDelay = 120;

            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (searchRunnable != null) {
                    recyclerView.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchRunnable = () -> applyCustomFilters(filterHeader, filterTopic, filterKey);
                recyclerView.postDelayed(searchRunnable, debounceDelay);
            }
        });
    }

    private void applyCustomFilters(boolean h, boolean t, boolean k) {
        String q = normalize(searchEditText.getText().toString());

        filteredItems.clear();

        if (q.isEmpty()) {
            filteredItems.addAll(allItems);
            adapter.notifyDataSetChanged();
            return;
        }

        for (CardItem item : allItems) {
            if ((h && item.nHeader.contains(q)) ||
                    (t && item.nTopic.contains(q)) ||
                    (k && item.nKey.contains(q))) {
                filteredItems.add(item);
            }
        }

        adapter.notifyDataSetChanged();
    }


    // ---------------- SEARCH STATE ----------------

    private void loadSavedSearch() {
        String s = getSharedPreferences(SEARCH_PREFS, MODE_PRIVATE)
                .getString(KEY_SEARCH_TEXT, "");
        searchEditText.setText(s);
        applyCustomFilters(filterHeader, filterTopic, filterKey);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences(SEARCH_PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_SEARCH_TEXT, searchEditText.getText().toString())
                .apply();
    }

    // ---------------- HELPERS ----------------

    static String normalize(String t) {
        if (t == null) return "";
        return Normalizer.normalize(t, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public void acyl(String key) {
        MainActivity.tema = key;
        MainActivity.searchedText = searchEditText.getText().toString();
        startActivity(new Intent(this, Tema.class));
    }

    // ---------------- DRAWER ----------------

    private void updateDrawerMenuTitles() {
        if (navigationView == null) return;
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

    // ---------------- POPUP FILTER (UNCHANGED) ----------------

    private void showFilterPopup() {
        FrameLayout root = findViewById(R.id.main); // Overlay for blur
        View overlay = new View(this);
        overlay.setBackgroundColor(Color.parseColor("#80000000")); // semi-transparent black
        root.addView(overlay, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)); // Change background to another color
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.blured_main_green));
        View popupView = getLayoutInflater().inflate(R.layout.popup_filter, null);
        float density = getResources().getDisplayMetrics().density;
        int widthInPx = (int) (200 * density); // 200dp width
        PopupWindow popupWindow = new PopupWindow(popupView, widthInPx, RecyclerView.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(12);
        popupWindow.showAtLocation(root, Gravity.CENTER, 0, 0);
        TextView textView = popupView.findViewById(R.id.filterTitle);
        CheckBox checkHeader = popupView.findViewById(R.id.checkBoxHeader);
        CheckBox checkTopic = popupView.findViewById(R.id.checkBoxTopic);
        CheckBox checkKey = popupView.findViewById(R.id.checkBoxKey);
        Button applyButton = popupView.findViewById(R.id.buttonApply);
        int idtext = this.getResources().getIdentifier(("Boýunça_süzüň" + MainActivity.currentLanguage), "string", this.getPackageName());
        String text = this.getString(idtext);
        textView.setText(text);
        int idTM = this.getResources().getIdentifier(("Temaň_ady" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textTM = this.getString(idTM);
        checkHeader.setText(textTM);
        int idEN = this.getResources().getIdentifier(("Tema" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textEN = this.getString(idEN);
        checkTopic.setText(textEN);
        int idRU = this.getResources().getIdentifier(("Temaň_belgisi" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textRU = this.getString(idRU);
        checkKey.setText(textRU);
        int idJP = this.getResources().getIdentifier(("Bolýar" + MainActivity.currentLanguage), "string", this.getPackageName());
        String textJP = this.getString(idJP);
        applyButton.setText(textJP);
        Typeface framd = ResourcesCompat.getFont(this, R.font.framd);
        checkHeader.setTypeface(framd);
        checkTopic.setTypeface(framd);
        checkKey.setTypeface(framd);
        applyButton.setTypeface(framd);
        checkHeader.setChecked(filterHeader);
        checkTopic.setChecked(filterTopic);
        checkKey.setChecked(filterKey);
        applyButton.setOnClickListener(v -> {
            filterHeader = checkHeader.isChecked();
            filterTopic = checkTopic.isChecked();
            filterKey = checkKey.isChecked();
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putBoolean(PREF_HEADER, filterHeader);
            editor.putBoolean(PREF_TOPIC, filterTopic);
            editor.putBoolean(PREF_KEY, filterKey);
            editor.apply();
            applyCustomFilters(filterHeader, filterTopic, filterKey);
            popupWindow.dismiss();
            root.removeView(overlay); // Change background to another color
            root.setBackgroundColor(ContextCompat.getColor(this, R.color.main_green));
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                root.removeView(overlay); // Change background to another color
                root.setBackgroundColor(ContextCompat.getColor(root.getContext(), R.color.main_green));
            }
        });
    }
}
