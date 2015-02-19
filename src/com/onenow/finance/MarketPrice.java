package com.onenow.finance;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.onenow.investor.DataType;
import com.onenow.investor.QuoteDepth.DeepRow;

public class MarketPrice {

	HashMap<String, Double> 				prices; // $
	HashMap<String, Long> 					times; 	// when
	HashMap<String, Integer> 				size; 	// volume
	HashMap<String, ArrayList<DeepRow>>		depth;	// market depth
	HashMap<String, Boolean>				flag;	// flag
	

	public MarketPrice() {
		setPrices(new HashMap<String, Double>());
		setTimes(new HashMap<String, Long>());
		setSize(new HashMap<String, Integer>());
		setDepth(new HashMap<String, ArrayList<DeepRow>>());
	}

	public Long setRealTime(Investment inv, String rtvolume) {
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
			}
			if(i==3) { //	Last trade size
				lastTradeSize = split;
			}
			if(i==4) { //	Last trade time
				lastTradeTime = split;
			}
			if(i==5) { //	Total volume
				totalVolume = split;
			}
			if(i==6) { //	VWAP
				VWAP = split;
			}
			if(i==7) { //	Single trade flag - True indicates the trade was filled by a single market maker; False indicates multiple market-makers helped fill the trade
				splitFlag = split;
			}
			System.out.println(split);
			i++;
		}
		Long time = Long.valueOf(lastTradeTime);
		fillRealTime(time, inv, Double.valueOf(lastTradedPrice), Integer.valueOf(lastTradeSize),  
					Integer.valueOf(totalVolume), Double.valueOf(VWAP), Boolean.valueOf(splitFlag));
		return time;
	}
 	
	private void fillRealTime(	Long lastTradeTime, Investment inv, Double lastPrice, Integer lastSize, 
								Integer volume, Double VWAP, boolean splitFlag) {
		String type="";
		type = TradeType.LAST.toString();
		String key = getTimedLookupKey(lastTradeTime, inv, type);
		getPrices().put(getTimedLookupKey(lastTradeTime, inv, type), lastPrice);
		getSize().put(getTimedLookupKey(lastTradeTime, inv, type), lastSize);
		type = DataType.VOLUME.toString();
		getSize().put(getTimedLookupKey(lastTradeTime, inv, type), volume);
		type = DataType.VWAP.toString();
		getPrices().put(getTimedLookupKey(lastTradeTime, inv, type), VWAP);
		type = DataType.TRADEFLAG.toString();
		getFlag().put(getLookupKey(inv, type), splitFlag);
	}
	
	
	public String getRealTime(Long tradeTime, Investment inv) {
		String s = inv.toString() + "\n";
		s = s +	"Price " + getTimedPrice(tradeTime, inv, TradeType.LAST.toString()) + "\n" +
				"Size " + getTimedSize(tradeTime, inv, TradeType.LAST.toString()) + "\n" + 
				"Volume " + getTimedSize(tradeTime, inv, DataType.VOLUME.toString()) + "\n" +
				"VWAP " + getTimedPrice(tradeTime, inv, DataType.VWAP.toString()) + "\n" +
				"Trade Flag " + getTimedFlag(tradeTime, inv, DataType.TRADEFLAG.toString());
		return s;
	}
	
	public void setFlag(Investment inv, boolean flag) {
		getFlag().put(getLookupKey(inv, DataType.TRADEFLAG.toString()), flag);	
	}
	
	public boolean getTimedFlag(Long time, Investment inv, String dataType) {
		String key = getTimedLookupKey(time, inv, dataType);
		boolean flag=false;
		try {
			flag = (boolean) (getFlag().get(key)); 
		} catch (Exception e) {
			e.printStackTrace();
		} 		
		return flag;			
	}
	
	public boolean getFlag(Investment inv) {
		String key = getLookupKey(inv, DataType.TRADEFLAG.toString());
		boolean flag = false;
		try {
			flag = (boolean) (getFlag().get(key)); // let price be null to know it's not set
		} catch (Exception e) {
			e.printStackTrace();
		} 	
		return flag;		
	}
	
	public void setDepth(Investment inv, ArrayList<DeepRow> depth) {
		getDepth().put(getLookupKey(inv, DataType.MARKETDEPTH.toString()), depth);		
	}
	
	public ArrayList<DeepRow> getDepth(Investment inv) {
		String key = getLookupKey(inv, DataType.MARKETDEPTH.toString());
		ArrayList<DeepRow> depth = new ArrayList<DeepRow>();
		try {
			depth = (ArrayList<DeepRow>) (getDepth().get(key)); // let price be null to know it's not set
		} catch (Exception e) {
			e.printStackTrace();
		} 	
		if(depth==null) {
			depth=new ArrayList<DeepRow>(); // return empty
		}
		return depth;
	}
	
	public void setLastTime(Investment inv, Long time) {
		getTimes().put(getLookupKey(inv, DataType.LASTTIME.toString()), time);
	}
	public Long getLastTime(Investment inv, String dataType) {
		String key = getLookupKey(inv, dataType);
		Long lastTime=(long) 0;
		try {
			lastTime = (Long) (getTimes().get(key)); // let price be null to know it's not set
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return lastTime;
	}
	
	public void setSize(Investment inv, Integer size, String dataType) {
		getSize().put(getLookupKey(inv, dataType), size);
	}
	public Integer getTimedSize(Long time, Investment inv, String dataType) {
		String key = getTimedLookupKey(time, inv, dataType);
		Integer size=0;
		try {
			size = (Integer) (getSize().get(key)); 
		} catch (Exception e) {
			e.printStackTrace();
		} 		
		return size;		
	}
	
	public Integer getSize(Investment inv, String dataType) {
		String key = getLookupKey(inv, dataType);
		Integer size=0;
		try {
			size = (Integer) (getSize().get(key)); 
		} catch (Exception e) {
			e.printStackTrace();
		} 		
		return size;
	}
	
	public void setPrice(Investment inv, Double price, String dataType) {
		getPrices().put(getLookupKey(inv, dataType), price);
	}
	
	public Double getTimedPrice(Long time, Investment inv, String dataType) {
		String key = getTimedLookupKey(time, inv, dataType);
		Double price=0.0;
		try {
			price = (Double) (getPrices().get(key)); // let price be null to know it's not set
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return price;		
	}
	public Double getPrice(Investment inv, String dataType) {
		String key = getLookupKey(inv, dataType);
		Double price=0.0;
		try {
			price = (Double) (getPrices().get(key)); // let price be null to know it's not set
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return price;
	}

	private String getTimedLookupKey(Long time, Investment inv, String dataType) {
		String s="";
		s = time.toString() + "-";
		s = s + getLookupKey(inv, dataType);
		return s;
	}
	private String getLookupKey(Investment inv, String dataType) {
		Underlying under = inv.getUnder();
		String lookup = under.getTicker() + "-" + 
		                inv.getInvType() + "-" +
		                dataType;		
		if (inv instanceof InvestmentOption) {
			Double strike = ((InvestmentOption) inv).getStrikePrice();
			String exp = (String) ((InvestmentOption) inv).getExpirationDate();
			lookup = lookup + "-" + strike + "-" + exp; 
		}
		return (lookup);
	}

	// PRINT
	public String toString() {
		String s="";
		s = prices.toString();
		return s;
	}
	
	// TEST
	
	// SET GET
	private HashMap<String, Double> getPrices() {
		return prices;
	}

	private void setPrices(HashMap<String, Double> prices) {
		this.prices = prices;
	}

	private HashMap<String, Long> getTimes() {
		return times;
	}

	private void setTimes(HashMap<String, Long> times) {
		this.times = times;
	}

	private HashMap<String, Integer> getSize() {
		return size;
	}

	private void setSize(HashMap<String, Integer> size) {
		this.size = size;
	}

	private HashMap<String, ArrayList<DeepRow>> getDepth() {
		return depth;
	}

	private void setDepth(HashMap<String, ArrayList<DeepRow>> depth) {
		this.depth = depth;
	}

	private HashMap<String, Boolean> getFlag() {
		return flag;
	}

	private void setFlag(HashMap<String, Boolean> flag) {
		this.flag = flag;
	}
	


}
