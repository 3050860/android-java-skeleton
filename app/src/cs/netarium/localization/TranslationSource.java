package cs.netarium.localization;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Единственное место, которое знает, где лежат «серверные» данные: список языков
 * и файлы переводов. Сейчас их изображают файлы в assets; когда появится настоящий сервер,
 * меняется только этот класс.
 */
final class TranslationSource {

	static final String LANGUAGE_LIST_FILE = "languages.json";

	private final Context context;

	TranslationSource(Context context) {
		// Файлам в assets язык контекста не важен, поэтому хватает контекста приложения
		this.context = context.getApplicationContext();
	}

	/** Имя файла переводов языка: "es" -> "es.json". */
	static String translationsFile(String languageCode) {
		return languageCode + ".json";
	}

	/** Список языков с сервера (JSON). */
	String languageList() throws IOException {
		return readAsset(LANGUAGE_LIST_FILE);
	}

	/** Переводы языка (JSON). */
	String translations(String languageCode) throws IOException {
		return readAsset(translationsFile(languageCode));
	}

	private String readAsset(String fileName) throws IOException {
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
