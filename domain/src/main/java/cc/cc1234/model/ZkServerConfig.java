package cc.cc1234.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ZkServerConfig {

    private String host;

    private int connectTimes = 0;

    private List<String> aclList = new ArrayList<>();
}
