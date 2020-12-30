package cc.cc1234.app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class Formatters {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    static {
        transformerFactory.setAttribute("indent-number", 2);

    }

    public static String prettyJson(String raw) throws JsonProcessingException {
        return jsonMapper.readTree(raw).toPrettyString();
    }

    public static String prettyXml(String raw) throws Exception {
        return prettyFormat(raw);
    }

    public static String prettyFormat(String input) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
