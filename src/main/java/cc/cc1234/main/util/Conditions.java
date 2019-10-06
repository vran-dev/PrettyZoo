package cc.cc1234.main.util;

import java.util.Optional;
import java.util.function.Supplier;

public class Conditions {

    private Optional<Supplier<Boolean>> onPredicate = Optional.empty();

    private Optional<Runnable> thenDo = Optional.empty();

    private Optional<Runnable> elseDo = Optional.empty();


    private Conditions(Supplier<Boolean> onPredicate) {
        this.onPredicate = Optional.of(onPredicate);
    }


    public static Conditions on(Supplier<Boolean> condition) {
        final Conditions conditions = new Conditions(condition);
        return conditions;
    }

    public Conditions thenDo(Runnable thenDo) {
        this.thenDo = Optional.of(thenDo);
        return this;
    }

    public void elseDo(Runnable elseDo) {
        this.elseDo = Optional.of(elseDo);
        execute();
    }

    protected void execute() {
        onPredicate.ifPresent(condition -> {
            if (condition.get()) {
                thenDo.ifPresent(Runnable::run);
            } else {
                elseDo.ifPresent(Runnable::run);
            }
        });
    }
}
