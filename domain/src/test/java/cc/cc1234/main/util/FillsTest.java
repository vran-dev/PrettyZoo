package cc.cc1234.main.util;

import cc.cc1234.util.Fills;
import org.junit.Assert;
import org.junit.Test;

public class FillsTest {

    @Test
    public void testSingleMatch() {
        String origin = "abcdefghij";
        final String findFirst = String.join("",
                Fills.fill(origin, "a", s -> s, s -> "\"" + s + "\""));
        Assert.assertEquals("\"a\"bcdefghij", findFirst);

        final String findLast = String.join("",
                Fills.fill(origin, "j", s -> s, s -> "\"" + s + "\""));
        Assert.assertEquals("abcdefghi\"j\"", findLast);

        final String findLastBefore = String.join("",
                Fills.fill(origin, "i", s -> s, s -> "\"" + s + "\""));
        Assert.assertEquals(findLastBefore, "abcdefgh\"i\"j");
    }

    @Test
    public void testMultiMatch() {
        String origin = " hello world java and javafx";
        final String res = String.join("", Fills.fill(origin, "java", s -> s, s -> "*"));
        Assert.assertEquals(" hello world * and *fx", res);

        String origin2 = "javafx is a GUI framework for java";
        final String re2 = String.join("", Fills.fill(origin2, "java", s -> s, s -> "*"));
        Assert.assertEquals("*fx is a GUI framework for *", re2);
    }

    @Test
    public void testSingleWordMatch() {
        String origin = "helloworld";
        final String res = String.join("", Fills.fill(origin, "l", s -> s, s -> "[" + s + "]"));
        Assert.assertEquals("he[l][l]owor[l]d", res);
    }

    @Test
    public void testNoMatch() {
        String origin = "this is test no match";
        final String res = String.join("", Fills.fill(origin, "java", s -> s, s -> "*"));
        Assert.assertEquals(origin, res);

        final String res2 = String.join("", Fills.fill(origin, " this", s -> s, s -> "*"));
        Assert.assertEquals(origin, res2);
    }

    @Test
    public void testEmptySearch() {
        String origin = "abcdefghi";
        final String res = String.join("", Fills.fill(origin, "", s -> s, s -> "*"));
        Assert.assertEquals(origin, res);

        String origin2 = " hello asdfa asdfkj ";
        final String res2 = String.join("", Fills.fill(origin2, "", s -> s, s -> "*"));
        Assert.assertEquals(origin2, res2);

        String origin3 = "";
        final String res3 = String.join("", Fills.fill(origin3, " ", s -> s, s -> "*"));
        Assert.assertEquals(origin3, res3);

    }
}
