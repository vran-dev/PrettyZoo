package cc.cc1234.app.util;

import cc.cc1234.app.context.LocaleContext;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleUtils {

    public static ResourceBundle get(Locale locale) {
        return ResourceBundle.getBundle("cc.cc1234.i18n.lang", locale);
    }

    public static ResourceBundle get() {
        Locale locale = LocaleContext.getOption().orElse(Locale.ENGLISH);
        return ResourceBundle.getBundle("cc.cc1234.i18n.lang", locale);
    }

    public static String getContent(String key) {
        Locale locale = LocaleContext.getOption().orElse(Locale.ENGLISH);
        return ResourceBundle.getBundle("cc.cc1234.i18n.lang", locale)
                .getString(key);
    }
}
