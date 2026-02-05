package com.medicine.kitaphana;

import java.text.Normalizer;
import java.util.Locale;

public class CardItem {

    final String key;
    final String header;
    final String topic;

    // Cached normalized versions (SEARCH USE ONLY)
    final String nKey;
    final String nHeader;
    final String nTopic;

    public CardItem(String key, String header, String topic) {
        this.key = key;
        this.header = header;
        this.topic = topic;

        this.nKey = normalize(key);
        this.nHeader = normalize(header);
        this.nTopic = normalize(topic);
    }

    public String getKey() { return key; }
    public String getHeader() { return header; }
    public String getTopic() { return topic; }

    private static String normalize(String t) {
        if (t == null) return "";
        return Normalizer.normalize(t, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
