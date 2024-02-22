package cs.youtube;

import java.time.Instant;

record YoutubeChannelUpdatedEvent(Instant when, String videoId, String channelId) {
}
