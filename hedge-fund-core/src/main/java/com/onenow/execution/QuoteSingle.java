package com.onenow.execution;

import static com.ib.controller.Formats.fmtPct;

import javax.swing.table.AbstractTableModel;

import com.ib.client.TickType;
import com.ib.client.Types.MktDataType;
import com.onenow.constant.InvDataSource;
import com.onenow.constant.InvDataTiming;
import com.onenow.constant.InvDataType;
import com.onenow.constant.TradeType;
import com.onenow.data.MarketPrice;
import com.onenow.instrument.Investment;
import com.onenow.portfolio.BrokerController.TopMktDataAdapter;

/**
 * Single quote class
 *
 */
public class QuoteSingle extends TopMktDataAdapter {
	AbstractTableModel m_model;
	String m_description;
	double m_bid;
	double m_ask;
	double m_last;
	long m_lastTime;
	int m_bidSize;
	int m_askSize;
	double m_close;
	int m_volume;
	boolean m_frozen;
	
	Investment investment;
	MarketPrice marketPrice;
	
	public QuoteSingle () {
		
	}
	
	QuoteSingle( AbstractTableModel model, String description, Investment inv, MarketPrice marketPrice) {
		m_model = model;
		m_description = description;
		
		setInvestment(inv);
		setMarketPrice(marketPrice);
	}
	
	public String change() {
		return m_close == 0	? null : fmtPct( (m_last - m_close) / m_close);
	}

	// INTERFACE
	@Override public void tickPrice( TickType tickType, double price, int canAutoExecute) {
		switch( tickType) {
			case BID:
				m_bid = price;
//				System.out.println("Bid " + m_bid);
				getMarketPrice().writePriceNotRealTime(getInvestment(), m_bid, TradeType.SELL.toString());
				break;
			case ASK:
				m_ask = price;
//				System.out.println("Ask " + m_ask);
				getMarketPrice().writePriceNotRealTime(getInvestment(), m_ask, TradeType.BUY.toString());
				break;
			case LAST:
				m_last = price;
//				System.out.println("Last " + m_last);
				getMarketPrice().writePriceNotRealTime(getInvestment(), m_last, TradeType.TRADED.toString());
				break;
			case CLOSE:
				m_close = price;
//				System.out.println("Close " + m_close);
				getMarketPrice().writePriceNotRealTime(getInvestment(), m_close, TradeType.CLOSE.toString());
				break;
			default: break;	
		}
		m_model.fireTableDataChanged(); // should use a timer to be more efficient
	}

	@Override public void tickSize( TickType tickType, int size) {
		switch( tickType) {
			case BID_SIZE:
				m_bidSize = size;
				getMarketPrice().writeSizeNotRealTime(getInvestment(), m_bidSize, InvDataType.BIDSIZE.toString());
//				System.out.println("Bid size " + m_bidSize);
				break;
			case ASK_SIZE:
				m_askSize = size;
				getMarketPrice().writeSizeNotRealTime(getInvestment(), m_askSize, InvDataType.ASKSIZE.toString());
//				System.out.println("Ask size " + m_askSize);
				break;
			case VOLUME:
				m_volume = size;
				getMarketPrice().writeSizeNotRealTime(getInvestment(), m_volume, InvDataType.VOLUME.toString());
//				System.out.println("Volume size " + m_volume);
				break;
            default: break; 
		}
		m_model.fireTableDataChanged();			
	}
	
