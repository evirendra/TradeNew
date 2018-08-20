package my.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExitCache {

	private static final Logger logger = LoggerFactory.getLogger(ExitCache.class);
	private static ExitCache cache = new ExitCache();

	@JsonIgnore
	public static ExitCache getCache() {
		return cache;
	}
	/**
	 * this is used for clean up to avoid next day issues
	 */
	public static void  resetCache() {
		ExitCache.cache = new ExitCache();
	}

	private double bankNiftyUpperLimit;
	private double bankNiftyLowerLimit;
	private double optionTotalUpperLimit;
	private double optionTotalLowerLimit;
	private double callOptionUpperLimit;
	private double putOptionUpperLimit;
	private boolean testMode;
	private double callOption;
	private Integer callOptionQty;
	private double putOption;
	private Integer putOptionQty;
	private boolean exitActionEnabled;
	private boolean sellIfTotalReachesFlag;
	private double sellIfTotalReachesAmount;
	private boolean exitSystemEnabled;

	public boolean isExitSystemEnabled() {
		return exitSystemEnabled;
	}

	public void setExitSystemEnabled(boolean exitSystemEnabled) {
		this.exitSystemEnabled = exitSystemEnabled;
	}

	public double getCallOption() {
		return callOption;
	}

	public void setCallOption(double callOption) {
		this.callOption = callOption;
	}

	public Integer getCallOptionQty() {
		return callOptionQty;
	}

	public void setCallOptionQty(Integer callOptionQty) {
		this.callOptionQty = callOptionQty;
	}

	public double getPutOption() {
		return putOption;
	}

	public void setPutOption(double putOption) {
		this.putOption = putOption;
	}

	public Integer getPutOptionQty() {
		return putOptionQty;
	}

	public void setPutOptionQty(Integer putOptionQty) {
		this.putOptionQty = putOptionQty;
	}

	public void setSellIfTotalReachesAmount(double sellIfTotalReachesAmount) {
		this.sellIfTotalReachesAmount = sellIfTotalReachesAmount;
	}

	public boolean isExitActionEnabled() {
		return exitActionEnabled;
	}

	public void setExitActionEnabled(boolean exitActionEnabled) {
		this.exitActionEnabled = exitActionEnabled;
	}

	public boolean isSellIfTotalReachesFlag() {
		return sellIfTotalReachesFlag;
	}

	public void setSellIfTotalReachesFlag(boolean sellIfTotalReachesFlag) {
		this.sellIfTotalReachesFlag = sellIfTotalReachesFlag;
	}

	public double getSellIfTotalReachesAmount() {
		return sellIfTotalReachesAmount;
	}

	public double getBankNiftyUpperLimit() {
		return bankNiftyUpperLimit;
	}

	public void setBankNiftyUpperLimit(double bankNiftyUpperLimit) {
		this.bankNiftyUpperLimit = bankNiftyUpperLimit;
	}

	public double getBankNiftyLowerLimit() {
		return bankNiftyLowerLimit;
	}

	public void setBankNiftyLowerLimit(double bankNiftyLowerLimit) {
		this.bankNiftyLowerLimit = bankNiftyLowerLimit;
	}

	public double getOptionTotalUpperLimit() {
		return optionTotalUpperLimit;
	}

	public void setOptionTotalUpperLimit(double optionTotalUpperLimit) {
		this.optionTotalUpperLimit = optionTotalUpperLimit;
	}

	public double getOptionTotalLowerLimit() {
		return optionTotalLowerLimit;
	}

	public void setOptionTotalLowerLimit(double optionTotalLowerLimit) {
		this.optionTotalLowerLimit = optionTotalLowerLimit;
	}

	public double getCallOptionUpperLimit() {
		return callOptionUpperLimit;
	}

	public void setCallOptionUpperLimit(double callOptionUpperLimit) {
		this.callOptionUpperLimit = callOptionUpperLimit;
	}

	public double getPutOptionUpperLimit() {
		return putOptionUpperLimit;
	}

	public void setPutOptionUpperLimit(double putOptionUpperLimit) {
		this.putOptionUpperLimit = putOptionUpperLimit;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		String writeValueAsString = "";
		try {
			writeValueAsString = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writeValueAsString;
	}

	public boolean isTestMode() {
		return testMode;
	}

	public void populateExitCache(String bankNiftyUpperLimit, String bankNiftyLowerLimit, String optionTotalUpperLimit,
			String optionTotalLowerLimit, String callOptionUpperLimit, String putOptionUpperLimit, boolean testMode,
			String callOptionSymbol, String callOptionQty, String putOptionSymbol, String putOptionQty,
			boolean exitActionEnabled, boolean sellIfTotalReachesFlag, String sellIfTotalReachesAmount) {

		this.setBankNiftyUpperLimit(Double.parseDouble(bankNiftyUpperLimit));
		this.setBankNiftyLowerLimit(Double.parseDouble(bankNiftyLowerLimit));
		this.setOptionTotalUpperLimit(Double.parseDouble(optionTotalUpperLimit));
		this.setOptionTotalLowerLimit(Double.parseDouble(optionTotalLowerLimit));
		this.setCallOptionUpperLimit(Double.parseDouble(callOptionUpperLimit));
		this.setPutOptionUpperLimit(Double.parseDouble(putOptionUpperLimit));
		this.setTestMode(testMode);
		this.setCallOption(Double.parseDouble(callOptionSymbol));
		this.setCallOptionQty(Integer.parseInt(callOptionQty));
		this.setPutOption(Double.parseDouble(putOptionSymbol));
		this.setPutOptionQty(Integer.parseInt(putOptionQty));
		this.setExitActionEnabled(exitActionEnabled);
		this.setSellIfTotalReachesFlag(sellIfTotalReachesFlag);
		if (sellIfTotalReachesFlag) {
			this.setSellIfTotalReachesAmount(Double.parseDouble(sellIfTotalReachesAmount));
		} else {
			this.setSellIfTotalReachesAmount(0);
		}
	}

}
