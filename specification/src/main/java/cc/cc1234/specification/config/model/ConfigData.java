package cc.cc1234.specification.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端核心配置
 */
@Data
public class ConfigData {

    private List<ServerConfigData> servers = new ArrayList<>();

    private FontConfigData fontConfig;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FontConfigData {

        private Integer fontSize;

    }

}
