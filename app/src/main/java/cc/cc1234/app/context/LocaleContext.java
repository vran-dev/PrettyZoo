package cc.cc1234.app.context;

import java.util.Locale;
import java.util.Optional;

public class LocaleContext {

    private static Locale locale = null;

    public static synchronized void set(Locale service) {
        locale = service;
    }

    public static synchronized Locale get() {
        return locale;
    }

    public static synchronized Optional<Locale> getOption() {
        return Optional.ofNullable(locale);
    }

}
