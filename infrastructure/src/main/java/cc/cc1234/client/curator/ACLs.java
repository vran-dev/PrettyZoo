package cc.cc1234.client.curator;

import org.apache.curator.framework.AuthInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ACLs {

    private static final Pattern DIGEST_PATTERN = Pattern.compile("(?<schema>digest)(\\:)(?<auth>.*)");

    public static AuthInfo parseDigest(String acl) {
        final Matcher matcher = DIGEST_PATTERN.matcher(acl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("illegal acl String: " + acl);
        }
        return new AuthInfo(matcher.group("schema"), matcher.group("auth").getBytes());
    }

}
