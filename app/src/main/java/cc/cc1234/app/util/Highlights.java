package cc.cc1234.app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Highlights {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        if (text == null || text.isBlank()) {
            return StyleSpans.singleton(Collections.singleton(""), 0);
        }
        if (isXml(text)) {
            return XmlHighlights.computeHighlighting(text);
        }
        if (isJson(text)) {
            return JsonHighlights.computeHighlighting(text);
        }
        return StyleSpans.singleton(Collections.singleton(""), text.length());
    }

    private static boolean isXml(String xml) {
        try {
            DocumentBuilder dBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource src = new InputSource(new StringReader(xml));
            dBuilder.parse(src);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private static class JsonHighlights {

        private static final Pattern JSON_PATTERN = Pattern.compile("(?<KEY>\".*\")(?<SEP>\\s*:\\s*)(?<VALUE>(\".*\")|(true|false)|(null)|(\\d+\\.?\\d*))?");

        public static StyleSpans<Collection<String>> computeHighlighting(String text) {
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            Matcher matcher = JSON_PATTERN.matcher(text);
            int last = 0;
            while (matcher.find()) {
                spansBuilder.add(Collections.emptyList(), matcher.start() - last);
                if (matcher.group("KEY") != null) {
                    spansBuilder.add(Collections.singleton("json-key"), matcher.end("KEY") - matcher.start("KEY"));
                    last = matcher.end();
                }

                if (matcher.group("SEP") != null) {
                    spansBuilder.add(Collections.singleton(""), matcher.end("SEP") - matcher.start("SEP"));
                    last = matcher.end();
                }

                if (matcher.group("VALUE") != null) {
                    int start = matcher.start("VALUE");
                    if (start > last) {
                        spansBuilder.add(Collections.singleton(""), start - last);
                    }

                    String result = matcher.group(3);
                    if (matcher.group(4) != null) {
                        spansBuilder.add(Collections.singleton("json-string-value"), result.length());
                    } else if (matcher.group(5) != null) {
                        spansBuilder.add(Collections.singleton("json-boolean-value"), result.length());
                    } else if (matcher.group(6) != null) {
                        spansBuilder.add(Collections.singleton("json-null-value"), result.length());
                    } else if (matcher.group(7) != null) {
                        spansBuilder.add(Collections.singleton("json-number-value"), result.length());
                    }
                    last = matcher.end(3);
                }
                last = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), text.length() - last);
            return spansBuilder.create();
        }
    }

    private static class XmlHighlights {
        private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*/?>))"
                + "|(?<COMMENT><!--[^<>]+-->)");

        private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");

        private static final int GROUP_OPEN_BRACKET = 2;
        private static final int GROUP_ELEMENT_NAME = 3;
        private static final int GROUP_ATTRIBUTES_SECTION = 4;
        private static final int GROUP_CLOSE_BRACKET = 5;
        private static final int GROUP_ATTRIBUTE_NAME = 1;
        private static final int GROUP_EQUAL_SYMBOL = 2;
        private static final int GROUP_ATTRIBUTE_VALUE = 3;

        public static StyleSpans<Collection<String>> computeHighlighting(String text) {

            Matcher matcher = XML_TAG.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            while (matcher.find()) {
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                if (matcher.group("COMMENT") != null) {
                    spansBuilder.add(Collections.singleton("xml-comment"), matcher.end() - matcher.start());
                } else {
                    if (matcher.group("ELEMENT") != null) {
                        String attributesText = matcher.group(GROUP_ATTRIBUTES_SECTION);

                        spansBuilder.add(Collections.singleton("xml-tag-mark"), matcher.end(GROUP_OPEN_BRACKET) - matcher.start(GROUP_OPEN_BRACKET));
                        spansBuilder.add(Collections.singleton("xml-any-tag"), matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET));

                        if (!attributesText.isEmpty()) {

                            lastKwEnd = 0;

                            Matcher amatcher = ATTRIBUTES.matcher(attributesText);
                            while (amatcher.find()) {
                                spansBuilder.add(Collections.emptyList(), amatcher.start() - lastKwEnd);
                                spansBuilder.add(Collections.singleton("xml-attribute"), amatcher.end(GROUP_ATTRIBUTE_NAME) - amatcher.start(GROUP_ATTRIBUTE_NAME));
                                spansBuilder.add(Collections.singleton("xml-tag-mark"), amatcher.end(GROUP_EQUAL_SYMBOL) - amatcher.end(GROUP_ATTRIBUTE_NAME));
                                spansBuilder.add(Collections.singleton("xml-value"), amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
                                lastKwEnd = amatcher.end();
                            }
                            if (attributesText.length() > lastKwEnd)
                                spansBuilder.add(Collections.emptyList(), attributesText.length() - lastKwEnd);
                        }

                        lastKwEnd = matcher.end(GROUP_ATTRIBUTES_SECTION);

                        spansBuilder.add(Collections.singleton("xml-tag-mark"), matcher.end(GROUP_CLOSE_BRACKET) - lastKwEnd);
                    }
                }
                lastKwEnd = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
            return spansBuilder.create();
        }
    }


}
