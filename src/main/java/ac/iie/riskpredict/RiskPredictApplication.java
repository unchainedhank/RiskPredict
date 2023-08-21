package ac.iie.riskpredict;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author a3
 */
@SpringBootApplication
@EnableScheduling
public class RiskPredictApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskPredictApplication.class, args);
    }

}
