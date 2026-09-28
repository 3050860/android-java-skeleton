package cs.netarium;

import android.app.Application;

import java.util.Locale;

import dev.b3nedikt.restring.Restring;
import dev.b3nedikt.restring.repository.MemoryStringsRepository;
import dev.b3nedikt.reword.RewordInterceptor;
import dev.b3nedikt.viewpump.ViewPump;

public class MyApplication extends Application {

    private static final String PREFS = "app_prefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "ru";

    private static volatile String sLanguage = DEFAULT_LANGUAGE;

    @Override
    public void onCreate() {
        super.onCreate();

        Restring.init(this);
        Restring.stringRepository = new MemoryStringsRepository();
        ViewPump.init(RewordInterceptor.INSTANCE);

        sLanguage = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public static String getLanguage() {
        return sLanguage;
    }

    public void setLanguage(String language) {
        sLanguage = language;
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit().putString(KEY_LANGUAGE, language).apply();
    }
    public static boolean isNativeLocale() {
        return TranslationHelper.isNativeLanguage(sLanguage);
    }
}
