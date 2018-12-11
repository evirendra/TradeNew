package my.trade;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("my")
public class ConfigProperties {
	private String bankNiftyExpiry;

	public String getBankNiftyExpiry() {
		return bankNiftyExpiry;
	}

	public void setBankNiftyExpiry(String bankNiftyExpiry) {
		this.bankNiftyExpiry = bankNiftyExpiry;
	}
	
	

}
