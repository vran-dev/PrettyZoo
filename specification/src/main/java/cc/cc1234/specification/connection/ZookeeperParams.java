package cc.cc1234.specification.connection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZookeeperParams {

    private String id;

    private String url;

    private List<String> aclList;

    @Builder.Default
    private int maxRetries = 2;

    @Builder.Default
    private int retryIntervalTime = 1000;

    /**
     * mill seconds
     */
    @Builder.Default
    private int connectionTimeout = 5000;

    /**
     * mill seconds
     */
    @Builder.Default
    private int sessionTimeout = 6000;

}
