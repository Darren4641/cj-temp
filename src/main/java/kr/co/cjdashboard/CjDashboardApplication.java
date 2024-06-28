package kr.co.cjdashboard;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import kr.co.cjdashboard.properties.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({
        CorsProperties.class
})
@SpringBootApplication
public class CjDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(CjDashboardApplication.class, args);
    }

}
