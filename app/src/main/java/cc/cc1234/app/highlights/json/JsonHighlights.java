package cc.cc1234.app.highlights.json;

import cc.cc1234.antlr4.json.JSONLexer;
import cc.cc1234.antlr4.json.JSONParser;
import cc.cc1234.app.highlights.SyntaxParseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsonHighlights {

    public static StyleSpans<Collection<String>> compute(String json) {
        if (json == null || json.isBlank()) {
            return StyleSpans.singleton(Collections.singleton(""), 0);
        }
        var lexer = new JSONLexer(CharStreams.fromString(json));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new SyntaxParseErrorListener());
        var tokenStream = new CommonTokenStream(lexer);
        var jsonParser = new JSONParser(tokenStream);
        jsonParser.addErrorListener(new SyntaxParseErrorListener());
        final JSONParser.JsonContext jc = jsonParser.json();
        final List<StyleSpan<Collection<String>>> spans = jc.accept(new JsonHighlightsVisitor());
        final StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
        if (spans.isEmpty()) {
            builder.add(Collections.emptyList(), json.length());
        } else {
            builder.addAll(spans);
        }
        return builder.create();
    }

}
