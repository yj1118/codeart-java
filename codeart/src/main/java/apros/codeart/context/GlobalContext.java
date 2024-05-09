package apros.codeart.context;

import java.util.Locale;

public final class GlobalContext {

	private GlobalContext() {
	}

	public static Locale locale() {
		return Locale.getDefault();
	}

//	/**
//	 * 设置当前会话上下文的语言
//	 * 
//	 * @param value1
//	 */
//	private static Locale getLocale(String language) {
//		if (Strings.isNullOrEmpty(language))
//
//			return localeUtil.get(language);
//	}

//	#region 语言和身份（全局）

//	/**
//	 * 系统本地的身份（也就是通过配置文件配置的身份）
//	 */
//	public final static DTObject _identity;
//
//	public static DTObject identity() {
//		return _identity;
//	}
//
//	private static DTObject getIdentity() {
//		var section = AppConfig.section("identity");
//		if (section == null)
//			return DTObject.Empty;
//		var identity = DTObject.editable();
//		identity.setString("name", section.getString("name"));
//		identity.setString("language", section.getString("language"));
//		return identity.asReadOnly();
//	}

//	#endregion

}
