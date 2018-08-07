package my.trade;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class IchimokuDataMapper implements RowMapper<IchimokuData> {

	@Override
	public IchimokuData mapRow(ResultSet rs, int rowNum) throws SQLException {
		IchimokuData ichimokuData = new IchimokuData();
		ichimokuData.setTenkan(rs.getDouble("tenkan"));
		ichimokuData.setKijun(rs.getDouble("kijun"));
		ichimokuData.setSpanA(rs.getDouble("spana"));
		ichimokuData.setSpanB(rs.getDouble("spanb"));
		ichimokuData.setChikou(rs.getDouble("chikou"));
		ichimokuData.setSymbol(rs.getString("symbol"));
		return ichimokuData;
	}

}
