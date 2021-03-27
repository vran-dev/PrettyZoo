package cc.cc1234.app.highlights.json;

import cc.cc1234.antlr4.json.JSONBaseVisitor;
import cc.cc1234.antlr4.json.JSONParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.fxmisc.richtext.model.StyleSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JsonHighlightsVisitor extends JSONBaseVisitor<List<StyleSpan<Collection<String>>>> {

    private List<StyleSpan<Collection<String>>> spans = new ArrayList<>();

    private int lastIndex = 0;

    @Override
    public List<StyleSpan<Collection<String>>> visitPair(JSONParser.PairContext ctx) {
        final TerminalNode start = ctx.STRING();
        if (start != null) {
            addEmpty(start.getSymbol().getStartIndex());
            addStyleSpan("json-key", start.getText().length());
            lastIndex = start.getSymbol().getStopIndex() + 1;
        }
        return super.visitChildren(ctx);
    }

    @Override
    public List<StyleSpan<Collection<String>>> visitValue(JSONParser.ValueContext ctx) {
        final Token tokenPosition = ctx.getStart();
        addEmpty(tokenPosition.getStartIndex());

        final int length = ctx.getText().length();
        if (ctx.STRING() != null) {
            addStyleSpan("json-string-value", length);
            lastIndex = tokenPosition.getStopIndex() + 1;

        } else if (ctx.NUMBER() != null) {
            addStyleSpan("json-number-value", length);
            lastIndex = tokenPosition.getStopIndex() + 1;

        } else if (ctx.getText().equals("true") || ctx.getText().equals("false")) {
            addStyleSpan("json-boolean-value", length);
            lastIndex = tokenPosition.getStopIndex() + 1;

        } else if (ctx.getText().equals("null")) {
            addStyleSpan("json-null-value", length);
            lastIndex = tokenPosition.getStopIndex() + 1;

        } else {
            addEmpty(tokenPosition.getStopIndex());
        }
        return super.visitChildren(ctx);
    }


    @Override
    protected List<StyleSpan<Collection<String>>> defaultResult() {
        return spans;
    }

    private void addEmpty(int start) {
        if (start > lastIndex) {
            spans.add(new StyleSpan<>(Collections.emptyList(), start - lastIndex));
            lastIndex = start;
        }
    }

    private void addStyleSpan(String style, int length) {
        spans.add(new StyleSpan<>(Collections.singleton(style), length));
    }

}
