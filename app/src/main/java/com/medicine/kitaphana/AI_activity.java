package com.medicine.kitaphana;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.style.ReplacementSpan;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.SpannableBuilder;
import org.commonmark.node.FencedCodeBlock;
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

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.Prism4j;

public class AI_activity extends AppCompatActivity {

    private static final String GEMINI_MODEL        = "gemini-2.5-flash-lite";
    private static final String QAMAR_MODEL         = "Qamar2";
    private static final String QAMAR_API_URL       = "http://api.qamar.asia:7777/v1/chat/completions";
    private static final String PREF_SELECTED_MODEL = "selected_model";
    private static final String MODEL_GEMINI        = "gemini";
    private static final String MODEL_QAMAR         = "qamar";

    private static final String APP_INFO =
            "Türkmenistanyň Dermanlyk Ösümlikleri goşundysy barada maglumat:\n" +
                    "Bu goşundy Türkmenistanyň dermanlyk ösümlikleri atly 16 jiltden ybarat gollanmadyr.\n" +
                    "Diller: türkmen, iňlis, rus, ýapon, ispan.\n" +
                    "Aýratynlyklary: internet bolmasa-da işleýär, gözleg, süzgüç, sahypa belgisi, garaňky tema, köp dilli goldaw, AI kömekçi.\n" +
                    "Ulanylyşy: 17 jilt bar, her kitapda ösümligiň ady, beýany, ylmy ady we bejeriş ulanylşy görkezilýär.\n" +
                    "Talyplara, mugallymlara we tebigy lukmançylyk bilen gyzyklanýanlara niýetlenendir.\n" +
                    "Mazmuny Türkmenistanyň Milli Lideri, Gahryman Arkadag Gurbanguly Berdimuhamedowyň eserinden alyndy.";
    private static final String DEVELOPER_INFO =
            "Bu programmany Musa Annagulyýew döretdi.\n" +
                    "Musa Android developer, CS student, 3D/AR höwesjeňi.\n" +
                    "Doglan senesi: 05.10.2010;\n" +
                    "Ýaşaýan ýeri: Türkmenistan;\n" +
                    "Okaýan mekdebi (2016-2028): Balkan welaýatynyň Balkanabat şäheriniň 3-nji orta mekdebiniň 10-njy B synpy;\n" +
                    "Başarnyklary: Android Studio (Java/Kotlin), Jetpack Compose, AR Foundation, Python, JS, HTML, CSS;\n" +
                    "Habarlaşmak: musa.annaguliev@gmail.com\nTelegram: @Mu4asa\nInstagram: @musa.annaguliev";

    private static final String SYSTEM_PROMPT =
            "Seniň adyň TakykAI 🌿\n" +
                    "Sen peýdaly we dostlukly AI kömekçi.\n" +
                    "Ulanyjy haýsy dilde ýazsa şol dilde jogap ber.\n" +
                    "Jogaplaryňy Markdown formatda we emojiler bilen ýaz.\n" +
                    "Gysgaça we anyk jogap ber.\n" +
                    "ÜNS BER: Eger ulanyja salgy bermeli bolsa, hemişe Gysga (Inline) görnüşünde ber!!! Ulgamly (Reference) görnüşde hiç haçan berme (Ulanyjy soramadyk bolsa salgyň görnüşini aýtma)!!!\n\n" +
                    "Eger ulanyjy bu goşundy ýa-da programma barada sorasa şu maglumaty ulan:\n" +
                    APP_INFO + "\n\n" +
                    "Eger ulanyjy developer ýa-da Musa barada sorasa şu maglumaty ulan (diňe soralanyny ber):\n" +
                    DEVELOPER_INFO;

    private static final String[] SLOGANS = {
            "TakykAI 🌿,\nTiz, Takyk, Düşnükli jogaplar berýär.",
            "TakykAI 🌿,\nAkylly kömekçiňiz, hemişe taýyn.",
            "TakykAI 🌿,\nMaglumaty çalt tapyň, wagt ýitirmäň.",
            "TakykAI 🌿,\nSoragyňyz bar bolsa, TakykAI şu ýerde.",
            "TakykAI 🌿,\nBilim bilen güýçlendirilen kömekçi.",
            "TakykAI 🌿,\nHer soraga dogry jogap."
    };

    private static class Message {
        String text;
        boolean isUser;
        Message(String text, boolean isUser) { this.text = text; this.isUser = isUser; }
    }

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
    private boolean sloganVisible  = true;
    private Markwon markwon;

