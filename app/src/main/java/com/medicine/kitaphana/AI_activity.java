package com.medicine.kitaphana;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;

public class AI_activity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView burgerIcon;
    private EditText userInput;
    private TextView aiResponseText;
    private ProgressBar loadingBar; // Declared here
    private ChatFutures chatSession;
    private boolean isAiReady = false; // Safety flag

    @SuppressLint({"MissingInflatedId", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

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
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_saved) startActivity(new Intent(this, Saved.class));
            else if (id == R.id.nav_settings) startActivity(new Intent(this, Settings.class));
            else if (id == R.id.nav_aboutapp) startActivity(new Intent(this, AboutApp.class));
            else if (id == R.id.nav_aboutus) drawerLayout.closeDrawer(GravityCompat.START);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // --- FIX STARTS HERE ---
        userInput = findViewById(R.id.user_input);
        aiResponseText = findViewById(R.id.ai_response);
        loadingBar = findViewById(R.id.loading_bar); // <--- THIS WAS MISSING!
        ImageButton btnSend = findViewById(R.id.send_button);
        // --- FIX ENDS HERE ---

        // Initialize Gemini in background
        new Thread(() -> {
            initializeGemini();

            // Update UI when ready
            runOnUiThread(() -> {
                isAiReady = true;
                Toast.makeText(this, "AI hünärmeni taýýar! (AI Ready)", Toast.LENGTH_SHORT).show();
            });
        }).start();

        btnSend.setOnClickListener(v -> {
            String text = userInput.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(this, "Sorag ýazyň (Write a question)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Safety Check: Don't crash if user clicks before AI loads
            if (!isAiReady || chatSession == null) {
                Toast.makeText(this, "AI entek ýüklenýär, garaşyň... (Please wait)", Toast.LENGTH_SHORT).show();
                return;
            }

            askGemini(text);
        });
    }

    private void initializeGemini() {
        // 1. Get data
        String plantData = getPlantKnowledgeBase();

        // 2. Set instructions
        String systemInstructionText =
                "Sen hünärmen AI.\n" +
                        "Eger ulanyjy Türkmenistanyň dermanlyk ösümlikleri barada sorasa, diňe berlen maglumatlara esaslan: " +
                        "maglumat ýok bolsa 'Bilmedim' ýada internetdaky maglumaty görkez, maglumat bar ýöne düşünmek kyn bolsa 'Düşünmedim'.\n\n" +
                        "Plant data gysga we strukturaly bolmaly, aşakdaky görnüşde:\n" +
                        "## 🌿 Plant Name (bold & large text)\n" +
                        "- What it does for illness\n" +
                        "- Things needed\n" +
                        "- How to prepare\n\n" +
                        "Eger ulanyjy goşmaça şahsy maglumat berse (meselem, ýaşy, agramy, belentligi, alamatlary), " +
                        "AI diňe ýokardaky plant maglumatlaryna esaslanyp, maslahat berip biler, dogry diagnoz bermän. " +
                        "Her zaman degişli medisina hünärmenine ýüz tutmagy maslahat berýär.\n\n" +
                        "PLANT DATA (source in Turkmen):\n" + plantData + "\n\n" +
                        "Eger ulanyjy başga sorag berse (meselem, howa, dünýäniň iň uly ýurdy, umumy bilim soraglary), " +
                        "AI jogap berip biler.\n\n" +
                        "AI useriň soragynyň dilini anyklaýar we şol dilde jogap berýär. " +
                        "Plant data diňe Turkmen dilinde berlen maglumatdan peýdalanyp, dogry we takyk bolmalydyr. " +
                        "Other questions can be answered normally in the user’s language.";

        Content systemInstruction = new Content.Builder()
                .addText(systemInstructionText)
                .build();

        // 3. Configure Model (Explicit Constructor to avoid Builder errors)
        GenerationConfig config = new GenerationConfig.Builder().build();
        RequestOptions requestOptions = new RequestOptions();

        GenerativeModel gm = new GenerativeModel(
                "gemini-3-flash-preview",
                "AIzaSyD_cZQnWTcNw1rLTfeMq7jJ4l15d35yzOU", // Your Key
                config,
                new ArrayList<SafetySetting>(),
                requestOptions,
                null,
                null,
                systemInstruction
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        chatSession = model.startChat();
    }

    private void askGemini(String query) {
        // Clear input and show typing indicator
        userInput.setText("");
        loadingBar.setVisibility(View.VISIBLE);
        aiResponseText.setText("AI jogap berýär...");


        // Build content for Gemini
        Content content = new Content.Builder().addText(query).build();
        ListenableFuture<GenerateContentResponse> response = chatSession.sendMessage(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);

                    String mdResponse = result.getText();
                    String htmlResponse = convertMarkdownToHtml(mdResponse);
                    aiResponseText.setText(Html.fromHtml(htmlResponse, Html.FROM_HTML_MODE_LEGACY)+"\n\n");
                    aiResponseText.setMovementMethod(LinkMovementMethod.getInstance()); // clickable links
                });
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Throwable t) {
                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    aiResponseText.setText("Ýalňyşlyk: " + t.getMessage());
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }


    public String getPlantKnowledgeBase() {
        StringBuilder sb = new StringBuilder();
        // Scanning K1 (Book 1) and T1 to T150 (Plants)
        for (int k = 1; k <= 2; k++) {
            for (int t = 1; t <= 150; t++) {
                String headerKey = "K" + k + "T" + t + "HTM";
                String bodyKey = "K" + k + "T" + t + "TM";

                int hId = getResources().getIdentifier(headerKey, "string", getPackageName());
                int bId = getResources().getIdentifier(bodyKey, "string", getPackageName());

                if (hId != 0 && bId != 0) {
                    sb.append("Ady: ").append(getResources().getString(hId)).append("\n");
                    sb.append("Maglumat: ").append(getResources().getString(bId)).append("\n\n");
                }
            }
        }
        return sb.toString();
    }


    private String convertMarkdownToHtml(String md) {
        if (md == null) return "";

        String html = md;

        // Bold + Italic first
        html = html.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<b><i>$1</i></b>");
        html = html.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        html = html.replaceAll("\\*(.*?)\\*", "<i>$1</i>");

        // Headings
        html = html.replaceAll("(?m)^### (.*)", "<h4>$1</h4>");
        html = html.replaceAll("(?m)^## (.*)", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^# (.*)", "<h2>$1</h2>");

        // Bullet lists
        html = html.replaceAll("(?m)^\\s*[-*+] (.*)", "&#8226; $1<br>");

        // Inline code
        html = html.replaceAll("`(.*?)`", "<code>$1</code>");

        // Code blocks
        html = html.replaceAll("(?s)```(.*?)```", "<pre>$1</pre>");

        // Links
        html = html.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\"$2\">$1</a>");

        // Horizontal rules
        html = html.replaceAll("(?m)^---$", "<hr>");

        // Newlines → <br>
        html = html.replaceAll("\\n", "<br>");

        return html;
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