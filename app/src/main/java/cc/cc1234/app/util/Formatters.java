package cc.cc1234.app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class Formatters {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static String prettyJson(String raw) throws JsonProcessingException {
        return jsonMapper.readTree(raw).toPrettyString();
    }

    public static String prettyXml(String raw) {
        return xmlFormat(raw);
    }

    public static String xmlFormat(String xml) {
        try {
            final InputSource src = new InputSource(new StringReader(xml));
            final Node document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(src)
                    .getDocumentElement();
            final Boolean keepDeclaration = xml.contains("<?xml");

            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            final LSSerializer writer = impl.createLSSerializer();
            // Set this to true if the output needs to be beautified.
            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            // Set this to true if the declaration is needed to be outputted.
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);
            return writer.writeToString(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String longToHexString(long val) {
        return "0x" + Long.toHexString(val);
    }
    
}
