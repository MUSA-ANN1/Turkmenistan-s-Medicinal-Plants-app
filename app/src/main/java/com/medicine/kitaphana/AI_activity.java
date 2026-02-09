package com.medicine.kitaphana;

import android.os.Bundle;
import android.util.Xml;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.GenerateContentResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AI_activity extends AppCompatActivity {

    private EditText etPrompt;
    private TextView tvResult;
    private List<String> knowledgeBase = new ArrayList<>();
    private String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        etPrompt = findViewById(R.id.etPrompt);
        tvResult = findViewById(R.id.tvResult);

        // Load API key from BuildConfig (local.properties)
        apiKey = BuildConfig.GEMINI_API_KEY;

        // Load all strings from XML (RAG knowledge)
        loadStringsFromXML();

        // When user presses enter, ask AI
        etPrompt.setOnEditorActionListener((v, actionId, event) -> {
            String question = etPrompt.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "Write something!", Toast.LENGTH_SHORT).show();
                return true;
            }
            askAI(question);
            return true;
        });
    }

    // ------------------------
    // LOAD STRINGS XML CONTENT
    // ------------------------
    private void loadStringsFromXML() {
        try {
            XmlPullParser parser = getResources().getXml(R.xml.strings);
            int type = parser.getEventType();

            while (type != XmlPullParser.END_DOCUMENT) {
                if (type == XmlPullParser.START_TAG && parser.getName().equals("string")) {
                    knowledgeBase.add(parser.nextText());
                }
                type = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------
    // SIMPLE RAG (KNOWLEDGE PICK)
    // -------------------------
    private String retrieveBestMatch(String userPrompt) {
        userPrompt = userPrompt.toLowerCase();
        String match = "";

        for (String s : knowledgeBase) {
            if (userPrompt.contains("baş") || userPrompt.contains("head")) {
                match = "Baş agyry barada maglumat: " + s;
                break;
            }
            if (userPrompt.contains("suw") || userPrompt.contains("water")) {
                match = "Suwuklyk we saglyk: " + s;
                break;
            }
        }

        if (match.isEmpty()) match = "General info: " + knowledgeBase.get(0);

        return match;
    }

    // -------------------------
    // SEND DATA TO GEMINI
    // -------------------------
    private void askAI(String question) {
        new Thread(() -> {
            try {
                String ragText = retrieveBestMatch(question);

                GenerativeModel gm = new GenerativeModel("gemini-pro", apiKey);
                GenerateContentResponse resp = gm.generateContent(
                        ragText + "\n\nUser asked: " + question
                );

                String result = resp.getText();

                runOnUiThread(() -> tvResult.setText(result));

            } catch (Exception e) {
                runOnUiThread(() -> tvResult.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}
