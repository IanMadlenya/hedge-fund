package com.onenow.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;

import com.onenow.constant.DBname;
import com.onenow.constant.InvDataSource;
import com.onenow.constant.InvDataTiming;
import com.onenow.constant.SamplingRate;
import com.onenow.constant.TradeType;
import com.onenow.data.DataSampling;
import com.onenow.execution.NetworkConfig;
import com.onenow.execution.NetworkService;
import com.onenow.instrument.Investment;
import com.onenow.research.Candle;
import com.onenow.util.ParseDate;

public class TSDB {
	
	private InfluxDB DB;
	private Lookup dbLookup = new Lookup();
	private DataSampling dataSampling = new DataSampling();
	
	private NetworkService tsdbService = new NetworkConfig().tsdb;
	
	/**
	 * Default constructor connects to database
	 */
	public TSDB() {
		dbConnect();
		dbCreate();
	}
	
// INIT
private void dbConnect() { 
	boolean tryToConnect = true;
	
	while(tryToConnect) {
		try {
			tryToConnect = false;
			System.out.println("\n" + "CONNECTING TO TSDB...");
			setDB(InfluxDBFactory.connect(	tsdbService.protocol+"://"+tsdbService.URI+":"+tsdbService.port.toString(), 
											tsdbService.user, tsdbService.pass));
		} catch (Exception e) {
			tryToConnect = true;
			System.out.println("\n" + "...COULD NOT CONNECT TO TSDB: ");
			e.printStackTrace();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				// nothing to do
			}
		}
	} 
	System.out.println("CONNECTED TO TSDB!");
}

private void dbCreate() {
	try {
		getDB().createDatabase(DBname.PRICE.toString());
		getDB().createDatabase(DBname.SIZE.toString());
	} catch (Exception e) {
		// Throws exception if the DB already exists
		// e.printStackTrace();
	}
}

// PRICE
public void writePrice(Long time, Investment inv, TradeType tradeType, Double price, InvDataSource source, InvDataTiming timing) {
	String name = dbLookup.getInvestmentKey(inv, tradeType, source, timing);
	Serie serie = new Serie.Builder(name)
	.columns("time", "price")
	.values(time, price)
	.build();
	System.out.println("WRITE " + DBname.PRICE.toString() + " " + serie);
	getDB().write(DBname.PRICE.toString(), TimeUnit.MILLISECONDS, serie);
}

public List<Candle> readPriceFromDB(	Investment inv, TradeType tradeType, SamplingRate sampling,
										String fromDate, String toDate,
										InvDataSource source, InvDataTiming timing) {
	
		List<Candle> candles = new ArrayList<Candle>();
		
		String key = dbLookup.getInvestmentKey(inv, tradeType, source, timing);

		List<Serie> series = queryPrice(DBname.PRICE.toString(), key, sampling, fromDate, toDate);

		candles = priceSeriesToCandles(series); 

		System.out.println("Cache Chart/Price READ: L1 " + fromDate + " " + toDate + " " + " for " + key + " Prices: " + candles.toString());
		
		return candles;
	}

/**
 * Price queries per http://influxdb.com/docs/v0.7/api/aggregate_functions.html
 * @param dbName
 * @param serieName
 * @param sampling
 * @param fromDate
 * @param toDate
 * @return
 */
public List<Serie> queryPrice(String dbName, String serieName, SamplingRate sampling, String fromDate, String toDate) {
	List<Serie> series = new ArrayList<Serie>();
	 
	String query = 	"SELECT " +
						"FIRST(price)" + ", " +			
						"LAST(price)" + ", " +			
						"DIFFERENCE(price)" + ", " +							
						"MIN(price)" + ", " +			
						"MAX(price)" + ", " +			
						"MEAN(price)" + ", " +			
						"MODE(price)" + ", " +			
						"MEDIAN(price)" + ", " +						
						"STDDEV(price)" + ", " +						
						"DISTINCT(price)" + ", " +					
						"COUNT(price)" + ", " +			
						"SUM(price) " + ", " + 
						"DERIVATIVE(price)" + 						
					"FROM " + "\"" + serieName + "\" " +
					"GROUP BY " +
						"time" + "(" + dataSampling.getGroupByTimeString(sampling) + ") " + 
					// "FILL(0) " +
					"WHERE " +
					"time > " + "'" + fromDate + "' " + 
					"AND " +
					"time < " + "'" + toDate + "' "; 
					
	// TODO: SELECT PERCENTILE(column_name, N) FROM series_name group by time(10m) ...
	// TODO: SELECT HISTOGRAM(column_name) FROM series_name ...
	// TODO: SELECT TOP(column_name, N) FROM series_name ...
	// TODO: SELECT BOTTOM(column_name, N) FROM series_name ...
	
	try {
		System.out.println("#PRICE# QUERY " + query);
		series = getDB().query(	dbName, query, TimeUnit.MILLISECONDS);
	} catch (Exception e) {
//		e.printStackTrace();  some time series don't exist or have data
	}
	return series;
}

