package cc.cc1234.main.vo;

import cc.cc1234.main.context.ApplicationContext;
import cc.cc1234.main.model.ZkServerConfig;
import cc.cc1234.main.service.PrettyZooConfigService;
import com.google.common.base.Strings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;

public class AddServerVO {

    private PrettyZooConfigService prettyZooConfigService = ApplicationContext.get().getBean(PrettyZooConfigService.class);

    private StringProperty host = new SimpleStringProperty();

    private StringProperty acl = new SimpleStringProperty();

    public void onConfirm() {
        final ZkServerConfig zkServerConfig = new ZkServerConfig();
        zkServerConfig.setHost(getHost());
        zkServerConfig.getAclList().addAll(Arrays.asList(Strings.nullToEmpty(acl.get()).split("\n")));
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
