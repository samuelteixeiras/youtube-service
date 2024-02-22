package cs.youtube.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import cs.youtube.client.xml.Feed;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
@Slf4j
public class XmlParseUtils {

    public static Optional<Feed> parseXml(String xmlString)  {
        XmlMapper xmlMapper = new XmlMapper();
        Feed value = null;
        try {
            value = xmlMapper.readValue(xmlString, Feed.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return Optional.empty();
        }
        return Optional.of(value);
    }
}
