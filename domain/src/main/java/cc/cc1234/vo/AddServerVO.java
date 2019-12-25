package cc.cc1234.vo;

import cc.cc1234.context.ApplicationContext;
import cc.cc1234.model.ZkServerConfig;
import cc.cc1234.service.PrettyZooConfigService;
import com.google.common.base.Strings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddServerVO {

    private PrettyZooConfigService prettyZooConfigService = ApplicationContext.get().getBean(PrettyZooConfigService.class);

    private StringProperty host = new SimpleStringProperty();

    private StringProperty acl = new SimpleStringProperty();

    public void onConfirm() {
        final ZkServerConfig zkServerConfig = new ZkServerConfig();
        zkServerConfig.setHost(getHost());
        if (!Strings.isNullOrEmpty(acl.get())) {
            final List<String> acls = Arrays.stream(acl.get().split("\n"))
                    .filter(acl -> !Strings.isNullOrEmpty(acl))
                    .collect(Collectors.toList());
            zkServerConfig.getAclList().addAll(acls);
        }
        prettyZooConfigService.add(zkServerConfig);
    }

    public boolean exists() {
        return prettyZooConfigService.contains(getHost());
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    public String getAcl() {
        return acl.get();
    }

    public StringProperty aclProperty() {
        return acl;
    }

    public void setAcl(String acl) {
        this.acl.set(acl);
    }
}
