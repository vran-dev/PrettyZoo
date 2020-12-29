package cc.cc1234.app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class Formatters {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static final XmlMapper xmlMapper = new XmlMapper();

    public static String prettyJson(String raw) throws JsonProcessingException {
        return jsonMapper.readTree(raw).toPrettyString();
    }

    public static String prettyXml(String raw) throws JsonProcessingException {
        return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlMapper.readValue(raw, Object.class));
    }

}
