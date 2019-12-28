package cc.cc1234.app.vo;

import cc.cc1234.facade.PrettyZooFacade;
import cc.cc1234.spi.config.model.ServerConfig;
import com.google.common.base.Strings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddServerVO {

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private StringProperty host = new SimpleStringProperty();

    private StringProperty acl = new SimpleStringProperty();

    public void onConfirm() {
        final ServerConfig zkServerConfig = new ServerConfig();
        zkServerConfig.setHost(getHost());
        if (!Strings.isNullOrEmpty(acl.get())) {
            final List<String> acls = Arrays.stream(acl.get().split("\n"))
                    .filter(acl -> !Strings.isNullOrEmpty(acl))
                    .collect(Collectors.toList());
            zkServerConfig.getAclList().addAll(acls);
        }
        prettyZooFacade.addConfig(zkServerConfig);
    }

    public boolean exists() {
        return prettyZooFacade.hasServerConfig(getHost());
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
