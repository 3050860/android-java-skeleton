package cs.netarium;

import java.util.Locale;

import cs.netarium.localization.Language;
import dev.b3nedikt.restring.LocaleProvider;

/**
 * LocaleProvider для Restring, который берёт локаль из выбранного в приложении языка.
 *
 * isInitial() всегда возвращает true: при false Restring в каждом getString()
 * мутирует Configuration активити (ResourcesDelegate.setLocale), что приводило
 * к бесконечному пересозданию активити. Локаль контекста мы выставляем сами
 * в MainActivity.attachBaseContext, а Restring нужна только для выбора строк.
 */
final class AppLocaleProvider implements LocaleProvider {

	@Override
	public boolean isInitial() {
		return true;
	}

	@Override
	public Locale getCurrentLocale() {
		return Language.toLocale(MyApplication.getLanguage());
	}

	@Override
	public void setCurrentLocale(Locale locale) {
		// Не используется: источник истины - язык в MyApplication (SharedPreferences).
	}
}
