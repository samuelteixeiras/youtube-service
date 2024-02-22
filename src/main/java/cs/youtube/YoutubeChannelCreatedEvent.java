package cs.youtube;

import java.time.Instant;

record YoutubeChannelCreatedEvent(Instant when, String channelId) {
}
