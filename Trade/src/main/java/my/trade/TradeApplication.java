package my.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import my.trade.qrtz.QuartzApplication;

@SpringBootApplication
@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class TradeApplication {

	public static void main(String[] args) {
		Class[] sources = new Class[] { TradeApplication.class, QuartzApplication.class };
		SpringApplication.run(sources, args);
	}
}
