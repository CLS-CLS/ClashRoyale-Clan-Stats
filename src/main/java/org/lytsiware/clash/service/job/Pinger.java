package org.lytsiware.clash.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;

@Service
@Profile({"heroku", "local-herokuDB"})
public class Pinger {

    Logger logger = LoggerFactory.getLogger(Pinger.class);

    @Value("${ping.url}")
    private Resource urlToPing;

    //TODO add fixed rate as application property
    @Scheduled(initialDelayString = "${pinger.initialDelay}", fixedRate = 125 * 60 * 1000)
    @Retryable(maxAttempts = 10, backoff = @Backoff(1000 * 30))
    public void ping() {
        try {
            logger.info("Pinging Url {} ", urlToPing);
            HttpURLConnection connection = (HttpURLConnection) urlToPing.getURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
            connection.setConnectTimeout(50 * 1000);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Expected response code 200 but was " + responseCode);
            }
        } catch (Exception e) {
            logger.error("Exception while trying to ping ", e);
            throw new RuntimeException(e);
        }

    }

}
