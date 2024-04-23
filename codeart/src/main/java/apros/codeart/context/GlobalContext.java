package apros.codeart.context;

import java.util.Locale;

import com.google.common.base.Strings;

import apros.codeart.AppConfig;
import apros.codeart.dto.DTObject;
import apros.codeart.util.StringUtil;
import apros.codeart.util.localeUtil;

public final class GlobalContext {

	private GlobalContext() {
	}

	private final static Locale _locale;

	public static Locale locale() {
		return _locale;
	}

	/**
	 * 设置当前会话上下文的语言
	 * 
	 * @param value1
	 */
	private static Locale getLocale(String language) {
		if (Strings.isNullOrEmpty(language))
			return Locale.getDefault();
		return localeUtil.get(language);
	}

//	#region 语言和身份（全局）

	/**
	 * 系统本地的身份（也就是通过配置文件配置的身份）
	 */
	public final static DTObject _identity;

	public static DTObject identity() {
		return _identity;
	}

	private static DTObject getIdentity() {
		var section = AppConfig.section("identity");
		if (section == null)
			return DTObject.Empty;
		var identity = DTObject.editable();
		identity.setString("name", section.getString("name"));
		identity.setString("language", section.getString("language"));
		return identity.asReadOnly();
	}

//	#endregion

	static {
		_identity = getIdentity();
		_locale = getLocale(_identity.getString("language", StringUtil.empty()));
	}

}
