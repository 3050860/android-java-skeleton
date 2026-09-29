package cs.netarium;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

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

        Log.d(TAG, "@@@ Application.onCreate sdk=" + Build.VERSION.SDK_INT
                + " targetSdk=" + getApplicationInfo().targetSdkVersion
                + " javaDefaultLocale=" + Locale.getDefault());

        Restring.init(this);
        Restring.stringRepository = new MemoryStringsRepository();
        ViewPump.init(RewordInterceptor.INSTANCE);

        sLanguage = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);

        Log.d(TAG, "@@@ Application.onCreate storedLanguage=" + sLanguage
                + " isNativeLocale=" + isNativeLocale()
                + " restringLocale=" + Restring.getLocale()
                + " providerInitial=" + Restring.getLocaleProvider().isInitial()
                + " repository=" + Restring.getStringRepository().getClass().getSimpleName()
                + " supportedLocales=" + Restring.getStringRepository().getSupportedLocales()
                + " appLocales=" + AppCompatDelegate.getApplicationLocales());
    }

    public static String getLanguage() {
        return sLanguage;
    }

    public void setLanguage(String language) {
        Log.d(TAG, "@@@ MyApplication.setLanguage " + sLanguage + " -> " + language);
        sLanguage = language;
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit().putString(KEY_LANGUAGE, language).apply();
    }
    public static boolean isNativeLocale() {
        return TranslationHelper.isNativeLanguage(sLanguage);
    }
}
