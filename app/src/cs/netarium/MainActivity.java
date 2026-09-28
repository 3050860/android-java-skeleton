package cs.netarium;

import android.content.res.Resources;
import android.os.Bundle;
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

    private AppCompatDelegate appCompatDelegate;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String language = ((MyApplication)getApplication()).getLanguage();
        setLanguage(language);

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
            default:
                message = "Неизвестное действие";
                break;
        }
    }

    public String getLanguage() {
        return ((MyApplication)getApplication()).getLanguage();
    }

    public void changeLanguage(String language) {
        ((MyApplication)getApplication()).setLanguage(language);
        recreate();
    }

    public void setLanguage(String language) {

        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(language);
        AppCompatDelegate.setApplicationLocales(appLocale);
        TranslationHelper.loadLanguage(this, language);
    }

    @NonNull
    @Override
    public AppCompatDelegate getDelegate() {
        return getAppCompatDelegate();
    }

    @Override
    public Resources getResources() {
        return Restring.wrapResources(getApplicationContext(), super.getResources());
    }
}