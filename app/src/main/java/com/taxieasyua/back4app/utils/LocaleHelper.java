package com.taxieasyua.back4app.utils;

import java.util.Locale;

public class LocaleHelper {

    public static String getLocale() {
        return Locale.getDefault().getLanguage();
    }
}
