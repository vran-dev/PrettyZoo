package cc.cc1234.app.util;

import cc.cc1234.app.highlights.json.JsonHighlights;
import cc.cc1234.app.highlights.xml.XmlHighlights;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fxmisc.richtext.model.StyleSpans;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Collection;
import java.util.Collections;

public class Highlights {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        if (text == null || text.isBlank()) {
            return StyleSpans.singleton(Collections.singleton(""), 0);
        }
        if (isJson(text)) {
            return JsonHighlights.compute(text);
        }
        if (isXml(text)) {
            return XmlHighlights.compute(text);
        }
        return StyleSpans.singleton(Collections.singleton(""), text.length());
    }

    private static boolean isXml(String xml) {
        try {
            Formatters.xmlFormat(xml);
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

}
