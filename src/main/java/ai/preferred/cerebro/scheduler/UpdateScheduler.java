package ai.preferred.cerebro.scheduler;

import ai.preferred.cerebro.webservice.UpdateController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author hpminh@apcs.vn
 */
@Component
public class UpdateScheduler {
    private static final Logger log = LoggerFactory.getLogger(UpdateScheduler.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedDelay = 5000)
    public void reportCurrentTime() {
        log.info("Update begin. The time is now {}", dateFormat.format(new Date()));
        try {
            UpdateController.tellCornacToUpdate("http://localhost:5000/generate");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
