package my.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import my.trade.qrtz.QuartzApplication;

@SpringBootApplication
public class TradeApplication {

	public static void main(String[] args) {
		Class[] sources = new Class[] { TradeApplication.class, QuartzApplication.class };
		SpringApplication.run(sources, args);
	}
}
