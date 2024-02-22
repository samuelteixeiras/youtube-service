package cs.youtube.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import cs.youtube.client.xml.Feed;

public class MainT {

    public static void main(String args[]) throws JsonProcessingException {
        String xmlStr = """
                <feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns="http://www.w3.org/2005/Atom"><link rel="hub" href="https://pubsubhubbub.appspot.com"/><link rel="self" href="https://www.youtube.com/xml/feeds/videos.xml?channel_id=UCZvWcqKK7xRUgy5avHkFYSw"/><title>YouTube video feed</title><updated>2024-02-19T16:08:37.10953657+00:00</updated><entry>
                  <id>yt:video:s_KK4am1XcU</id>
                  <yt:videoId>s_KK4am1XcU</yt:videoId>
                  <yt:channelId>UCZvWcqKK7xRUgy5avHkFYSw</yt:channelId>
                  <title>Ano novo parte 2</title>
                  <link rel="alternate" href="https://www.youtube.com/watch?v=s_KK4am1XcU"/>
                  <author>
                   <name>Samuel Teixeira</name>
                   <uri>https://www.youtube.com/channel/UCZvWcqKK7xRUgy5avHkFYSw</uri>
                  </author>
                  <published>2015-01-15T14:18:07+00:00</published>
                  <updated>2024-02-19T16:08:37.10953657+00:00</updated>
                 </entry></feed>
                """;
        parse2(xmlStr);
    }



    private static void parse2(String xmlString) throws JsonProcessingException {
        XmlMapper xmlMapper = new XmlMapper();
        Feed value = xmlMapper.readValue(xmlString, Feed.class);
        System.out.print(value);
    }
}