    DrawerLayout   drawerLayout;
    NavigationView navigationView;
    ImageView      burgerIcon;

    // -------------------- Adapter --------------------
    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Message> msgs;
        ChatAdapter(List<Message> msgs) { this.msgs = msgs; }

        @Override public int getItemViewType(int pos) { return msgs.get(pos).isUser ? 1 : 2; }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == 1 ? R.layout.message_item_user : R.layout.message_item_ai;
            View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return viewType == 1 ? new UserHolder(v) : new AiHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
            Message msg = msgs.get(pos);
            if (holder instanceof AiHolder) {
                TextView tv = ((AiHolder) holder).msg;
                if (pos == msgs.size() - 1 && !msg.isUser) {
                    // Newest AI message: type plain chars, apply Markwon at end
                    typeTextMarkwon(tv, msg.text, 15);
                } else {
                    markwon.setMarkdown(tv, msg.text);
                }
            } else {
                ((UserHolder) holder).msg.setText(msg.text);
            }
        }

        @Override public int getItemCount() { return msgs.size(); }

        class UserHolder extends RecyclerView.ViewHolder {
            TextView msg;
            UserHolder(View v) { super(v); msg = v.findViewById(R.id.messageText); }
        }
        class AiHolder extends RecyclerView.ViewHolder {
            TextView msg;
            AiHolder(View v) { super(v); msg = v.findViewById(R.id.messageText); }
        }
    }

    // -------------------- Markwon typing animation --------------------
    private void typeTextMarkwon(TextView textView, String fullText, int delayMs) {
        if (typingHandler != null) typingHandler.removeCallbacksAndMessages(null);
        typingHandler = new Handler(Looper.getMainLooper());
        final int[] index = {0};
        textView.setText("");

        Runnable runnable = new Runnable() {
            @Override public void run() {
                if (index[0] <= fullText.length()) {
                    String partial = fullText.substring(0, index[0]);
                    // Replace table blocks with plain text placeholder during typing
                    String safePartial = replaceTablesWithPlain(partial);
                    markwon.setMarkdown(textView, safePartial);
                    index[0]++;
                    typingHandler.postDelayed(this, delayMs);
                } else {
                    // Full render with real tables at the end
                    markwon.setMarkdown(textView, fullText);
                }
            }
        };
        typingHandler.post(runnable);
    }


    /**
     * Replaces Markdown table blocks with plain text so Markwon
     * doesn't try to render a half-typed broken table during animation.
     * All other Markdown (headers, bold, links, etc.) stays untouched.
     */
    private String replaceTablesWithPlain(String text) {
        // Match table lines: lines that start and end with |
        // Replace each table line with plain text (strip the | pipes)
        String[] lines = text.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            // Table row or separator line
            if (trimmed.startsWith("|") || trimmed.matches("[-| :]+")) {
                // Strip pipes and dashes, show as plain text
                String plain = trimmed
                        .replaceAll("\\|", " ")
                        .replaceAll("-{2,}", " ")
                        .replaceAll("\\s{2,}", " ")
                        .trim();
                if (!plain.isEmpty()) sb.append(plain).append("\n");
            } else {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    // -------------------- Code Block Background (rounded) --------------------
    static class CodeBlockBackgroundSpan implements android.text.style.LineBackgroundSpan {
        private final int bgColor;
        private final float radius;
        private final float padding;
        private int firstLine = -1;
        private int lastLine  = -1;

        // Store line tops/bottoms to draw one unified rect
        private final java.util.Map<Integer, int[]> lineCoords = new java.util.HashMap<>();

        CodeBlockBackgroundSpan(int bgColor, float radius, float padding) {
            this.bgColor = bgColor;
            this.radius  = radius;
            this.padding = padding;
        }

        @Override
        public void drawBackground(@NonNull android.graphics.Canvas canvas,
                                   @NonNull android.graphics.Paint paint,
                                   int left, int right, int top, int baseline,
                                   int bottom, @NonNull CharSequence text,
                                   int start, int end, int lineNumber) {
            lineCoords.put(lineNumber, new int[]{top, bottom});
            if (firstLine == -1 || lineNumber < firstLine) firstLine = lineNumber;
            if (lineNumber > lastLine) lastLine = lineNumber;

            // Only draw when we're on the last recorded line
            int[] firstCoords = lineCoords.get(firstLine);
            int[] lastCoords  = lineCoords.get(lastLine);

            if (firstCoords == null || lastCoords == null) return;

            android.graphics.Paint bgPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(bgColor);
            android.graphics.RectF rect = new android.graphics.RectF(
                    left   - padding,
                    firstCoords[0] - padding,
                    right  + padding,
                    lastCoords[1]  + padding
            );
            canvas.drawRoundRect(rect, radius, radius, bgPaint);
        }
    }

    // Helper — add this method inside AI_activity
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    // -------------------- Slogan animation --------------------
    private void startSloganLoop() {
        if (sloganHandler != null) sloganHandler.removeCallbacksAndMessages(null);
        sloganHandler = new Handler(Looper.getMainLooper());
        currentSloganIndex = 0;
        animateSlogan();
    }

    private void animateSlogan() {
        if (!sloganVisible || tvSlogan == null) return;
        String full      = SLOGANS[currentSloganIndex];
        final String body   = full.endsWith(".") ? full.substring(0, full.length() - 1) : full;
        final boolean hasDot = full.endsWith(".");
        tvSlogan.setText("");
        final int[] index = {0};
        Runnable r = new Runnable() {
            @Override public void run() {
                if (!sloganVisible) return;
                if (index[0] <= body.length()) {
                    tvSlogan.setText(body.substring(0, index[0]++));
                    sloganHandler.postDelayed(this, 40);
                } else {
                    if (hasDot) blinkDot(body, 0);
                    else        scheduleNextSlogan();
                }
            }
        };
        sloganHandler.post(r);
    }

    private void blinkDot(String body, int count) {
        if (!sloganVisible || tvSlogan == null) return;
        if (count >= 3) { tvSlogan.setText(body + "."); scheduleNextSlogan(); return; }
        tvSlogan.setText(body);
        sloganHandler.postDelayed(() -> {
            if (!sloganVisible) return;
            tvSlogan.setText(body + ".");
            sloganHandler.postDelayed(() -> blinkDot(body, count + 1), 600);
        }, 600);
    }

    private void scheduleNextSlogan() {
        sloganHandler.postDelayed(() -> {
            if (!sloganVisible) return;
            currentSloganIndex = (currentSloganIndex + 1) % SLOGANS.length;
            animateSlogan();
        }, 800);
    }

    private void hideSloganAnimated() {
        if (tvSlogan == null || tvSlogan.getVisibility() != View.VISIBLE) return;
        sloganVisible = false;
        if (sloganHandler != null) sloganHandler.removeCallbacksAndMessages(null);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(tvSlogan, "alpha", 2.5f, 0f);
        fadeOut.setDuration(400);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                tvSlogan.setVisibility(View.GONE);
                // Restore cached messages
                String saved = sp.getString("messages", null);
                if (saved != null) {
                    try {
                        JSONArray arr = new JSONArray(saved);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            messages.add(new Message(obj.getString("text"), obj.getBoolean("isUser")));
                        }
                        adapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) chatRecycler.scrollToPosition(messages.size() - 1);
                    } catch (Exception e) { e.printStackTrace(); }
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

        // ── Init Markwon ──
        // Requires a @PrismBundle annotated class for GrammarLocatorDef to be generated.
        // Add to any class in your project:
        //
        //   import io.noties.prism4j.annotations.PrismBundle;
        //   @PrismBundle(includeAll = true, grammarLocatorClassName = ".GrammarLocatorDef")
        //   public class PrismConfig {}
        //
        // Also in build.gradle (app):
        //   annotationProcessor "io.noties.markwon:syntax-highlight:4.6.2"
        //
        // To skip syntax highlighting temporarily, remove the SyntaxHighlightPlugin line.
        markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(SyntaxHighlightPlugin.create(
                        new Prism4j(new GrammarLocatorDef()),
                        Prism4jThemeDarkula.create()
                ))
                .usePlugin(io.noties.markwon.linkify.LinkifyPlugin.create())
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
                        builder.on(FencedCodeBlock.class, (visitor, fencedCodeBlock) -> {
                            final String lang = (fencedCodeBlock.getInfo() != null
                                    && !fencedCodeBlock.getInfo().trim().isEmpty())
                                    ? fencedCodeBlock.getInfo().trim() : "";
                            final String code = fencedCodeBlock.getLiteral() != null
                                    ? fencedCodeBlock.getLiteral().trim() : "";

                            SpannableBuilder sb = visitor.builder();

                            // ── Whole block start (label + code together for background) ──
                            visitor.builder().append("\n");
                            int blockStart = sb.length();

                            // ── Language label ──
                            int labelStart = sb.length();
                            visitor.builder().append(" " + (lang.isEmpty() ? "CODE" : lang.toUpperCase()) + " \n");
                            int labelEnd = sb.length();
                            sb.setSpan(new android.text.style.ForegroundColorSpan(0xFFAAAAAA), labelStart, labelEnd, 0);
                            sb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), labelStart, labelEnd, 0);
                            sb.setSpan(new android.text.style.TypefaceSpan(String.valueOf(R.font.framd)), labelStart, labelEnd, 0);

                            // ── Code (let SyntaxHighlightPlugin color it via its own spans later) ──
                            visitor.builder().append(code);

                            int blockEnd = sb.length();

                            // ── Apply background + rounded corners to entire block ──
                            sb.setSpan(new CodeBlockBackgroundSpan(0xFF1E1E1E, dpToPx(12), dpToPx(2)), blockStart, blockEnd, 0);
                            sb.setSpan(new android.text.style.TypefaceSpan(String.valueOf(R.font.framd)), blockStart, blockEnd, 0);
                            sb.setSpan(new android.text.style.RelativeSizeSpan(0.70f), blockStart, blockEnd, 0);

                            visitor.builder().append("\n");
                        });
                    }
                })
                .build();

        drawerLayout   = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        burgerIcon     = findViewById(R.id.burger_icon);
        tvSlogan       = findViewById(R.id.tv_slogan);

        updateDrawerMenuTitles();
        burgerIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);
            if (id == R.id.nav_home) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else if (id == R.id.nav_saved)    {
                startActivity(new Intent(this, Saved.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else if (id == R.id.nav_aboutapp) {
                startActivity(new Intent(this, AboutApp.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else if (id == R.id.nav_aboutus)  {
                startActivity(new Intent(this, AboutUs.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
            else return false;
            return true;
        });

        EditText userInput = findViewById(R.id.user_input);
        sendButton         = findViewById(R.id.send_button);
        loadingBar         = findViewById(R.id.loading_bar);
        chatRecycler       = findViewById(R.id.chatRecycler);
        btnModelSelector   = findViewById(R.id.btn_model_selector);

        modelPrefs   = getSharedPreferences("model_prefs", MODE_PRIVATE);
        currentModel = modelPrefs.getString(PREF_SELECTED_MODEL, MODEL_GEMINI);
        updateModelButton();
        btnModelSelector.setOnClickListener(v -> showModelDialog());

        sp       = getSharedPreferences("chat_cache", MODE_PRIVATE);
        messages = new ArrayList<>();
        adapter  = new ChatAdapter(messages);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatRecycler.setLayoutManager(lm);
        chatRecycler.setAdapter(adapter);

        initGemini();

        tvSlogan.setVisibility(View.VISIBLE);
        tvSlogan.setAlpha(1f);
        sloganVisible = true;
        startSloganLoop();

        userInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && sloganVisible) hideSloganAnimated();
        });

        sendButton.setOnClickListener(v -> {
            String query = userInput.getText().toString().trim();
            if (query.isEmpty()) return;
            if (sloganVisible) hideSloganAnimated();

            messages.add(new Message(query, true));
            adapter.notifyItemInserted(messages.size() - 1);
            chatRecycler.scrollToPosition(messages.size() - 1);
            userInput.setText("");
            saveChatCache();

            loadingBar.setVisibility(View.VISIBLE);
            sendButton.setEnabled(false);

            if (currentModel.equals(MODEL_QAMAR)) sendMessageQamar(query);
            else                                  sendMessageGemini(query);
        });
    }

    // -------------------- Model dialog --------------------
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

    private void updateModelButton() {
        btnModelSelector.setText(currentModel.equals(MODEL_QAMAR) ? "🌙" : "🤖");
    }

    // -------------------- Init Gemini --------------------
    private void initGemini() {
        GenerationConfig.Builder cfg = new GenerationConfig.Builder();
        cfg.maxOutputTokens = 500;
        cfg.temperature     = 0.7f;
        Content sys = new Content.Builder().addText(SYSTEM_PROMPT).build();
        GenerativeModel model = new GenerativeModel(
                GEMINI_MODEL, BuildConfig.GEMINI_API_KEY,
                cfg.build(), null, new RequestOptions(), null, null, sys);
        chat = GenerativeModelFutures.from(model).startChat();
    }

    // -------------------- Send via Gemini --------------------
    private void sendMessageGemini(String query) {
        Content userContent = new Content.Builder().addText(query).build();
        ListenableFuture<GenerateContentResponse> future = chat.sendMessage(userContent);
        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override public void onSuccess(GenerateContentResponse result) {
                String t = result.getText();
                if (t == null) t = "❌ Jogap boş geldi.";
                final String ft = t;
                runOnUiThread(() -> onAiResponse(ft));
            }
            @Override public void onFailure(@NonNull Throwable t) {
                runOnUiThread(() -> onAiResponse(
                        "❌ Haýyş edýäs, internediňizi barlaň, modeli çalşyp görüň ýa-da soňrak täzeden synanyşyň"));
            }
        }, executor);
    }

    // -------------------- Send via Qamar --------------------
    private void sendMessageQamar(String query) {
        executor.execute(() -> {
            try {
                JSONArray arr = new JSONArray();
                JSONObject sys = new JSONObject();
                sys.put("role", "system"); sys.put("content", SYSTEM_PROMPT); arr.put(sys);

                int start = Math.max(0, messages.size() - 20);
                for (int i = start; i < messages.size(); i++) {
                    Message m = messages.get(i);
                    JSONObject hm = new JSONObject();
                    hm.put("role", m.isUser ? "user" : "assistant");
                    hm.put("content", m.text);
                    arr.put(hm);
                }
                JSONObject um = new JSONObject();
                um.put("role", "user"); um.put("content", query); arr.put(um);

                JSONObject body = new JSONObject();
                body.put("model", QAMAR_MODEL); body.put("messages", arr);
                body.put("max_tokens", 500); body.put("temperature", 0.7);
                body.put("stream", false);

                URL url = new URL(QAMAR_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + BuildConfig.QAMAR_API_KEY);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                StringBuilder sb = new StringBuilder();

                if (code == 200) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    String aiText = new JSONObject(sb.toString())
                            .getJSONArray("choices").getJSONObject(0)
                            .getJSONObject("message").getString("content");
                    final String ft = aiText.trim();
                    runOnUiThread(() -> onAiResponse(ft));
                } else {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();
                    runOnUiThread(() -> onAiResponse(
                            "❌ Haýyş edýäs, internediňizi barlaň, modeli çalşyp görüň ýa-da soňrak täzeden synanyşyň"));
                }
                conn.disconnect();

            } catch (Exception e) {
                runOnUiThread(() -> onAiResponse(
                        "❌ Haýyş edýäs, internediňizi barlaň, modeli çalşyp görüň ýa-da soňrak täzeden synanyşyň"));
            }
        });
    }

    // -------------------- Response handler --------------------
    private void onAiResponse(String text) {
        loadingBar.setVisibility(View.GONE);
        sendButton.setEnabled(true);
        messages.add(new Message(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        chatRecycler.scrollToPosition(messages.size() - 1);
        saveChatCache();
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    // -------------------- Lifecycle --------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (typingHandler != null) typingHandler.removeCallbacksAndMessages(null);
        if (sloganHandler != null) sloganHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void updateDrawerMenuTitles() {
        NavigationView nav = findViewById(R.id.navigation_view);
        if (nav == null) return;
        Menu menu = nav.getMenu();
        menu.findItem(R.id.nav_home).setTitle(getString(
                getResources().getIdentifier("home"      + MainActivity.currentLanguage, "string", getPackageName())));
        menu.findItem(R.id.nav_settings).setTitle(getString(
                getResources().getIdentifier("settings"  + MainActivity.currentLanguage, "string", getPackageName())));
        menu.findItem(R.id.nav_saved).setTitle(getString(
                getResources().getIdentifier("saved"     + MainActivity.currentLanguage, "string", getPackageName())));
        menu.findItem(R.id.nav_aboutapp).setTitle(getString(
                getResources().getIdentifier("about_app" + MainActivity.currentLanguage, "string", getPackageName())));
        menu.findItem(R.id.nav_aboutus).setTitle(getString(
                getResources().getIdentifier("about_us"  + MainActivity.currentLanguage, "string", getPackageName())));
    }
}