package cc.cc1234.main.util;

import java.net.URL;

public class FXMLs {

    public static URL load(String path) {
        return Thread.currentThread().getContextClassLoader().getResource(path);
    }

}
