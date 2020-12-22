package cc.cc1234.app.fp;

@FunctionalInterface
public interface CheckedSupplier<R> {

    R get() throws Exception;

}
