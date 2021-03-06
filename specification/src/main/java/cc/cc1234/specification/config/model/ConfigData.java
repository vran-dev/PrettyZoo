package cc.cc1234.specification.config.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端核心配置
 */
@Data
public class ConfigData {

    private List<ServerConfigData> servers = new ArrayList<>();

}
