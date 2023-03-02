open module specification.main {
    requires static lombok;
    exports cc.cc1234.specification.connection;
    exports cc.cc1234.specification.config;
    exports cc.cc1234.specification.config.model;
    exports cc.cc1234.specification.node;
    exports cc.cc1234.specification.listener;
    exports cc.cc1234.specification.util;
    requires merged.zookeeper;
}
