package cc.cc1234.app.fp;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Try<T> {

    static <T> Try<T> of(CheckedRunnable runnable) {
        try {
            runnable.run();
            return new Success<>(null);
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    static <T> Try<T> of(CheckedSupplier<T> function) {
        try {
            return new Success<>(function.get());
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    default Try<T> onSuccess(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer);
        if (isSuccess()) {
            consumer.accept(get());
        }
        return this;
    }

    default <X extends Throwable> Try<T> onFailure(Consumer<X> consumer) {
        Objects.requireNonNull(consumer);
        if (isFailure()) {
            consumer.accept((X) getCause());
        }
        return this;
    }

    default <X extends Throwable> Try<T> onFailureMap(Function<X, T> function) {
        Objects.requireNonNull(function);
        try {
            if (isFailure()) {
                return new Success<>(function.apply((X) getCause()));
            }
            return this;
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    default Throwable getCause() {
        throw new UnsupportedOperationException();
    }

    default T get() {
        throw new UnsupportedOperationException();
    }

    default <R> Try<R> flatMap(Function<? super T, ? extends Try<? extends R>> function) {
        if (isFailure()) {
            return (Failure<R>) this;
        } else {
            return (Try<R>) function.apply(get());
        }
    }

    default <R> Try<R> map(Function<? super T, ? extends R> function) {
        if (isFailure()) {
            return (Failure<R>) this;
        } else {
            return new Success<>(function.apply(get()));
        }
    }

    default Try<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isFailure()) {
            return this;
        }

        if (predicate.test(get())) {
            return new Failure<>(new NoSuchElementException());
        } else {
            return this;
        }
    }

    Boolean isSuccess();

    default Boolean isFailure() {
        return !isSuccess();
    }

    final class Failure<T> implements Try<T> {

        private Throwable throwable;

        public Failure(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public Throwable getCause() {
            return throwable;
        }

        @Override
        public Boolean isSuccess() {
            return false;
        }
    }

    final class Success<T> implements Try<T> {

        private T value;

        public Success(T value) {
            this.value = value;
        }

        @Override
        public Try<T> onSuccess(Consumer<? super T> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Boolean isSuccess() {
            return true;
        }
    }
}
