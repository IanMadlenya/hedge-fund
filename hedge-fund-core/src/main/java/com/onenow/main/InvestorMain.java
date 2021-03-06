package com.onenow.main;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.onenow.constant.BrokerMode;
import com.onenow.constant.InvApproach;
import com.onenow.data.InitMarket;
import com.onenow.data.InvestmentList;
import com.onenow.execution.BrokerActivityImpl;
import com.onenow.execution.BrokerInteractive;
import com.onenow.instrument.InvestmentIndex;
import com.onenow.instrument.Underlying;
import com.onenow.portfolio.Portfolio;
import com.onenow.portfolio.PortfolioFactory;
import com.onenow.util.ParseDate;

public class InvestorMain {
		
	private static Portfolio marketPortfolio = new Portfolio();
	private static BrokerInteractive brokerInteractive;

	private static InvestmentList invList = new InvestmentList();
	private static ParseDate parseDate = new ParseDate();

	public static void main(String[] args) throws ParseException, InterruptedException {

	    // choose relevant timeframe
	    String toDashedDate = parseDate.getDashedDatePlus(parseDate.getDashedToday(), 1);

		brokerInteractive = new BrokerInteractive(BrokerMode.PRIMARY, marketPortfolio); 

		InitMarket initMarket = new InitMarket(	marketPortfolio, 
												invList.getUnderlying(invList.someStocks), invList.getUnderlying(invList.someIndices),
												invList.getUnderlying(invList.futures), invList.getUnderlying(invList.options),
    											toDashedDate);						

		brokerInteractive.getLiveQuotes(); 
		PortfolioFactory portfolioFactory = new PortfolioFactory(brokerInteractive, marketPortfolio);
		portfolioFactory.launch();							
	}
	
}
