package cc.cc1234.app.converter;

import javafx.util.StringConverter;

public class IntegerNumberConverter extends StringConverter<Number> {

    public static IntegerNumberConverter INSTANCE = new IntegerNumberConverter();

    @Override
    public String toString(Number object) {
        return object.toString();
    }

    @Override
    public Number fromString(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
