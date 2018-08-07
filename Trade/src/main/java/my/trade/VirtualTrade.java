package my.trade;

import java.sql.Timestamp;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VirtualTrade {
	private Timestamp createdTime;
	private Timestamp closedTime;
	private String symbol;
	private double purchasedPrice;
	private double soldPrice;
	private int qty;
	private boolean display;
	private Integer id;
	private double ltp;
	private int tType;
	DecimalFormat f = new DecimalFormat("##.00");

	public Timestamp getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Timestamp createdTime) {
		this.createdTime = createdTime;
	}

	public Timestamp getClosedTime() {
		return closedTime;
	}

	public void setClosedTime(Timestamp closedTime) {
		this.closedTime = closedTime;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getPurchasedPrice() {
		return purchasedPrice;
	}

	public void setPurchasedPrice(double purchasedPrice) {
		this.purchasedPrice = purchasedPrice;
	}

	public double getSoldPrice() {
		return soldPrice;
	}

	public void setSoldPrice(double soldPrice) {
		this.soldPrice = soldPrice;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public boolean isDisplay() {
		return display;
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public double getLtp() {
		return ltp;
	}

	public void setLtp(double ltp) {
		this.ltp = ltp;
	}

	public String getPl() {
		if (tType == 1) {
			// long position
			Double pl = this.qty * (this.ltp - this.purchasedPrice);
			return f.format(pl);

		} else if (tType == 2) {
			// short position
			Double pl = this.qty * (this.soldPrice - this.ltp);
			return f.format(pl);

		} else if (tType == 3) {
			// long squared off
			Double pl = this.qty * (this.soldPrice - this.purchasedPrice);
			return f.format(pl);

		} else if (tType == 4) {
			// short squared off
			Double pl = this.qty * (this.purchasedPrice - this.soldPrice);
			return f.format(pl);
		}

		return "";
	}

	public int gettType() {
		return tType;
	}

	public void settType(int tType) {
		this.tType = tType;
	}
	
	public String getpType() {
		if (tType == 1) {
			return "Long";

		} else if (tType == 2) {
			return "Short";

		} else if (tType == 3) {
			return "Long SquaredOff";

		} else if (tType == 4) {
			return "Short SquaredOff";
		}

		return "";
	}

}