	/**
	 * Handler of all callback tick types
	 */
	// reqScannerSubscription 
	// for 500 companies: $120 / mo
	@Override public void tickString(TickType tickType, String value) {
		switch( tickType) {
			case LAST_TIMESTAMP:
				m_lastTime = Long.parseLong( value) * 1000;
//				getMarketPrice().setLastTime(getInvestment(), m_lastTime);
//				System.out.println("Last time " + m_lastTime);
				break;
			case AVG_VOLUME:
				System.out.println("AVG_VOLUME " + value); // not for indices
				break;
			case OPTION_CALL_VOLUME:
				System.out.println("OPTION_CALL_VOLUME " + value); // stocks 
				break;
			case OPTION_PUT_VOLUME:
				System.out.println("OPTION_PUT_VOLUME " + value); // stocks
				break;
			case AUCTION_VOLUME:
				System.out.println("AUCTION_VOLUME " + value); // subscribe to
				break;
			case RT_VOLUME:
				// the time-stamp is in UTC time zone
				System.out.println("\n" + "RT_VOLUME " + value); 
				parseAndWriteRealTime(getInvestment(), value);
				// RT_VOLUME 0.60;1;1424288913903;551;0.78662433;true
				// InvestmentStock inv=new InvestmentStock(new Underlying("SPX"));
				break;
			case VOLUME_RATE:
				System.out.println("VOLUME_RATE " + value); // not for indices
				break;
			
            default: break; 
		}
	}
	
	@Override public void marketDataType(MktDataType marketDataType) {
		m_frozen = marketDataType == MktDataType.Frozen;
		m_model.fireTableDataChanged();
		
		if(m_frozen==true) {
			System.out.println("...frozen data");
		}
	}

	// PRIVATE
	public void parseAndWriteRealTime(Investment inv, String rtvolume) {
		String lastTradedPrice="";
		String lastTradeSize="";
		String lastTradeTime="";
		String totalVolume="";
		String VWAP="";
		String splitFlag="";
		
		int i=1;
		for(String split:rtvolume.split(";")) {
			if(i==1) { //	Last trade price
				lastTradedPrice = split;
				if(lastTradedPrice.equals("")) {
					return;
				}
			}
			if(i==2) { //	Last trade size
				lastTradeSize = split;
				if(lastTradeSize.equals("")) {
					return;
				}
			}
			if(i==3) { //	Last trade time
				lastTradeTime = split;
				if(lastTradeTime.equals("")) {
					return;
				}
			}
			if(i==4) { //	Total volume
				totalVolume = split;
				if(totalVolume.equals("")) {
					return;
				}
			}
			if(i==5) { //	VWAP
				VWAP = split;
				if(VWAP.equals("")) {
					return;
				}
			}
			if(i==6) { //	Single trade flag - True indicates the trade was filled by a single market maker; False indicates multiple market-makers helped fill the trade
				splitFlag = split;
				if(splitFlag.equals("")) {
					return;
				}
			}
			i++;
		}
		Long time = Long.parseLong(lastTradeTime); 	// TODO: *1000 ?
		
		InvDataSource source = InvDataSource.IB;
		InvDataTiming timing = InvDataTiming.REALTIME;
		getMarketPrice().writeRealTime(time, inv, Double.parseDouble(lastTradedPrice), Integer.parseInt(lastTradeSize),  
					Integer.parseInt(totalVolume), Double.parseDouble(VWAP), Boolean.parseBoolean(splitFlag),
					source, timing);
		return;
	}

	// TEST
	
	// PRINT
	public String toString() {
		String s="\n\n";
		s = s + "QUOTE" + "\n";
		s = s + "Description " + m_description + "\n";
		s = s + "Bid " + m_bid + "\n";
		s = s + "Ask " + m_ask + "\n";
		s = s + "Last " + m_last + "\n";
		s = s + "Last time " + m_lastTime + "\n";
		s = s + "Bid size " + m_bidSize + "\n";
		s = s + "Ask size " + m_askSize + "\n";
		s = s + "Close " + m_close + "\n";
		s = s + "Frozen " + m_frozen + "\n";
		return s;
	}

	
	// SET GET
	public MarketPrice getMarketPrice() {
		return marketPrice;
	}

	public void setMarketPrice(MarketPrice marketPrice) {
		this.marketPrice = marketPrice;
	}

	public Investment getInvestment() {
		return investment;
	}

	public void setInvestment(Investment investment) {
		this.investment = investment;
	}

}