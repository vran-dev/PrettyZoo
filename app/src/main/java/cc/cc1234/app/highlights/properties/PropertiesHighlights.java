package cc.cc1234.app.highlights.properties;

import cc.cc1234.antlr4.properties.PropertiesLexer;
import cc.cc1234.antlr4.properties.PropertiesParser;
import cc.cc1234.app.highlights.SyntaxParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;

public class PropertiesHighlights {

    public static StyleSpans<Collection<String>> compute(String properties) {
        var propertiesLexer = new PropertiesLexer(CharStreams.fromString(properties));
        propertiesLexer.addErrorListener(new SyntaxParseErrorListener());
        var tokenStream = new CommonTokenStream(propertiesLexer);
        var propertiesParser = new PropertiesParser(tokenStream);
        propertiesParser.addErrorListener(new SyntaxParseErrorListener());
        var propertiesFileContext = propertiesParser.propertiesFile();
        final var spans = propertiesFileContext.accept(new PropertiesHighlightsVisitor());
        final StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
        if (spans.isEmpty()) {
            builder.add(Collections.singleton("black-text"), properties.length());
        } else {
            builder.addAll(spans);
        }
        return builder.create();
    }
}
