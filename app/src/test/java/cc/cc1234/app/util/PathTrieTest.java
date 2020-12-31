package cc.cc1234.app.util;

import cc.cc1234.app.trie.PathTrie;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class PathTrieTest {

    @Test
    public void testOf() {
        Map<String, String> data = Map.of(
                "/", "/",
                "/home", "home",
                "/home/user", "user",
                "/home/opt", "opt",
                "/home/user/downloads", "downloads",
                "/home/user/image", "image"
        );
        PathTrie<String> pathTrie = PathTrie.of(data);
        Assert.assertTrue(pathTrie.find("/home").isPresent());
        Assert.assertTrue(pathTrie.find("/home/user").isPresent());
        Assert.assertTrue(pathTrie.find("/home/opt").isPresent());
        Assert.assertTrue(pathTrie.find("/home/user/downloads").isPresent());
        Assert.assertTrue(pathTrie.find("/home/user/image").isPresent());
        Assert.assertTrue(pathTrie.find("/home/user/image").isPresent());
        Assert.assertFalse(pathTrie.find("/home/users").isPresent());
        Assert.assertFalse(pathTrie.find("/opt").isPresent());
        Assert.assertFalse(pathTrie.find("/home/opt/downloads").isPresent());
        Assert.assertFalse(pathTrie.find("/hom").isPresent());
    }

    @Test
    public void testAdd() {
        PathTrie<String> pathTrie = new PathTrie<>();
        pathTrie.add("/home", "home");
        pathTrie.add("/home/user", "home-user");
        pathTrie.add("/home/opt", "home-opt");
        pathTrie.add("/home", "home2");
        pathTrie.add("/home/user/vran", "vran");
        pathTrie.add("/test", "test");

        Assert.assertTrue(pathTrie.contains("/home"));
        Assert.assertTrue(pathTrie.contains("/home/user"));
        Assert.assertTrue(pathTrie.contains("/home/opt"));
        Assert.assertTrue(pathTrie.contains("/home/user/vran"));
        Assert.assertTrue(pathTrie.contains("/test"));

        Assert.assertEquals("home2", pathTrie.find("/home").get());
        Assert.assertEquals("home-user", pathTrie.find("/home/user").get());
        Assert.assertEquals("home-opt", pathTrie.find("/home/opt").get());
        Assert.assertEquals("vran", pathTrie.find("/home/user/vran").get());
        Assert.assertEquals("test", pathTrie.find("test").get());
    }

    @Test
    public void testRemove() {
        Map<String, String> data = Map.of(
                "/", "/",
                "/home", "home",
                "/home/user", "user",
                "/home/opt", "opt",
                "/home/user/downloads", "downloads",
                "/home/user/image", "image",
                "/home/virtual/image", "image"
        );
        PathTrie<String> pathTrie = PathTrie.of(data);
        pathTrie.remove("/home/user/downloads");
        Assert.assertFalse(pathTrie.contains("/home/user/downloads"));
        Assert.assertTrue(pathTrie.contains("/home/user"));

        pathTrie.remove("/home/virtual/image");
        Assert.assertTrue(pathTrie.contains("/home/virtual"));
        Assert.assertFalse(pathTrie.find("/home/virtual").isPresent());

        pathTrie.remove("/home");
        Assert.assertFalse(pathTrie.contains("/home/user"));
        Assert.assertFalse(pathTrie.contains("/home/opt"));
        Assert.assertFalse(pathTrie.contains("/home/user/image"));
        Assert.assertFalse(pathTrie.contains("/home/user/downloads"));
    }

    @Test
    public void testSearch() {
        Map<String, String> data = Map.of(
                "/", "/",
                "/home", "home",
                "/home/user", "user",
                "/home/opt", "opt",
                "/home/user/downloads", "downloads",
                "/home/user/image", "image",
                "/home/virtual/image", "image"
        );
        final PathTrie<String> pathTrie = PathTrie.of(data);
        Assert.assertSame(1, pathTrie.search("home").size());
        Assert.assertSame(1, pathTrie.search("hom").size());
        Assert.assertSame(1, pathTrie.search("om").size());
        Assert.assertSame(1, pathTrie.search("user").size());
        Assert.assertSame(2, pathTrie.search("image").size());
        Assert.assertSame(3, pathTrie.search("o").size());
        final List<String> result = pathTrie.search("o");
        Assert.assertTrue(result.contains("home"));
        Assert.assertTrue(result.contains("opt"));
        Assert.assertTrue(result.contains("downloads"));
    }
}
