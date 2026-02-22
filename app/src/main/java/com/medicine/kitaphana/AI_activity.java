package com.medicine.kitaphana;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AI_activity extends AppCompatActivity {

    private static final String MODEL = "gemini-2.5-flash-lite";

    private static final String DEVELOPER_INFO =
            "Bu programmany Musa Annagulyýew döretdi.\n" +
                    "Musa Android developer, CS student, 3D/AR höwesjeňi.\n" +
                    "Başarnyklary: Android Studio (Java/Kotlin), Jetpack Compose, AR Foundation, Python, JavaScript, 3ds Max, AutoCAD, Photoshop, Illustrator.\n" +
                    "Çap edilen goşundylary:\n" +
                    "- Türkmenistanyň Dermanlyk Ösümlikleri (offline kitap, 5 dil)\n" +
                    "- Berk Bilim (mental arifmetika + karýera maslahatçysy)\n" +
                    "- Mini Chemistry Translator\n" +
                    "Maksat: MEXT üçin portfolio, AR programmalar.\n" +
                    "Habarlaşmak: musa.annaguliev@gmail.com | Telegram: @Mu4asa";

    private static final String SYSTEM_PROMPT =
            "Seniň adyň Ösümlik Bilimi 🌿\n" +
                    "Sen peýdaly we dostlukly AI kömekçi.\n" +
                    "Ulanyjy haýsy dilde ýazsa şol dilde jogap ber.\n" +
                    "Jogaplaryňy Markdown formatda we emojiler bilen ýaz.\n" +
                    "Gysgaça we anyk jogap ber.\n\n" +
                    "Eger ulanyjy developer, Musa ýa-da programma barada sorasa şu maglumaty ulan:\n" +
                    DEVELOPER_INFO;

    // -------------------- Data Model --------------------
    private static class Message {
        String text;
        boolean isUser;
        Message(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
        }
    }

    // -------------------- Fields --------------------
    private ChatFutures chat;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private List<Message> messages;
    private ChatAdapter adapter;
    private RecyclerView chatRecycler;
    private SharedPreferences sp;
    private ProgressBar loadingBar;
    private ImageButton sendButton;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView burgerIcon;

    // -------------------- Adapter --------------------
    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Message> msgs;

        ChatAdapter(List<Message> msgs) {
            this.msgs = msgs;
        }

        @Override
        public int getItemViewType(int pos) {
            return msgs.get(pos).isUser ? 1 : 2;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == 1 ? R.layout.message_item_user : R.layout.message_item_ai;
            View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return viewType == 1 ? new UserHolder(v) : new AiHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
            Message msg = msgs.get(pos);
            if (holder instanceof AiHolder) {
                ((AiHolder) holder).msg.setText(markdownToSpanned(msg.text));
            } else {
                ((UserHolder) holder).msg.setText(msg.text);
            }
        }

        @Override
        public int getItemCount() {
            return msgs.size();
        }

        class UserHolder extends RecyclerView.ViewHolder {
            TextView msg;
            UserHolder(View v) {
                super(v);
                msg = v.findViewById(R.id.messageText);
            }
        }

        class AiHolder extends RecyclerView.ViewHolder {
            TextView msg;
            AiHolder(View v) {
                super(v);
                msg = v.findViewById(R.id.messageText);
            }
        }
    }

    // -------------------- onCreate --------------------
    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

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
            else if (id == R.id.nav_saved) drawerLayout.closeDrawer(GravityCompat.START);
            else if (id == R.id.nav_settings) startActivity(new Intent(this, Settings.class));
            else if (id == R.id.nav_aboutapp) startActivity(new Intent(this, AboutApp.class));
            else if (id == R.id.nav_aboutus) startActivity(new Intent(this, AboutUs.class));
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        EditText userInput         = findViewById(R.id.user_input);
        sendButton                 = findViewById(R.id.send_button);
        loadingBar                 = findViewById(R.id.loading_bar);
        chatRecycler               = findViewById(R.id.chatRecycler);
        DrawerLayout drawerLayout  = findViewById(R.id.drawer_layout);
        NavigationView navView     = findViewById(R.id.navigation_view);
        View burgerIcon            = findViewById(R.id.burger_icon);

        sp = getSharedPreferences("chat_cache", MODE_PRIVATE);
        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatRecycler.setLayoutManager(lm);
        chatRecycler.setAdapter(adapter);

        // Init Gemini
        initGemini();

        burgerIcon.setOnClickListener(v -> drawerLayout.open());
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.close();
            return true;
        });

        // Restore cached chat
        String saved = sp.getString("messages", null);
        if (saved != null) {
            try {
                JSONArray arr = new JSONArray(saved);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    messages.add(new Message(obj.getString("text"), obj.getBoolean("isUser")));
                }
                adapter.notifyDataSetChanged();
                if (!messages.isEmpty())
                    chatRecycler.scrollToPosition(messages.size() - 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Send button
        sendButton.setOnClickListener(v -> {
            String query = userInput.getText().toString().trim();
            if (query.isEmpty()) return;

            messages.add(new Message(query, true));
            adapter.notifyItemInserted(messages.size() - 1);
            chatRecycler.scrollToPosition(messages.size() - 1);
            userInput.setText("");
            saveChatCache();

            loadingBar.setVisibility(View.VISIBLE);
            sendButton.setEnabled(false);

            sendMessage(query);
        });
    }

    // -------------------- Init Gemini --------------------
    private void initGemini() {
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.maxOutputTokens = 500;
        configBuilder.temperature = 0.7f;

        // System instruction as Content
        Content systemInstruction = new Content.Builder()
                .addText(SYSTEM_PROMPT)
                .build();

        GenerativeModel model = new GenerativeModel(
                MODEL,                        // modelName
                "AIzaSyDe4jLRchcGRLElIyjBck0QwHv6YZkFO2k",   // apiKey // backUp=AIzaSyByns9ZqBAC4ISbxlYDjez5vdbvzCeBU2g
                configBuilder.build(),        // generationConfig
                null,                         // safetySettings
                new RequestOptions(),         // requestOptions
                null,                         // tools
                null,                         // toolConfig
                systemInstruction             // systemInstruction
        );

        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(model);
        chat = modelFutures.startChat();
    }

    // -------------------- Send Message --------------------
    private void sendMessage(String query) {
        Content userContent = new Content.Builder()
                .addText(query)
                .build();

        ListenableFuture<GenerateContentResponse> future = chat.sendMessage(userContent);

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiText = result.getText();
                if (aiText == null) aiText = "❌ Jogap boş geldi.";
                final String finalText = aiText;
                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                    messages.add(new Message(finalText, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                    chatRecycler.scrollToPosition(messages.size() - 1);
                    saveChatCache();
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                android.util.Log.e("GEMINI", "Error: " + t.getMessage(), t);
                final String error = "❌ " + t.getClass().getSimpleName() + ": " + t.getMessage();
                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                    messages.add(new Message(error, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                    chatRecycler.scrollToPosition(messages.size() - 1);
                    saveChatCache();
                });
            }
        }, executor);
    }

    // -------------------- Markdown → Spanned --------------------
    private Spanned markdownToSpanned(String markdown) {
        if (markdown == null)
            return HtmlCompat.fromHtml("", HtmlCompat.FROM_HTML_MODE_LEGACY);

        String html = markdown
                .replaceAll("(?m)^### (.+)$", "<b><big>$1</big></b>")
                .replaceAll("(?m)^## (.+)$",  "<b><big><big>$1</big></big></b>")
                .replaceAll("(?m)^# (.+)$",   "<b><big><big><big>$1</big></big></big></b>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>")
                .replaceAll("\\*(.+?)\\*", "<i>$1</i>")
                .replaceAll("(?m)^[•\\-] (.+)$", "&#8226; $1<br>")
                .replace("\n", "<br>");

        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    // -------------------- Cache --------------------
    private void saveChatCache() {
        JSONArray arr = new JSONArray();
        try {
            int start = Math.max(0, messages.size() - 50);
            for (int i = start; i < messages.size(); i++) {
                JSONObject o = new JSONObject();
                o.put("text",   messages.get(i).text);
                o.put("isUser", messages.get(i).isUser);
                arr.put(o);
            }
            sp.edit().putString("messages", arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- Lifecycle --------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
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