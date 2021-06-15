package cc.cc1234.specification.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 客户端核心配置
 */
@Data
public class ConfigData {

    private List<ServerConfigData> servers = new ArrayList<>();

    private FontConfigData fontConfig;

    private LocalConfigData localConfig = new LocalConfigData();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FontConfigData {

        private Integer fontSize;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocalConfigData {

        // set default value because of   compatibility
        private Lang lang = Lang.ENGLISH;

    }

    public static enum Lang {
        ENGLISH {
            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        },

        SIMPLIFIED_CHINESE {
            @Override
            public Locale getLocale() {
                return Locale.SIMPLIFIED_CHINESE;
            }
        };

        public abstract Locale getLocale();

        public static Lang valueOf(Locale locale) {
            for (Lang value : Lang.values()) {
                if (value.getLocale().getLanguage().equals(locale.getLanguage())) {
                    return  value;
                }
            }
            return null;
        }
    }
}
