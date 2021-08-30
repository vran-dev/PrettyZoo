package cc.cc1234.specification.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtil {

    public static String getHostAddress(String host){
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            return host;
        }
    }

    public static String getUrl(String url){
        return getHostAddress(getDomain(url)) + ":" + getPort(url);
    }

    public static String getDomain(String host){
        if(null != host && host.indexOf(":") > -1){
            return host.split(":")[0].trim();
        }
        return null;
    }

    public static Integer getPort(String host){
        if(null != host && host.indexOf(":") > -1){
            return Integer.valueOf(host.split(":")[1].trim());
        }
        return null;
    }

}
