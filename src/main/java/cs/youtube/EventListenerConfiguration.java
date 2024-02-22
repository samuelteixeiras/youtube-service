package cs.youtube;

import com.joshlong.google.pubsubhubbub.PubsubHubbubClient;
import com.joshlong.twitter.Twitter;
import cs.youtube.client.Channel;
import cs.youtube.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
class EventListenerConfiguration {

	private final int leaseInSeconds = 60 * 60 * 1;

	@Value("${youtube.callBackUrl}")
	private String callbackUrl;

	@Value("${youtube.channel-id}")
	private String channelId;

	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	private final YoububeService service;

	private final ApplicationEventPublisher publisher;

	private final PubsubHubbubClient pubsubHubbubClient;

	private final Twitter twitter;

	private final DatabaseClient databaseClient;

	@EventListener
	void videoCreated(YoutubeVideoCreatedEvent videoCreatedEvent) {
		log.info("need to promote: {}", videoCreatedEvent.video().videoId() + ':' + videoCreatedEvent.video().title());
		// var scheduled = new Date();
		// twitter.scheduleTweet(scheduled, "starbuxman",
		// videoCreatedEvent.video().title() + " " + "https://www.youtube.com/watch?v="
		// + videoCreatedEvent.video().videoId(),
		// null) //
		// .flatMap(promoted -> databaseClient//
		// .sql(" update promoted_youtube_videos set promoted_at = :when where video_id =
		// :videoId ")//
		// .bind("when", scheduled) //
		// .bind("videoId", videoCreatedEvent.video().videoId())//
		// .fetch() //
		// .rowsUpdated()//
		// ) //
		// .filter(count -> count > 0) //
		// .subscribe(rows -> log.info("successfully promoted {} with title {}",
		// videoCreatedEvent.video().videoId(), videoCreatedEvent.video().title()));
	}

	@EventListener(YoutubeChannelUpdatedEvent.class)
	void youtubeChannelUpdated() {
		//this.service.refresh();
	}

	@EventListener(ApplicationReadyEvent.class)
	void ready() {
		this.scheduledExecutorService.scheduleAtFixedRate(this::renew, 0, this.leaseInSeconds, TimeUnit.SECONDS);
	}

	@EventListener(YoutubeChannelUpdatedEvent.class)
	void videoUpdated(YoutubeChannelUpdatedEvent event) {
		log.info("Video Updated: {}", event.videoId() + ':' + event.channelId());
		this.service.insertVideoUpdate( event.videoId(), event.channelId());
	}

	@EventListener(YoutubeChannelCreatedEvent.class)
	void channelSubscribed(YoutubeChannelCreatedEvent event) {
		log.info("Channel Subscribed: {}", event.channelId());
		// insert into table if not present, update sub time

	}

	private void renew() {
		// we need to have a list of all channels to resub
		List<Channel> allChannels = this.service.getAllChannels();
		log.info("Subscribing to " + allChannels.size() + " channels");
		allChannels.stream().forEach(channel -> {
			subscribe(this.pubsubHubbubClient, this.leaseInSeconds, channel.channelId(), this.callbackUrl);
			publisher.publishEvent(new YoutubeChannelCreatedEvent(Instant.now(), channelId));
		});
	}

	private static void subscribe(PubsubHubbubClient pubsubHubbubClient, int leaseInSeconds, String channelId,
			String callbackUrlStr) {
		var topicUrl = UrlUtils.url("https://www.youtube.com/xml/feeds/videos.xml?channel_id=" + channelId);
		var callbackUrl = UrlUtils.url(callbackUrlStr);
		var unsubscribe = pubsubHubbubClient //
				.unsubscribe(topicUrl, callbackUrl, PubsubHubbubClient.Verify.SYNC, leaseInSeconds, null)
				.onErrorComplete();// don't caer if this fails
		var subscribe = pubsubHubbubClient.subscribe(topicUrl, callbackUrl, PubsubHubbubClient.Verify.SYNC,
				leaseInSeconds, null);
		unsubscribe.thenMany(subscribe).subscribe(re -> log.info("subscribed to " + topicUrl.toExternalForm()));
	}

}
