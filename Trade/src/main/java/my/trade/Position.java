package my.trade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Position {

	private String tradingSymbol;
	private String exchange;
	private Integer quantity;
	private Double averagePrice;
	private Integer daySellQuantity;
	private Integer dayBuyQuantity;

	public String getTradingSymbol() {
		return tradingSymbol;
	}

	public void setTradingSymbol(String tradingSymbol) {
		this.tradingSymbol = tradingSymbol;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(Double averagePrice) {
		this.averagePrice = averagePrice;
	}

	public Integer getDaySellQuantity() {
		return daySellQuantity;
	}

	public void setDaySellQuantity(Integer daySellQuantity) {
		this.daySellQuantity = daySellQuantity;
	}
	
	public Integer getDayBuyQuantity() {
		return dayBuyQuantity;
	}

	public void setDayBuyQuantity(Integer dayBuyQuantity) {
		this.dayBuyQuantity = dayBuyQuantity;
	}

	public void populate(String tradingSymbol, String exchange, Integer quantity, Double averagePrice,
			Integer daySellQuantity, Integer dayBuyQuantity, Position position) {
		this.setTradingSymbol(tradingSymbol);
		this.setExchange(exchange);
		this.setQuantity(quantity);
		this.setAveragePrice(averagePrice);
		this.setDaySellQuantity(daySellQuantity);
		this.setDayBuyQuantity(dayBuyQuantity);
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

	public Double getRoundedAveragePrice() {

		double round = 0.05 * (Math.floor(averagePrice / 0.05));
		double roundedAveragePrice = (round * 100) / 100;
		return roundedAveragePrice;
	}

}