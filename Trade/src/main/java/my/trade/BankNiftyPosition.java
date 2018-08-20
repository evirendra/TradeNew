package my.trade;

import org.springframework.util.StringUtils;

import my.trade.qrtz.DataCache;

public class BankNiftyPosition {
	private String callOption;
	private Double callOptionLtp;
	private String putOption;
	private Double putOptionLtp;
	private Double totalOptionLtp;

	public String getCallOption() {
		return callOption;
	}

	public void setCallOption(String callOption) {
		this.callOption = callOption;
	}

	public String getPutOption() {
		return putOption;
	}

	public void setPutOption(String putOption) {
		this.putOption = putOption;
	}

	public Double getCallOptionLtp() {
		return callOptionLtp;
	}

	public void setCallOptionLtp(Double callOptionLtp) {
		this.callOptionLtp = callOptionLtp;
	}

	public Double getPutOptionLtp() {
		return putOptionLtp;
	}

	public void setPutOptionLtp(Double putOptionLtp) {
		this.putOptionLtp = putOptionLtp;
	}

	public Double getTotalOptionLtp() {
		return totalOptionLtp;
	}

	public void setTotalOptionLtp(Double totalOptionLtp) {
		this.totalOptionLtp = totalOptionLtp;
	}

	public void populateBankNiftyPosition() {
		if (ExitCache.getCache().isExitSystemEnabled()) {
			String callOption = Integer.toString((int) ExitCache.getCache().getCallOption());
			String putOption = Integer.toString((int) ExitCache.getCache().getPutOption());
			if (!StringUtils.isEmpty(callOption) && !StringUtils.isEmpty(putOption)) {

				double callOptionLtp = DataCache.get(BankNiftyOptionData.getInstrumentName(callOption, true))
						.doubleValue();
				double putOptionLtp = DataCache.get(BankNiftyOptionData.getInstrumentName(putOption, false))
						.doubleValue();
				this.setPutOption(putOption);
				this.setCallOption(callOption);
				this.setCallOptionLtp(callOptionLtp);
				this.setPutOptionLtp(putOptionLtp);
				this.setTotalOptionLtp(Double.sum(callOptionLtp, putOptionLtp));
			}
		}
	}
}
