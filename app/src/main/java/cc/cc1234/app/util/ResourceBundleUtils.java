package cc.cc1234.app.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleUtils {

    public static ResourceBundle get(Locale locale) {
        return ResourceBundle.getBundle("cc.cc1234.i18n.lang", locale);
    }

}
