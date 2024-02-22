package cs.youtube;

import com.joshlong.google.pubsubhubbub.PubsubHubbubClient;
import cs.youtube.client.ChannelSubscribe;
import cs.youtube.client.Video;
import cs.youtube.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Controller
@ResponseBody
@RequestMapping("/channel")
@RequiredArgsConstructor
// @CrossOrigin(origins = { "http://192.168.4.218:8081", "http://localhost:8081" })
class ChannelsController {

    private final ApplicationEventPublisher publisher;

    private final PubsubHubbubClient pubsubHubbubClient;

    private final YoububeService service;

    @Value("${youtube.callBackUrl}")
    private String callbackUrl;

    private final int leaseInSeconds = 60 * 60 * 1;

    @PostMapping(value = "/register" , consumes = {"application/json"})
    public ResponseEntity<String> addArticle(@RequestBody ChannelSubscribe channelSubscribe) {
        if (!channelSubscribe.channelId().isEmpty()) {
            String channelId = channelSubscribe.channelId();
            this.service.insertIntoChannel(channelId);
            subscribe(this.pubsubHubbubClient, this.leaseInSeconds, channelId, this.callbackUrl);
            log.info("channel registered" + channelId);
            return new ResponseEntity<>("channel created", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("channel not created", HttpStatus.BAD_REQUEST);
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
