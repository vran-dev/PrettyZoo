package cc.cc1234.app.highlights.properties;

import cc.cc1234.antlr4.properties.PropertiesBaseVisitor;
import cc.cc1234.antlr4.properties.PropertiesParser;
import org.antlr.v4.runtime.Token;
import org.fxmisc.richtext.model.StyleSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PropertiesHighlightsVisitor extends PropertiesBaseVisitor<List<StyleSpan<Collection<String>>>> {

    private List<StyleSpan<Collection<String>>> spans = new ArrayList<>();

    private int lastIndex = 0;

    @Override
    protected List<StyleSpan<Collection<String>>> defaultResult() {
        return spans;
    }

    @Override
    public List<StyleSpan<Collection<String>>> visitKey(PropertiesParser.KeyContext ctx) {
        final Token position = ctx.getStart();
        addEmpty(position.getStartIndex());
        addStyleSpan("property-key", ctx.getText().length());
        lastIndex = position.getStopIndex() + 1;
        return super.visitChildren(ctx);
    }

    @Override
    public List<StyleSpan<Collection<String>>> visitValue(PropertiesParser.ValueContext ctx) {
        final Token position = ctx.getStart();
        addEmpty(position.getStartIndex());
        addStyleSpan("property-value", ctx.getText().length());
        lastIndex = position.getStopIndex() + 1;
        return super.visitChildren(ctx);
    }

    @Override
    public List<StyleSpan<Collection<String>>> visitComment(PropertiesParser.CommentContext ctx) {
        final Token position = ctx.getStart();
        addEmpty(position.getStartIndex());
        addStyleSpan("property-comment", ctx.getText().length());
        lastIndex = position.getStopIndex() + 1;
        return super.visitChildren(ctx);
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
