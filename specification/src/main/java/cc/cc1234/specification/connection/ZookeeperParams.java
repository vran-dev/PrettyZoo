package cc.cc1234.specification.connection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZookeeperParams {

    private String url;

    private List<String> aclList;

}
