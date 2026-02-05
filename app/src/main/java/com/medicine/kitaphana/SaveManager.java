package com.medicine.kitaphana;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SaveManager {

    private static final String PREF_NAME = "saved_topics";
    private static final String KEY_SAVED = "saved_keys";

    public static boolean isSaved(Context c, String key) {
        SharedPreferences sp = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = sp.getStringSet(KEY_SAVED, new HashSet<>());
        return set.contains(key);
    }

    public static void toggle(Context c, String key) {
        SharedPreferences sp = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>(sp.getStringSet(KEY_SAVED, new HashSet<>()));

        if (set.contains(key)) {
            set.remove(key);
        } else {
            set.add(key);
        }

        sp.edit().putStringSet(KEY_SAVED, set).apply();
    }

    public static Set<String> getAll(Context c) {
        SharedPreferences sp = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(sp.getStringSet(KEY_SAVED, new HashSet<>()));
    }
}
