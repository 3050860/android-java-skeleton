package cs.netarium;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.ViewPumpAppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import dev.b3nedikt.restring.Restring;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LANGLOOP";
    private static int sInstance = 0;

    private final int instanceId = ++sInstance;
    private AppCompatDelegate appCompatDelegate;

    private String configInfo() {
        Configuration conf = super.getResources().getConfiguration();
        return "config=" + conf
                + " configLocales=" + conf.getLocales()
                + " configLocale=" + conf.getLocales().get(0);
    }

    private String delegateInfo() {
        AppCompatDelegate delegate = getDelegate();
        return "delegate=" + delegate.getClass().getName()
                + "@" + Integer.toHexString(System.identityHashCode(delegate));
    }

    private String appLocaleInfo() {
        return "appLocales=" + AppCompatDelegate.getApplicationLocales()
                + " restringLocale=" + Restring.getLocale()
                + " providerInitial=" + Restring.getLocaleProvider().isInitial();
    }

    private AppCompatDelegate getAppCompatDelegate() {
        if (appCompatDelegate == null) {
            appCompatDelegate = new ViewPumpAppCompatDelegate(
                    super.getDelegate(),
                    this,
                    Restring::wrapContext
            );
        }
        return appCompatDelegate;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        Log.d(TAG, ">>> [#" + instanceId + "] attachBaseContext sdk=" + Build.VERSION.SDK_INT
                + " targetSdk=" + getApplicationInfo().targetSdkVersion
                + " base=" + newBase
                + " activityResources=" + getResources().getClass().getSimpleName());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, ">>> [#" + instanceId + "] onConfigurationChanged new=" + newConfig
                + " newLocales=" + newConfig.getLocales());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "<<< [#" + instanceId + "] onCreate ENTER appLang=" + MyApplication.getLanguage()
                + " isNativeLocale=" + MyApplication.isNativeLocale()
                + " " + appLocaleInfo()
                + " " + configInfo()
                + " savedState=" + (savedInstanceState != null));

        String language = ((MyApplication)getApplication()).getLanguage();
        setLanguage(language);

        super.onCreate(savedInstanceState);

        Log.d(TAG, "<<< [#" + instanceId + "] onCreate AFTER super lang=" + MyApplication.getLanguage()
                + " " + appLocaleInfo()
                + " " + delegateInfo()
                + " " + configInfo());

        // Подключаем нашу разметку
        setContentView(R.layout.activity_main);

        // Находим TextView, которым нужно задать текст из кода
        TextView text3 = findViewById(R.id.text3);
        TextView text4 = findViewById(R.id.text4);

        // Задаем текст из кода
        text3.setText(R.string.string3);
        text4.setText(R.string.string4);

        Button btnShowDialog = findViewById(R.id.btnShowDialog);
        btnShowDialog.setOnClickListener(v -> showActionsDialog());

        Log.d(TAG, "<<< [#" + instanceId + "] onCreate EXIT"
                + " text1=" + ((TextView) findViewById(R.id.text1)).getText()
                + " text2=" + ((TextView) findViewById(R.id.text2)).getText()
                + " text3=" + text3.getText()
                + " text4=" + text4.getText()
                + " button=" + btnShowDialog.getText()
                + " resourcesClass=" + getResources().getClass().getSimpleName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "=== [#" + instanceId + "] onResume " + appLocaleInfo() + " " + configInfo());
    }

    private void showActionsDialog() {
        // Массив строк для пунктов меню
        String[] actions = {
                "Русский",
                "Английский",
                "Испанский",
                "Французский"
        };

        // Создаем AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Выберите язык")
                .setItems(actions, (dialog, which) -> {
                    // which - это индекс выбранного пункта (0, 1, 2 или 3)
                    handleAction(which);
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void handleAction(int actionIndex) {
        String[] l_codes = {
                "ru", "en", "es", "fr"
        };
        String message;
        switch (actionIndex) {
            case 0:
            case 1:
            case 2:
            case 3:
                changeLanguage(l_codes[actionIndex]);
                break;
            default:
                message = "Неизвестное действие";
                break;
        }
    }

    public String getLanguage() {
        return ((MyApplication)getApplication()).getLanguage();
    }

    public void changeLanguage(String language) {
        Log.d(TAG, "!!! [#" + instanceId + "] changeLanguage REQUEST lang=" + language
                + " [before] " + appLocaleInfo() + " " + configInfo() + " " + delegateInfo());

        ((MyApplication)getApplication()).setLanguage(language);

        setLanguage(language);

        Log.d(TAG, "!!! [#" + instanceId + "] changeLanguage DONE lang=" + language
                + " [after] " + appLocaleInfo() + " " + configInfo());

        Log.d(TAG, "!!! [#" + instanceId + "] changeLanguage recreate()");
        recreate();
    }

//    public void setLanguage(String language) {
//        TranslationHelper.loadLanguage(this, language);
//        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(language);
//        AppCompatDelegate.setApplicationLocales(appLocale);
//
//    }
    public void setLanguage(String language) {
        if (TranslationHelper.isNativeLanguage(language)) {
            // ru/en: штатные ресурсы, локаль через AndroidX
            TranslationHelper.loadLanguage(this, language);   // сброс Restring.setLocale
            AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(language));
        } else {
            // es/fr: ресурсы через Restring, локаль через Restring
            TranslationHelper.loadLanguage(this, language);
            AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(language));
        }
        Log.d(TAG, "    [#" + instanceId + "] setLanguage(" + language + ") isNative="
                + TranslationHelper.isNativeLanguage(language)
                + " [after] " + appLocaleInfo() + " " + configInfo());
    }
    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        if (MyApplication.isNativeLocale()) {
            AppCompatDelegate delegate = super.getDelegate();
            Log.d(TAG, "    [#" + instanceId + "] getDelegate() -> NATIVE "
                    + delegate.getClass().getSimpleName()
                    + "@" + Integer.toHexString(System.identityHashCode(delegate)));
            return delegate;  // ru/en -> штатный AppCompat, без Restring
        }
        AppCompatDelegate delegate = getAppCompatDelegate();
        AppCompatDelegate baseDelegate = super.getDelegate();
        Log.d(TAG, "    [#" + instanceId + "] getDelegate() -> VIEWPUMP "
                + delegate.getClass().getSimpleName()
                + "@" + Integer.toHexString(System.identityHashCode(delegate))
                + " base=" + baseDelegate.getClass().getSimpleName()
                + "@" + Integer.toHexString(System.identityHashCode(baseDelegate)));
        return delegate;
    }

    @Override
    public Resources getResources() {
        Resources baseResources = super.getResources();
        boolean isNative = MyApplication.isNativeLocale();
        Resources result = isNative ? baseResources : Restring.wrapResources(this, baseResources);
        // Логируем только когда реально создаётся/подменяется обёртка, чтобы не спамить на каждый вызов
        if (result != baseResources) {
            Log.d(TAG, "    [#" + instanceId + "] getResources -> WRAPPED "
                    + baseResources.getClass().getSimpleName()
                    + "@" + Integer.toHexString(System.identityHashCode(baseResources))
                    + " -> " + result.getClass().getSimpleName()
                    + "@" + Integer.toHexString(System.identityHashCode(result)));
        }
        return result;
    }
}