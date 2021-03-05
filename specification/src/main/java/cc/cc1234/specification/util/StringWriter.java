package cc.cc1234.specification.util;

import java.io.IOException;

public interface StringWriter {

    default void write(byte[] bytes) throws IOException {

    }

    default void write(String str) throws IOException {

    }
}
