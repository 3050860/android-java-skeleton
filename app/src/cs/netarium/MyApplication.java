package cs.netarium;

import android.app.Application;
import android.app.LocaleManager;
import android.os.Build;
import android.os.LocaleList;
import android.util.Log;

import java.util.Locale;

import dev.b3nedikt.restring.Restring;
import dev.b3nedikt.restring.repository.MemoryStringsRepository;
import dev.b3nedikt.reword.RewordInterceptor;
import dev.b3nedikt.viewpump.ViewPump;

public class MyApplication extends Application {

    private static final String TAG = "LANGLOOP";
    private static final String PREFS = "app_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "ru";

    private static volatile String sLanguage = DEFAULT_LANGUAGE;

    @Override
    public void onCreate() {
        super.onCreate();

        sLanguage = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);

        Restring.init(this);
        Restring.stringRepository = new MemoryStringsRepository();
        Restring.setLocaleProvider(new AppLocaleProvider());
        ViewPump.init(RewordInterceptor.INSTANCE);

        clearSystemAppLocales();

        // Строки для текущего не-нативного языка грузим один раз при старте процесса.
        TranslationHelper.loadLanguage(this, sLanguage);

        Log.d(TAG, "@@@ Application.onCreate sdk=" + Build.VERSION.SDK_INT
                + " deviceLocale=" + Locale.getDefault()
                + " storedLanguage=" + sLanguage
                + " isNative=" + isNativeLocale()
                + " restringLocale=" + Restring.getLocale());
    }

    /**
     * На Android 13+ после прежних экспериментов с AppCompatDelegate.setApplicationLocales
     * в системе мог остаться per-app язык. Он конфликтует с нашим выбором, поэтому сбрасываем.
     */
    private void clearSystemAppLocales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            LocaleManager localeManager = getSystemService(LocaleManager.class);
            if (localeManager != null && !localeManager.getApplicationLocales().isEmpty()) {
                Log.d(TAG, "@@@ clearing system app locales: " + localeManager.getApplicationLocales());
                localeManager.setApplicationLocales(LocaleList.getEmptyLocaleList());
            }
        }
    }

    public static String getLanguage() {
        return sLanguage;
    }

    public void setLanguage(String language) {
        Log.d(TAG, "@@@ setLanguage " + sLanguage + " -> " + language);
        sLanguage = language;
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit().putString(KEY_LANGUAGE, language).apply();
        TranslationHelper.loadLanguage(this, language);
    }

    public static boolean isNativeLocale() {
        return TranslationHelper.isNativeLanguage(sLanguage);
    }
}
