package com.medicine.kitaphana;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.medicine.kitaphana.BuildConfig;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AI_activity extends AppCompatActivity {

    // ── Model constants ──
    private static final String GEMINI_MODEL       = "gemini-2.5-flash-lite";
    private static final String QAMAR_MODEL        = "Qamar2";
    private static final String QAMAR_API_URL      = "http://api.qamar.asia:7777/v1/chat/completions";

    // ── SharedPreferences key for selected model ──
    private static final String PREF_SELECTED_MODEL = "selected_model";
    private static final String MODEL_GEMINI        = "gemini";
    private static final String MODEL_QAMAR         = "qamar";

    private static final String DEVELOPER_INFO =
            "Bu programmany Musa Annagulyýew döretdi.\n" +
                    "Musa Android developer, CS student, 3D/AR höwesjeňi.\n" +
                    "Doglan senesi: 05.10.2010;\n" +
                    "Ýaşaýan ýeri: Türkmenistan;\n" +
                    "Okaýan mekdebi (2016-2028-nji ýyllda): Balkan welaýatynyň Balkanabat şäheriniň daşary ýurt dillerine ýöriteleşdirilen 3-nji orta mekdebiniň 10-njy \"B\" synp okuwçysy (2025-2026 ýyldaky maglumat);\n" +
                    "Başarnyklary: Android Studio (Java/Kotlin), Jetpack Compose, AR Foundation, Python, JavaScript, HTML, CSS, 3ds Max, AutoCAD, Photoshop, Illustrator, Networking.\n" +
                    "Çap edilen goşundylary:\n" +
                    "- Türkmenistanyň Dermanlyk Ösümlikleri (offline kitap, 5 dil)\n" +
                    "- Berk Bilim (mental arifmetika + karýera maslahatçysy)\n" +
                    "Habarlaşmak: musa.annaguliev@gmail.com | Telegram: @Mu4asa | +993 61 192383 | Instagram: @musa.annaguliev";

    private static final String SYSTEM_PROMPT =
            "Seniň adyň TakykAI 🌿\n" +
                    "Sen peýdaly we dostlukly AI kömekçi.\n" +
                    "Ulanyjy haýsy dilde ýazsa şol dilde jogap ber.\n" +
                    "Jogaplaryňy Markdown formatda we emojiler bilen ýaz.\n" +
                    "Gysgaça we anyk jogap ber.\n\n" +
                    "Eger ulanyjy developer, Musa ýa-da programma barada sorasa şu maglumaty ulan (Musa barada hemme maglumaty ulanyjy soramasa berme, diňe gerekli (ulanyjynyň soran) maglumaty ber):\n" +
                    DEVELOPER_INFO;

    // ── Slogans for the welcome animation ──
    private static final String[] SLOGANS = {
            "TakykAI, Tiz, Takyk, Düşnükli jogaplar berýär.",
            "TakykAI, Islendik soraglaryňyza jogap berýän.",
            "TakykAI, Akylly kömekçiňiz, hemişe taýyn.",
            "TakykAI, Maglumaty çalt tapyň, wagt ýitirme.",
            "TakykAI, Soragyňyz bar bolsa, men şu ýerde.",
            "TakykAI, Bilim bilen güýçlendirilen kömekçi.",
            "TakykAI, Her soraga dogry jogap."
    };

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
    private SharedPreferences modelPrefs;
    private ProgressBar loadingBar;
    private ImageButton sendButton;
    private TextView btnModelSelector;
    private TextView tvSlogan;
    private Handler typingHandler;
    private Handler sloganHandler;
    private String currentModel;
    private int currentSloganIndex = 0;
    private boolean sloganVisible = true;

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
                if (pos == msgs.size() - 1 && !msg.isUser) {
                    typeTextAnimation(((AiHolder) holder).msg, markdownToSpanned(msg.text), 15);
                } else {
                    ((AiHolder) holder).msg.setText(markdownToSpanned(msg.text));
                }
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
            UserHolder(View v) { super(v); msg = v.findViewById(R.id.messageText); }
        }

        class AiHolder extends RecyclerView.ViewHolder {
            TextView msg;
            AiHolder(View v) { super(v); msg = v.findViewById(R.id.messageText); }
        }
    }

    // -------------------- Typing Animation (chat bubbles) --------------------
    private void typeTextAnimation(TextView textView, Spanned spanned, int delayMs) {
        if (typingHandler != null) typingHandler.removeCallbacksAndMessages(null);
        typingHandler = new Handler(Looper.getMainLooper());

        final String fullText = spanned.toString();
        final int[] index = {0};
        textView.setText("");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    textView.setText(fullText.substring(0, index[0]));
                    index[0]++;
                    typingHandler.postDelayed(this, delayMs);
                } else {
                    textView.setText(spanned);
                }
            }
        };
        typingHandler.post(runnable);
    }

    // -------------------- Slogan Typewriter Animation --------------------

    /**
     * Starts the looping slogan animation on tvSlogan.
     * Each slogan is typed char-by-char, then the final "." blinks 3×,
     * then we move to the next slogan.
     */
    private void startSloganLoop() {
        if (sloganHandler != null) sloganHandler.removeCallbacksAndMessages(null);
        sloganHandler = new Handler(Looper.getMainLooper());
        currentSloganIndex = 0;
        animateSlogan();
    }

    private void animateSlogan() {
        if (!sloganVisible || tvSlogan == null) return;

        String full = SLOGANS[currentSloganIndex];

        // Separate body (without trailing dot) and dot
        final String body;
        final boolean hasDot;
        if (full.endsWith(".")) {
            body = full.substring(0, full.length() - 1);
            hasDot = true;
        } else {
            body = full;
            hasDot = false;
        }

        tvSlogan.setText("");
        final int[] index = {0};
        // Type speed: ~40ms per char feels natural for big display text
        final int charDelay = 40;

        Runnable typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!sloganVisible) return;
                if (index[0] <= body.length()) {
                    tvSlogan.setText(body.substring(0, index[0]));
                    index[0]++;
                    sloganHandler.postDelayed(this, charDelay);
                } else {
                    // Typing done — now blink the dot then proceed
                    if (hasDot) {
                        blinkDot(body, 0);
                    } else {
                        scheduleNextSlogan();
                    }
                }
            }
        };
        sloganHandler.post(typingRunnable);
    }

    /**
     * Blinks the "." at the end of the slogan 3 times (on/off each 600ms),
     * leaves it visible at the end, then schedules the next slogan.
     */
    private void blinkDot(String body, int blinkCount) {
        if (!sloganVisible || tvSlogan == null) return;

        final int totalBlinks = 3; // 3 full on-off cycles
        final int blinkOnMs  = 600;
        final int blinkOffMs = 600;

        if (blinkCount >= totalBlinks) {
            // Leave dot visible, then go to next
            tvSlogan.setText(body + ".");
            scheduleNextSlogan();
            return;
        }

        // Show dot OFF
        tvSlogan.setText(body);
        sloganHandler.postDelayed(() -> {
            if (!sloganVisible) return;
            // Show dot ON
            tvSlogan.setText(body + ".");
            sloganHandler.postDelayed(() ->
                    blinkDot(body, blinkCount + 1), blinkOnMs);
        }, blinkOffMs);
    }

    /** Waits 800ms then advances to the next slogan */
    private void scheduleNextSlogan() {
        sloganHandler.postDelayed(() -> {
            if (!sloganVisible) return;
            currentSloganIndex = (currentSloganIndex + 1) % SLOGANS.length;
            animateSlogan();
        }, 800);
    }

    /** Fades out the slogan TextView over 400ms and stops the loop */
    private void hideSloganAnimated() {
        if (tvSlogan == null || tvSlogan.getVisibility() != View.VISIBLE) return;
        sloganVisible = false;
        if (sloganHandler != null) sloganHandler.removeCallbacksAndMessages(null);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(tvSlogan, "alpha", 2.8f, 0f);
        fadeOut.setDuration(400);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tvSlogan.setVisibility(View.GONE);

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
                        if (!messages.isEmpty()) {
                            chatRecycler.scrollToPosition(messages.size() - 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        fadeOut.start();
    }

    // -------------------- onCreate --------------------
    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        drawerLayout   = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        burgerIcon     = findViewById(R.id.burger_icon);
        tvSlogan       = findViewById(R.id.tv_slogan);

        updateDrawerMenuTitles();

        burgerIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                finish();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_saved) {
                startActivity(new Intent(this, Saved.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_aboutapp) {
                startActivity(new Intent(this, AboutApp.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_aboutus) {
                startActivity(new Intent(this, AboutUs.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;
            } else {
                return false;
            }
        });

        EditText userInput = findViewById(R.id.user_input);
        sendButton         = findViewById(R.id.send_button);
        loadingBar         = findViewById(R.id.loading_bar);
        chatRecycler       = findViewById(R.id.chatRecycler);
        btnModelSelector   = findViewById(R.id.btn_model_selector);

        // ── Load saved model preference ──
        modelPrefs   = getSharedPreferences("model_prefs", MODE_PRIVATE);
        currentModel = modelPrefs.getString(PREF_SELECTED_MODEL, MODEL_GEMINI);
        updateModelButton();

        // ── Model selector click → show dialog ──
        btnModelSelector.setOnClickListener(v -> showModelDialog());

        sp       = getSharedPreferences("chat_cache", MODE_PRIVATE);
        messages = new ArrayList<>();
        adapter  = new ChatAdapter(messages);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatRecycler.setLayoutManager(lm);
        chatRecycler.setAdapter(adapter);

        // Init Gemini (always init it; we switch routing at send time)
        initGemini();



        // ── Always show slogan on open, hide when user focuses EditText ──
        tvSlogan.setVisibility(View.VISIBLE);
        tvSlogan.setAlpha(1f);
        sloganVisible = true;
        startSloganLoop();

        userInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && sloganVisible) {
                hideSloganAnimated();
            }
        });

        // Send button
        sendButton.setOnClickListener(v -> {
            String query = userInput.getText().toString().trim();
            if (query.isEmpty()) return;

            // Also hide slogan if somehow still visible when send is pressed
            if (sloganVisible) {
                hideSloganAnimated();
            }

            messages.add(new Message(query, true));
            adapter.notifyItemInserted(messages.size() - 1);
            chatRecycler.scrollToPosition(messages.size() - 1);
            userInput.setText("");
            saveChatCache();

            loadingBar.setVisibility(View.VISIBLE);
            sendButton.setEnabled(false);

            // ── Route to correct model ──
            if (currentModel.equals(MODEL_QAMAR)) {
                sendMessageQamar(query);
            } else {
                sendMessageGemini(query);
            }
        });
    }

    // -------------------- Model Dialog --------------------
    private void showModelDialog() {
        String[] modelNames = {"🤖 Gemini (Google)", "🌙 Qamar AI"};
        int checkedItem = currentModel.equals(MODEL_GEMINI) ? 0 : 1;

        new AlertDialog.Builder(this)
                .setTitle("AI Model saýla")
                .setSingleChoiceItems(modelNames, checkedItem, (dialog, which) -> {
                    currentModel = (which == 0) ? MODEL_GEMINI : MODEL_QAMAR;
                    modelPrefs.edit().putString(PREF_SELECTED_MODEL, currentModel).apply();
                    updateModelButton();
                    dialog.dismiss();
                })
                .setNegativeButton("Ýap", null)
                .show();
    }

    // Update button emoji/label to reflect active model
    private void updateModelButton() {
        if (currentModel.equals(MODEL_QAMAR)) {
            btnModelSelector.setText("🌙");
        } else {
            btnModelSelector.setText("🤖");
        }
    }

    // -------------------- Init Gemini --------------------
    private void initGemini() {
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.maxOutputTokens = 500;
        configBuilder.temperature = 0.7f;

        Content systemInstruction = new Content.Builder()
                .addText(SYSTEM_PROMPT)
                .build();

        GenerativeModel model = new GenerativeModel(
                GEMINI_MODEL,
                BuildConfig.GEMINI_API_KEY,
                configBuilder.build(),
                null,
                new RequestOptions(),
                null,
                null,
                systemInstruction
        );

        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(model);
        chat = modelFutures.startChat();
    }

    // -------------------- Send via Gemini --------------------
    private void sendMessageGemini(String query) {
        Content userContent = new Content.Builder().addText(query).build();
        ListenableFuture<GenerateContentResponse> future = chat.sendMessage(userContent);

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiText = result.getText();
                if (aiText == null) aiText = "❌ Jogap boş geldi.";
                final String finalText = aiText;
                runOnUiThread(() -> onAiResponse(finalText));
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                final String error = "❌ Haýyş edýäs, internediňizi barlaň ýa-da soňrak täzeden synanyşyň";
                runOnUiThread(() -> onAiResponse(error));
            }
        }, executor);
    }

    // -------------------- Send via Qamar --------------------
    private void sendMessageQamar(String query) {
        executor.execute(() -> {
            try {
                // Build messages array: system + conversation history + new user message
                JSONArray messagesArray = new JSONArray();

                // System message
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", SYSTEM_PROMPT);
                messagesArray.put(systemMsg);

                // Add last 20 messages from history as context
                int historyStart = Math.max(0, messages.size() - 20);
                for (int i = historyStart; i < messages.size(); i++) {
                    Message m = messages.get(i);
                    JSONObject histMsg = new JSONObject();
                    histMsg.put("role", m.isUser ? "user" : "assistant");
                    histMsg.put("content", m.text);
                    messagesArray.put(histMsg);
                }

                // Current user message
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", query);
                messagesArray.put(userMsg);

                // Build request body
                JSONObject body = new JSONObject();
                body.put("model", QAMAR_MODEL);
                body.put("messages", messagesArray);
                body.put("max_tokens", 500);
                body.put("temperature", 0.7);
                body.put("stream", false);

                // HTTP POST
                URL url = new URL(QAMAR_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + BuildConfig.QAMAR_API_KEY);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(input);
                }

                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();

                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);
                    br.close();

                    JSONObject json = new JSONObject(response.toString());
                    String aiText = json
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    final String finalText = aiText.trim();
                    runOnUiThread(() -> onAiResponse(finalText));
                } else {
                    // Read error body
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);
                    br.close();

                    final String error = "❌ Haýyş edýäs, internediňizi barlaň ýa-da soňrak täzeden synanyşyň";
                    runOnUiThread(() -> onAiResponse(error));
                }

                conn.disconnect();

            } catch (Exception e) {
                final String error = "❌ Haýyş edýäs, internediňizi barlaň ýa-da soňrak täzeden synanyşyň";
                runOnUiThread(() -> onAiResponse(error));
            }
        });
    }

    // -------------------- Shared response handler --------------------
    private void onAiResponse(String text) {
        loadingBar.setVisibility(View.GONE);
        sendButton.setEnabled(true);
        messages.add(new Message(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        chatRecycler.scrollToPosition(messages.size() - 1);
        saveChatCache();
    }

    // -------------------- Markdown → Spanned --------------------
    private Spanned markdownToSpanned(String markdown) {
        if (markdown == null)
            return HtmlCompat.fromHtml("", HtmlCompat.FROM_HTML_MODE_LEGACY);

        String html = markdown
                .replaceAll("(?m)^###### (.+)$", "<small><b>$1</b></small>")
                .replaceAll("(?m)^##### (.+)$", "<b>$1</b>")
                .replaceAll("(?m)^#### (.+)$", "<b><big>$1</big></b>")
                .replaceAll("(?m)^### (.+)$", "<b><big><big>$1</big></big></b>")
                .replaceAll("(?m)^## (.+)$", "<b><big><big><big>$1</big></big></big></b>")
                .replaceAll("(?m)^# (.+)$", "<b><big><big><big><big>$1</big></big></big></big></b>")

                // ─── Bold + Italic combined ───
                .replaceAll("\\*\\*\\*(.+?)\\*\\*\\*", "<b><i>$1</i></b>")
                .replaceAll("___(.+?)___", "<b><i>$1</i></b>")

                // ─── Bold ───
                .replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>")
                .replaceAll("__(.+?)__", "<b>$1</b>")

                // ─── Italic ───
                .replaceAll("\\*(.+?)\\*", "<i>$1</i>")
                .replaceAll("_(.+?)_", "<i>$1</i>")

                // ─── Strikethrough ───
                .replaceAll("~~(.+?)~~", "<strike>$1</strike>")

                // ─── Code blocks (``` ... ```) MUST come before inline code ───
                .replaceAll("(?s)```[a-zA-Z]*\\n(.*?)```", "<br><tt>$1</tt><br>")
                .replaceAll("(?s)```(.*?)```", "<br><tt>$1</tt><br>")

                // ─── Inline code ───
                .replaceAll("`(.+?)`", "<tt>$1</tt>")

                // ─── Blockquote ───
                .replaceAll("(?m)^> (.+)$", "<blockquote>$1</blockquote>")

                // ─── Horizontal rule ───
                .replaceAll("(?m)^---$", "<br>──────────────<br>")
                .replaceAll("(?m)^\\*\\*\\*$", "<br>──────────────<br>")
                .replaceAll("(?m)^___$", "<br>──────────────<br>")

                // ─── Unordered lists ───
                .replaceAll("(?m)^[•\\-\\*] (.+)$", "&#8226; $1<br>")

                // ─── Ordered lists ───
                .replaceAll("(?m)^\\d+\\. (.+)$", "&#8226; $1<br>")

                // ─── Checkboxes ───
                .replaceAll("(?m)^- \\[x\\] (.+)$", "☑ $1<br>")
                .replaceAll("(?m)^- \\[ \\] (.+)$", "☐ $1<br>")

                // ─── Links ───
                .replaceAll("\\[(.+?)\\]\\((.+?)\\)", "<a href=\"$2\">$1</a>")

                // ─── Images (remove, TextView can't render) ───
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")

                // ─── Line breaks ───
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
                o.put("text", messages.get(i).text);
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
        if (typingHandler != null) typingHandler.removeCallbacksAndMessages(null);
        if (sloganHandler != null) sloganHandler.removeCallbacksAndMessages(null);
    }

    private void updateDrawerMenuTitles() {
        NavigationView navigationView = findViewById(R.id.navigation_view);
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
}