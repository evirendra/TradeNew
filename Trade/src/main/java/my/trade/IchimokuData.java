package my.trade;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import my.trade.qrtz.DataCache;

public class IchimokuData {
	private Timestamp time;
	private String symbol;
	private double tenkan;
	private double kijun;
	private double spanA;
	private double spanB;
	private double chikou;
	private IchimokuData cloudData;
	private IchimokuData chikouData;
	private OHLCData ohlcData;

	private static final Logger logger = LoggerFactory.getLogger(IchimokuData.class);

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public double getTenkan() {
		return tenkan;
	}

	public void setTenkan(double tenkan) {
		this.tenkan = tenkan;
	}

	public double getKijun() {
		return kijun;
	}

	public void setKijun(double kijun) {
		this.kijun = kijun;
	}

	public double getSpanA() {
		return spanA;
	}

	public void setSpanA(double spanA) {
		this.spanA = spanA;
	}

	public double getSpanB() {
		return spanB;
	}

	public void setSpanB(double spanB) {
		this.spanB = spanB;
	}

	public double getChikou() {
		return chikou;
	}

	public void setChikou(double chikou) {
		this.chikou = chikou;
	}

	public IchimokuData getChikouData() {
		return chikouData;
	}

	public void setChikouData(IchimokuData chikouData) {
		this.chikouData = chikouData;
	}

	public void checkForCrossOver(IchimokuIndicator indicator) {
		Number number = DataCache.get(symbol);
		double ltp = number.doubleValue();

		setTenkanKijun(indicator);
		setSpanASpanB(indicator);

		double spanAAtLtp = cloudData.getSpanA();
		double spanBAtLtp = cloudData.getSpanB();
		int spnaASpanBAtLtpComp = setltpCloud(indicator, ltp, spanAAtLtp, spanBAtLtp);

		setOhlcCLoseCloud(indicator, spanAAtLtp, spanBAtLtp, spnaASpanBAtLtpComp);

		double spanAForChikou = chikouData.getSpanA();
		double spanBForChikou = chikouData.getSpanB();

		int spnaASpanBAtChikou = Double.compare(spanAForChikou, spanBForChikou);

		setLtpChikouCLoud(indicator, ltp, spanAForChikou, spanBForChikou, spnaASpanBAtChikou);

	}

	private void setOhlcCLoseCloud(IchimokuIndicator indicator, double spanAAtLtp, double spanBAtLtp,
			int spnaASpanBAtLtpComp) {
		double ohlcClose = ohlcData.getClose();
		switch (spnaASpanBAtLtpComp) {
		case 1:
			if (Double.compare(ohlcClose, spanAAtLtp) == 1) {
				indicator.setOhlcCLose_cloud(1);
			}
			if (Double.compare(ohlcClose, spanBAtLtp) == -1) {
				indicator.setOhlcCLose_cloud(-1);
			}
			break;
		case -1:
			if (Double.compare(ohlcClose, spanAAtLtp) == -1) {
				indicator.setOhlcCLose_cloud(-1);
			}
			if (Double.compare(ohlcClose, spanBAtLtp) == 1) {
				indicator.setOhlcCLose_cloud(1);
			}
			break;
		default:
			if (Double.compare(ohlcClose, spanAAtLtp) == 1) {
				indicator.setOhlcCLose_cloud(1);

			}
			if (Double.compare(ohlcClose, spanBAtLtp) == -1) {
				indicator.setOhlcCLose_cloud(-1);
			}
			break;
		}
	}

	private void setLtpChikouCLoud(IchimokuIndicator indicator, double ltp, double spanAForChikou,
			double spanBForChikou, int spnaASpanBAtChikou) {
		switch (spnaASpanBAtChikou) {
		case 1:
			if (Double.compare(ltp, spanAForChikou) == 1) {
				indicator.setChikou_cloud(1);
			}
			if (Double.compare(ltp, spanBForChikou) == -1) {
				indicator.setChikou_cloud(-1);
			}

			break;
		case -1:
			if (Double.compare(ltp, spanAForChikou) == -1) {
				indicator.setChikou_cloud(-1);
			}
			if (Double.compare(ltp, spanBForChikou) == 1) {
				indicator.setChikou_cloud(1);
			}
			break;
		default:
			if (Double.compare(ltp, spanAForChikou) == 1) {
				indicator.setChikou_cloud(1);
			}
			if (Double.compare(ltp, spanAForChikou) == -1) {
				indicator.setChikou_cloud(-1);
			}
			break;
		}
	}

	private void setTenkanKijun(IchimokuIndicator indicator) {
		int tenkanKijunComp = Double.compare(tenkan, kijun);
		indicator.setTenkan_kijun(tenkanKijunComp);
	}

	private void setSpanASpanB(IchimokuIndicator indicator) {
		int spanASpanBComp = Double.compare(spanA, spanB);
		indicator.setSpanA_spanB(spanASpanBComp);
	}

	private int setltpCloud(IchimokuIndicator indicator, double ltp, double spanAAtLtp, double spanBAtLtp) {
		int spnaASpanBAtLtpComp = Double.compare(spanAAtLtp, spanBAtLtp);

		switch (spnaASpanBAtLtpComp) {
		case 1:
			if (Double.compare(ltp, spanAAtLtp) == 1) {
				indicator.setLtp_cloud(1);
			}
			if (Double.compare(ltp, spanBAtLtp) == -1) {
				indicator.setLtp_cloud(-1);
			}
			break;
		case -1:
			if (Double.compare(ltp, spanAAtLtp) == -1) {
				indicator.setLtp_cloud(-1);
			}
			if (Double.compare(ltp, spanBAtLtp) == 1) {
				indicator.setLtp_cloud(1);
			}
			break;
		default:
			if (Double.compare(ltp, spanAAtLtp) == 1) {
				indicator.setLtp_cloud(1);
			}
			if (Double.compare(ltp, spanBAtLtp) == -1) {
				indicator.setLtp_cloud(-1);
			}
			break;
		}
		return spnaASpanBAtLtpComp;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
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

	public IchimokuData getCloudData() {
		return cloudData;
	}

	public void setCloudData(IchimokuData cloudData) {
		this.cloudData = cloudData;
	}

	public OHLCData getOhlcData() {
		return ohlcData;
	}

	public void setOhlcData(OHLCData ohlcData) {
		this.ohlcData = ohlcData;
	}

}
