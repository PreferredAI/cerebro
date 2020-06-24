package ai.preferred.cerebro.webservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author hpminh@apcs.vn
 */
@SpringBootApplication
@EnableScheduling
public class CerebroBackendApplication {

    public static void main(String[] args){
        SpringApplication.run(CerebroBackendApplication.class, args);
    }

}
