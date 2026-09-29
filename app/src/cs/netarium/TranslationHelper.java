package cs.netarium;

import android.content.Context;
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

import dev.b3nedikt.restring.Restring;

public class TranslationHelper {

    private static final String TAG = "LANGLOOP";

    public static void loadLanguage(Context context, String languageCode) {

        // Для русского и английского не используем динамическую систему переводов
        // (JSON из assets + Restring): для этих языков применяются штатные
        // строковые ресурсы Android (res/values, res/values-en).
        if (isNativeLanguage(languageCode)) {
            return;
        }

        Locale locale = createLocale(languageCode);
        String json;
        String error = null;

        try {
            try {
                json = loadJsonFromAssets(context, languageCode + ".json");
            } catch (IOException e) {
                e.printStackTrace();
                json = "{}";
                error = "assets/" + languageCode + ".json not found: " + e;
            }
            JSONObject translations = new JSONObject(json);

            Map<String, String> stringMap = new HashMap<>();
            Iterator<String> keys = translations.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                stringMap.put(key, translations.getString(key));
            }

            Restring.putStrings(locale, stringMap);

            Log.d(TAG, "~~~ loadLanguage(" + languageCode + ") locale=" + locale
                    + " count=" + stringMap.size()
                    + (error != null ? " err=" + error : "")
                    + " supportedLocales=" + Restring.getStringRepository().getSupportedLocales());
        } catch (Exception e) {
            Log.e(TAG, "~~~ loadLanguage(" + languageCode + ") FAILED", e);
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

    static Locale createLocale(String languageCode) {
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

