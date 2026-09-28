package cs.netarium;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.ViewPumpAppCompatDelegate;

import java.util.Locale;

import dev.b3nedikt.restring.Restring;

public class MainActivity extends AppCompatActivity {

    protected void attachBaseContext(Context newBase) {
        // Подменяем контекст на контекст с французской локалью
//        Context localizedContext = setAppLocale(newBase, getLanguage());
        Context localizedContext = setAppLocale(newBase, "fr");
        super.attachBaseContext(localizedContext);
    }

    // 2. Вспомогательный метод для смены локали
    private Context setAppLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();

        // Для Android 7.0 (API 24) и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        }
        return context;
   }

//    private String language = "ru";


    private AppCompatDelegate appCompatDelegate;

    // Кэш решения: отключён ли Restring для текущего языка (ru/en).
    // Вычисляется лениво, т.к. getResources()/getDelegate() вызываются раньше onCreate().
    private Boolean _restringDisabled = false;

    /**
     * Возвращает true, если для текущего языка (русский или английский) система
     * переводов Restring должна быть полностью отключена: не инициализироваться и
     * не оборачивать ресурсы/контекст. В этом случае строки берутся напрямую из
     * штатных ресурсов приложения (res/values, res/values-en), без кэша Restring.
     */
    private boolean restringDisabled() {
        if (_restringDisabled == null) {
            _restringDisabled = getLanguage().equals("en") || getLanguage().equals("ru");
        }
        return _restringDisabled;
    }

    private AppCompatDelegate getAppCompatDelegate() {
        // Для ru/en не используем ViewPump/Reword-обёртку — Restring не задействован.
        if (restringDisabled())
            return super.getDelegate();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setLanguage("es");

        // Подключаем нашу разметку
        setContentView(R.layout.activity_main);

        // Находим TextView, которым нужно задать текст из кода
        TextView text3 = findViewById(R.id.text3);
        TextView text4 = findViewById(R.id.text4);

        // Задаем текст из кода
        text3.setText(R.string.string3);
        text4.setText(R.string.string4);
    }

    public String getLanguage() {
        return ((MyApplication)getApplication()).getLanguage();
    }

//    public void setLanguage(String language) {
//        this.language = language;
//
//        TranslationHelper.loadLanguage(this, language);
//    }

    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        return getAppCompatDelegate();
    }

    @Override
    public Resources getResources() {
        // Для ru/en возвращаем штатные ресурсы без обёртки Restring.
        if (restringDisabled())
            return super.getResources();
        return Restring.wrapResources(getApplicationContext(), super.getResources());
    }
}