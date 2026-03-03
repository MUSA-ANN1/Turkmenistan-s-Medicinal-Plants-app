package com.medicine.kitaphana;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SeasonIconWorker extends Worker {

    // Ramadan dates: {year, startMonth(1-12), startDay, endMonth, endDay}
    private static final int[][] RAMADAN_DATES = {
            {2026,  2, 18,  3, 19},
            {2027,  2,  8,  3,  9},
            {2028,  1, 28,  2, 26},
            {2029,  1, 16,  2, 14},
            {2030,  1,  6,  2,  4},
            {2031, 12, 14,  1, 12},
            {2032, 12,  3,  1,  1},
            {2033, 11, 22, 12, 21},
            {2034, 11, 11, 12, 10},
            {2035, 11,  1, 11, 30},
            {2036, 10, 20, 11, 18},
            {2037, 10, 10, 11,  8}
    };

    public SeasonIconWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = getApplicationContext()
                .getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean autoSeason = prefs.getBoolean("auto_season_icon", false);

        if (autoSeason) {
            String season = getCurrentSeason();
            prefs.edit().putString("current_season", season).apply(); // save for ImageView
            switchIcon(getApplicationContext(), season); // still works since we're inside the class
        }

        return Result.success();
    }

    public static boolean isRamadan() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year  = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH) + 1;
        int day   = cal.get(java.util.Calendar.DAY_OF_MONTH);

        int todayAbs = year * 10000 + month * 100 + day;

        for (int[] r : RAMADAN_DATES) {
            int startYear = r[0];
            int endYear   = (r[1] > r[3]) ? r[0] + 1 : r[0];

            int startAbs = startYear * 10000 + r[1] * 100 + r[2];
            int endAbs   = endYear   * 10000 + r[3] * 100 + r[4];

            if (todayAbs >= startAbs && todayAbs <= endAbs) return true;
        }
        return false;
    }

    public static String getCurrentSeason() {
        // Ramadan has highest priority
        if (isRamadan()) return "ramadan";

        int month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
        if (month == 11 || month == 0 || month == 1) return "winter";
        if (month >= 2 && month <= 4)                return "spring";
        if (month >= 5 && month <= 7)                return "summer";
        return "autumn";
    }

    public static void switchIcon(Context context, String season) {
        String pkg = context.getPackageName();

        String[] aliases = {
                pkg + ".SplashDefault",
                pkg + ".SplashWinter",
                pkg + ".SplashSpring",
                pkg + ".SplashSummer",
                pkg + ".SplashAutumn",
                pkg + ".SplashRamadan"
        };

        String target;
        switch (season) {
            case "winter":  target = pkg + ".SplashWinter";  break;
            case "spring":  target = pkg + ".SplashSpring";  break;
            case "summer":  target = pkg + ".SplashSummer";  break;
            case "autumn":  target = pkg + ".SplashAutumn";  break;
            case "ramadan": target = pkg + ".SplashRamadan"; break;
            default:        target = pkg + ".SplashDefault"; break;
        }

        PackageManager pm = context.getPackageManager();
        for (String alias : aliases) {
            int state = alias.equals(target)
                    ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

            pm.setComponentEnabledSetting(
                    new ComponentName(pkg, alias),
                    state,
                    PackageManager.DONT_KILL_APP
            );
        }
    }
}