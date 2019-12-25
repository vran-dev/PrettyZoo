package cc.cc1234.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PrettyZooConfig {

    private List<ZkServerConfig> servers = new ArrayList<>();

}
