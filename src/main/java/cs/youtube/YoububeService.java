package cs.youtube;

import cs.youtube.client.Channel;
import cs.youtube.client.Video;

import java.util.List;

interface YoububeService {

	void refresh();

	List<Video> videos();

	void insertVideoUpdate(String videoId, String channelId);

	List<Channel> getAllChannels();

	void insertIntoChannel(String channelId);

	List<Video> getLastVideosByChannels(String channelId, int limit)
}