private List<Candle> priceSeriesToCandles(List<Serie> series) {
	List<Candle> candles = new ArrayList<Candle>();
			
	String s="";
	for (Serie ser : series) {
		for (String col : ser.getColumns()) {
			s = s + col + "\t";
//			System.out.println("column " + col); column names
		}
		s = s + "\n";
		for (Map<String, Object> row : ser.getRows()) {
			Candle candle = new Candle();
			Integer i=0;
			for (String col : ser.getColumns()) {	// iterate columns to create candle
				s = s + row.get(col) + "\t";
				// System.out.println("row " + row + " " + row.get(col)); // full row
				if(i.equals(1)) {
					candle.openPrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(2)) {
					candle.closePrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(3)) {
					candle.difference = new Double(extractQueryString(row, col));
				}				
				//				"FIRST(price)" + ", " +			
				//				"LAST(price)" + ", " +			
				//				"DIFFERENCE(price)" + ", " +							
				if(i.equals(4)) {
					candle.lowPrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(5)) {
					candle.highPrice = new Double(extractQueryString(row, col));
				}
				//				"MIN(price)" + ", " +			
				//				"MAX(price)" + ", " +			
				if(i.equals(6)) {
					candle.meanPrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(7)) {
					candle.modePrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(8)) {
					candle.medianPrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(9)) {
					candle.stddevPrice = new Double(extractQueryString(row, col));
				}
				//				"MEAN(price)" + ", " +			
				//				"MODE(price)" + ", " +			
				//				"MEDIAN(price)" + ", " +						
				//				"STDDEV(price)" + ", " +						
				if(i.equals(10)) {
					candle.distinctPrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(11)) {
					candle.countPrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(12)) {
					candle.sumPrice = new Double(extractQueryString(row, col));
				}
				if(i.equals(13)) {
					candle.derivativePrice = new Double(extractQueryString(row, col));
				}
				//				"DISTINCT(price)" + ", " +					
				//				"COUNT(price)" + ", " +			
				//				"SUM(price) " + ", " + 
				//				"DERIVATIVE(price)" + 						

				i++;
			}
			s = s + "\n";
			candles.add(candle);
		}
	}
//	System.out.println("CANDLE: " + s + "\n");	full candle
	return candles;
}

private String extractQueryString(Map<String, Object> row, String col) {
	String s = "";
	try {
		s = row.get(col).toString();
	} catch (Exception e) {
		s = "0.0";
		System.out.println("WARNING: NULL query result: extractQueryString" + "row " + row + " " + row.get(col));
		// TODO: something to do for defaults?
		// e.printStackTrace();
	}
	return s;
}


// SIZE
public void writeSize(Long time, Investment inv, TradeType tradeType, Integer size, InvDataSource source, InvDataTiming timing) {
	String name = dbLookup.getInvestmentKey(inv, tradeType, source, timing);
	Serie serie = new Serie.Builder(name)
	.columns("time", "size")
	.values(time, size)
	.build();
	System.out.println("WRITE " + DBname.SIZE.toString() + " " + serie);
	getDB().write(DBname.SIZE.toString(), TimeUnit.MILLISECONDS, serie);
}

