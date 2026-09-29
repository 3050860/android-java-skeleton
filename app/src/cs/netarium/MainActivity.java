package cs.netarium;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.ViewPumpAppCompatDelegate;

import dev.b3nedikt.restring.Restring;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "LANGLOOP";
	private static int sInstance = 0;
	private final int instanceId = ++sInstance;

	/**
	 * Режим фиксируется один раз на экземпляр активити (при первом обращении,
	 * т.е. в attachBaseContext). Смена языка всегда идёт через recreate(),
	 * поэтому новый режим применяется только к новому экземпляру, и делегат/ресурсы
	 * не меняются посреди жизненного цикла.
	 */
	private Boolean nativeMode;

	private AppCompatDelegate viewPumpDelegate;
	private Resources restringResources;
	private Resources restringBaseResources;

	private boolean isNativeMode() {
		if (nativeMode == null) {
			nativeMode = MyApplication.isNativeLocale();
		}
		return nativeMode;
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		// Локаль приложения не зависит от локали устройства: подменяем её в конфигурации
		// базового контекста. Для ru/en это выбирает values/values-en, для es/fr задаёт
		// корректные форматы, а строки подставит Restring.
		Configuration config = new Configuration(newBase.getResources().getConfiguration());
		config.setLocale(TranslationHelper.createLocale(MyApplication.getLanguage()));
		super.attachBaseContext(newBase.createConfigurationContext(config));

		Log.d(TAG, ">>> [#" + instanceId + "] attachBaseContext lang=" + MyApplication.getLanguage()
				+ " nativeMode=" + isNativeMode()
				+ " configLocales=" + super.getResources().getConfiguration().getLocales());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		Log.d(TAG, "<<< [#" + instanceId + "] onCreate lang=" + MyApplication.getLanguage()
				+ " nativeMode=" + isNativeMode()
				+ " delegate=" + getDelegate().getClass().getSimpleName()
				+ " resources=" + getResources().getClass().getSimpleName()
				+ " restringLocale=" + Restring.getLocale()
				+ " configLocales=" + getResources().getConfiguration().getLocales()
				+ " savedState=" + (savedInstanceState != null)
				+ " | text1=" + ((TextView) findViewById(R.id.text1)).getText()
				+ " text3=" + text3.getText()
				+ " button=" + btnShowDialog.getText());
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "xxx [#" + instanceId + "] onDestroy nativeMode=" + isNativeMode());
		super.onDestroy();
	}

	private void showActionsDialog() {
		// Пункты меню берём из ресурсов по одному через getString():
		// для ru/en это штатные R.string, для es/fr строки подставляет Restring.
		String[] actions = {
				getString(R.string.lang_ru),
				getString(R.string.lang_en),
				getString(R.string.lang_es),
				getString(R.string.lang_fr)
		};

		// Создаем AlertDialog
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.select_language))
				.setItems(actions, (dialog, which) -> {
					// which - это индекс выбранного пункта (0, 1, 2 или 3)
					handleAction(which);
				})
				.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
				.show();
	}

	private void handleAction(int actionIndex) {
		String[] l_codes = {
				"ru", "en", "es", "fr"
		};
		if (actionIndex >= 0 && actionIndex < l_codes.length) {
			changeLanguage(l_codes[actionIndex]);
		}
	}

	public String getLanguage() {
		return MyApplication.getLanguage();
	}

	public void changeLanguage(String language) {
		if (language.equals(MyApplication.getLanguage())) {
			Log.d(TAG, "!!! [#" + instanceId + "] changeLanguage " + language + " - already active");
			return;
		}
		Log.d(TAG, "!!! [#" + instanceId + "] changeLanguage " + MyApplication.getLanguage()
				+ " -> " + language + ", recreate()");
		((MyApplication) getApplication()).setLanguage(language);
		recreate();
	}

	@NonNull
	@Override
	public AppCompatDelegate getDelegate() {
		if (isNativeMode()) {
			return super.getDelegate();  // ru/en -> штатный AppCompat, без Restring
		}
		if (viewPumpDelegate == null) {
			viewPumpDelegate = new ViewPumpAppCompatDelegate(
					super.getDelegate(),
					this,
					Restring::wrapContext
			);
		}
		return viewPumpDelegate;
	}

	@Override
	public Resources getResources() {
		Resources base = super.getResources();
		if (isNativeMode()) {
			return base;  // ru/en -> штатные ресурсы, без Restring
		}
		// Кешируем обёртку, пересоздаём только если сменились базовые ресурсы.
		if (restringResources == null || restringBaseResources != base) {
			restringBaseResources = base;
			restringResources = Restring.wrapResources(this, base);
		}
		return restringResources;
	}
}
