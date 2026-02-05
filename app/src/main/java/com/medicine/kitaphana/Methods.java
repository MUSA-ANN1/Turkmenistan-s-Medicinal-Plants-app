package com.medicine.kitaphana;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.navigation.NavigationView;

public class Methods {
    static void setDrawerMenuFont(NavigationView navigationView, Context context) {
        Typeface typeface = Typeface.create(ResourcesCompat.getFont(context, R.font.framd), Typeface.BOLD);
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);

            // Apply custom font
            SpannableString spanString = new SpannableString(menuItem.getTitle());
            spanString.setSpan(new CustomTypefaceSpan("", typeface), 0, spanString.length(), 0);
            menuItem.setTitle(spanString);

            // If it has a submenu, apply font recursively
            if (menuItem.hasSubMenu()) {
                SubMenu subMenu = menuItem.getSubMenu();
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    SpannableString subSpan = new SpannableString(subMenuItem.getTitle());
                    subSpan.setSpan(new CustomTypefaceSpan("", typeface), 0, subSpan.length(), 0);
                    subMenuItem.setTitle(subSpan);
                }
            }
        }
    }


}
