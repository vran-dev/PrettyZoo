package cc.cc1234.app.util;

import cc.cc1234.app.fp.Try;
import cc.cc1234.app.highlights.json.JsonHighlights;
import cc.cc1234.app.highlights.properties.PropertiesHighlights;
import cc.cc1234.app.highlights.xml.XmlHighlights;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;
import java.util.Collections;

public class Highlights {

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        if (text == null || text.isBlank()) {
            return StyleSpans.singleton(Collections.singleton(""), 0);
        }

        var result = Try.of(() -> JsonHighlights.compute(text))
                .onFailureMap(thr -> PropertiesHighlights.compute(text));

        if (result.isSuccess()) {
            return result.get();
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
}