public List<Integer> readSizeFromDB(	Investment inv, TradeType tradeType, SamplingRate sampling,
										String fromDate, String toDate,
										InvDataSource source, InvDataTiming timing) {
	
	List<Integer> sizes = new ArrayList<Integer>();
	
	String key = dbLookup.getInvestmentKey(inv, tradeType, source, timing);
	
	List<Serie> series = querySize(	DBname.SIZE.toString(), key,  sampling, fromDate, toDate);
	
	sizes = sizeSeriesToInts(series); 
	
	System.out.println("Cache Chart/Size READ: L1 " + fromDate + " " + toDate + " " + " for " + key + " Sizes: " + sizes.toString());

	return sizes;
}

/**
 * Size/volume queries per http://influxdb.com/docs/v0.7/api/aggregate_functions.html
 * @param dbName
 * @param serieName
 * @param sampling
 * @param fromDate
 * @param toDate
 * @return
 */
public List<Serie> querySize(String dbName, String serieName, SamplingRate sampling, String fromDate, String toDate) {
	List<Serie> series = new ArrayList<Serie>();
	
	String query = 	"SELECT " +
						"FIRST(size)" + ", " +			
						"LAST(size)" + ", " +			
						"DIFFERENCE(size)" + ", " +							
						"MIN(size)" + ", " +			
						"MAX(size)" + ", " +			
						"MEAN(size)" + ", " +			
						"MODE(size)" + ", " +			
						"MEDIAN(size)" + ", " +						
						"DISTINCT(size)" + ", " +					
						"STDDEV(size)" + ", " +						
						"DERIVATIVE(size)" + ", " +						
						"COUNT(size)" + ", " +			
						"SUM(size) " +  
					"FROM " + "\"" + serieName + "\" " +
					"GROUP BY " +
						"time" + "(" + dataSampling.getGroupByTimeString(sampling) + ") " +
					// "FILL(0) " +
					"WHERE " +
					"time > " + "'" + fromDate + "' " + 
					"AND " +
					"time < " + "'" + toDate + "' ";
					
	try {
		System.out.println("#SIZE# QUERY " + query);
		series = getDB().query(	dbName, query, TimeUnit.MILLISECONDS);
	} catch (Exception e) {
//		e.printStackTrace(); some time series don't exist or have data
	}
	
	return series;
}

private List<Integer> sizeSeriesToInts(List<Serie> series) {
	List<Integer> sizes = new ArrayList<Integer>();
	
	String s="";
	for (Serie ser : series) {
		for (String col : ser.getColumns()) {
			s = s + col + "\t";
//			System.out.println("column " + col); column names
		}
		s = s + "\n";
		for (Map<String, Object> row : ser.getRows()) {
			Candle candle = new Candle();
			Integer i=0;
			for (String col : ser.getColumns()) {	// iterate columsn to get ints
				s = s + row.get(col) + "\t";
				// System.out.println("row " + row + " " + row.get(col)); full row
				if(i.equals(1)) {
				}
				if(i.equals(2)) {
				}
				if(i.equals(3)) {
				}
				//				"FIRST(price)" + ", " +			
				//				"LAST(price)" + ", " +			
				//				"DIFFERENCE(price)" + ", " +							
				if(i.equals(4)) {
				}
				if(i.equals(5)) {
				}
				//				"MIN(price)" + ", " +			
				//				"MAX(price)" + ", " +	
				if(i.equals(6)) {
				}
				if(i.equals(7)) {
				}
				if(i.equals(8)) {
				}
				if(i.equals(9)) {
				}
				//				"MEAN(price)" + ", " +			
				//				"MODE(price)" + ", " +			
				//				"MEDIAN(price)" + ", " +						
				//				"STDDEV(price)" + ", " +						
				if(i.equals(10)) {
				}
				if(i.equals(11)) {
				}
				if(i.equals(12)) {
					Double num = new Double(extractQueryString(row, col));
					sizes.add((int) Math.round(num));
				}
				if(i.equals(13)) {
				}
				//				"DISTINCT(price)" + ", " +					
				//				"COUNT(price)" + ", " +			
				//				"SUM(price) " + ", " + 
				//				"DERIVATIVE(price)" + 	
				i++;
			}
			s = s + "\n";
		}
	}
//	System.out.println("SIZES: " + s + "\n");	full series
	return sizes;
}


	// PRIVATE


	// TEST
	
	
	// PRINT
	
	// SET GET
	private InfluxDB getDB() {
		return DB;
	}
	
	private void setDB(InfluxDB dB) {
		DB = dB;
	}	
	
}