package cs.youtube.client.xml;

import lombok.Data;

import java.util.Date;
@Data
public class Entry {
    public String id;
    public String videoId;
    public String channelId;
    public String title;
    public Link link;
    public Author author;

    public Date published;
    public Date updated;
}
