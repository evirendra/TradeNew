package my.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IchimokuIndicator {
	private String symbol;
	private int ltp_cloud;
	private int spanA_spanB;
	private int tenkan_kijun;
	private int chikou_cloud;
	private int ohlcCLose_cloud;

	private boolean checkForAlerts = false;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public int getLtp_cloud() {
		return ltp_cloud;
	}

	public void setLtp_cloud(int ltp_cloud) {
		if (this.ltp_cloud != ltp_cloud) {
			checkForAlerts = true;
			this.ltp_cloud = ltp_cloud;
		}
	}

	public int getSpanA_spanB() {
		return spanA_spanB;
	}

	public void setSpanA_spanB(int spanA_spanB) {
		if (this.spanA_spanB != spanA_spanB) {
			checkForAlerts = true;
			this.spanA_spanB = spanA_spanB;
		}
	}

	public int getTenkan_kijun() {
		return tenkan_kijun;
	}

	public void setTenkan_kijun(int tenkan_kijun) {
		if (this.tenkan_kijun != tenkan_kijun) {
			checkForAlerts = true;
			this.tenkan_kijun = tenkan_kijun;
		}
	}

	public int getChikou_cloud() {
		return chikou_cloud;
	}

	public void setChikou_cloud(int chikou_cloud) {
		if (this.chikou_cloud != chikou_cloud) {
			checkForAlerts = true;
			this.chikou_cloud = chikou_cloud;
		}
	}

	public int getOhlcCLose_cloud() {
		return ohlcCLose_cloud;
	}

	public void setOhlcCLose_cloud(int ohlcCLose_cloud) {
		if (this.ohlcCLose_cloud != ohlcCLose_cloud) {
			checkForAlerts = true;
			this.ohlcCLose_cloud = ohlcCLose_cloud;
		}
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

	public boolean isCheckForAlerts() {
		return checkForAlerts;
	}

	public void setCheckForAlerts(boolean checkForAlerts) {
		this.checkForAlerts = checkForAlerts;
	}

	public int checkAlerts() {
		int alert = 0;
		if (this.tenkan_kijun == 1 && this.ltp_cloud == 1 && this.spanA_spanB == 1 && this.chikou_cloud == 1
				&& this.ohlcCLose_cloud == 1) {
			alert = 1;
		}
		if (this.tenkan_kijun == -1 && this.ltp_cloud == -1 && this.spanA_spanB == -1 && this.chikou_cloud == -1
				&& this.ohlcCLose_cloud == -1) {
			alert = -1;
		}
		this.checkForAlerts = false;
		return alert;
	}

}
