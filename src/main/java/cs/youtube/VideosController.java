package cs.youtube;

import com.fasterxml.jackson.core.JsonProcessingException;
import cs.youtube.client.xml.Feed;
import cs.youtube.client.Video;
import cs.youtube.utils.XmlParseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Controller
@ResponseBody
@RequestMapping("/video")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://192.168.4.218:8081", "http://localhost:8081"})
class VideosController {

    private final Executor executor = Executors.newSingleThreadExecutor();

    private final YoububeService service;

    private final ApplicationEventPublisher publisher;

    @RequestMapping(value = "/update", method = {RequestMethod.GET, RequestMethod.POST})
    ResponseEntity<?> reset(RequestEntity<String> payload,
                            @RequestParam(value = "hub.challenge", required = false) String hubChallenge) {
        log.info("========================================");
        if (StringUtils.hasText(hubChallenge)) {
            log.info("Hub challenge " + hubChallenge);
            return ResponseEntity.ok(hubChallenge);
        }

        payload.getHeaders().forEach((k, v) -> log.info('\t' + k + '=' + String.join(",", v)));
        log.debug("payload: " + payload.getBody());
        createVideoUpdateEvent(payload);

        return ResponseEntity.status(204).build();
    }

    private void createVideoUpdateEvent(RequestEntity<String> payload) {
        Optional<Feed> feedMaybe = XmlParseUtils.parseXml(payload.getBody());
        if (feedMaybe.isPresent()) {
            Feed feed = feedMaybe.get();
            log.info("Updated for Video: " + feed.entry.videoId + "and channel :" + feed.entry.channelId);
            this.executor.execute(() -> publisher.publishEvent(
                    new YoutubeChannelUpdatedEvent(Instant.now(), feed.entry.videoId, feed.entry.channelId)));
        }
    }

    @GetMapping("/videos")
    Collection<Video> videos() {
        return this.service.videos();
    }
}
