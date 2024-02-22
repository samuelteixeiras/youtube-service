package cs.youtube.client.xml;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Feed {
	public List<Link> link;
	public String title;
	public Date updated;
	public Entry entry;
}
