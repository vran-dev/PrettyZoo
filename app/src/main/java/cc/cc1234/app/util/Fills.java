package cc.cc1234.app.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Fills {

    public static <R> List<R> fill(String origin,
                                   String match,
                                   Function<String, R> notMatchFill,
                                   Function<String, R> matchFill) {
        if (match == null || match.length() == 0) {
            return Collections.singletonList(notMatchFill.apply(origin));
        }
        int pre = 0;
        int pos = origin.indexOf(match);
        List<R> result = new ArrayList<>();
        while (pos != -1) {
            result.add(notMatchFill.apply(origin.substring(pre, pos)));
            result.add(matchFill.apply(origin.substring(pos, pos + match.length())));
            pre = pos + match.length();
            pos = origin.indexOf(match, pre);
        }
        if (pre < origin.length()) {
            result.add(notMatchFill.apply(origin.substring(pre)));
        }
        return result;
    }

}
