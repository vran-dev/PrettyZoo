package cc.cc1234.app.util;

import cc.cc1234.app.checker.Checkers;
import org.junit.Assert;
import org.junit.Test;

public class CheckersTest {

    @Test
    public void testIfHostNotMatch() {
        Checkers.ifHostNotMatch("127.0.0.1:2181", Assert::fail);
        Checkers.ifHostNotMatch("192.168.1.1:65535", Assert::fail);
        Checkers.ifHostNotMatch("127.0.0.1:1", Assert::fail);
        Checkers.ifHostNotMatch("http://zk.dev:80", Assert::fail);
        Checkers.ifHostNotMatch("http://zk.dev:0", Assert::fail);

        Checkers.ifHostNotMatch("", () -> Assert.assertTrue(true));
        Checkers.ifHostNotMatch("127.0.0.1", () -> Assert.assertTrue(true));
        Checkers.ifHostNotMatch("http://zk.dev", () -> Assert.assertTrue(true));
        Checkers.ifHostNotMatch("abcd:", () -> Assert.assertTrue(true));
        Checkers.ifHostNotMatch("127.0.0.1:", () -> Assert.assertTrue(true));
        Checkers.ifHostNotMatch(null, () -> Assert.assertTrue(true));
    }

}
