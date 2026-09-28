package cs.netarium;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import dev.b3nedikt.restring.MutableStringRepository;
import dev.b3nedikt.restring.Restring;
import dev.b3nedikt.restring.StringRepository;
import dev.b3nedikt.restring.repository.CachedStringRepository;

public class TranslationHelper {

    public static void loadLanguage(Context context, String languageCode) {

        // Для русского и английского не используем динамическую систему переводов
        // (JSON из assets + Restring): для этих языков применяются штатные
        // строковые ресурсы Android (res/values, res/values-en).
        if (isNativeLanguage(languageCode)) {
            return;
        }

        Locale locale = createLocale(languageCode);
        String json;

        try {
            try {
                json = loadJsonFromAssets(context, languageCode + ".json");
            } catch (IOException e) {
                e.printStackTrace();
                json = "{}";
            }
            JSONObject translations = new JSONObject(json);

            Map<String, String> stringMap = new HashMap<>();
            Iterator<String> keys = translations.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                stringMap.put(key, translations.getString(key));
            }

            // Заменяем строки для всех локалей или конкретной
            Restring.putStrings(Locale.getDefault(), stringMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает true, если для языка не нужна динамическая система переводов,
     * т.е. язык русский или английский. Для таких языков используются штатные
     * ресурсы приложения, а система Restring не инициализируется и не оборачивает
     * ресурсы/контекст вообще.
     */
    public static boolean isNativeLanguage(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) return false;
        // Берём только код языка (без региона), приводим к нижнему регистру
        String lang = languageCode.split("[-_]")[0].toLowerCase(Locale.ROOT);
        return "ru".equals(lang) || "en".equals(lang);
    }

    private static Locale createLocale(String languageCode) {
        if (languageCode.contains("-") || languageCode.contains("_")) {
            String[] parts = languageCode.split("[-_]");
            return new Locale(parts[0], parts.length > 1 ? parts[1] : "");
        }
        return new Locale(languageCode);
    }

    private static String loadJsonFromAssets(Context context, String fileName) throws IOException {
        // Использование try-with-resources автоматически закроет потоки при ошибке или завершении
        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

}

