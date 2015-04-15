# HEDGE 
## Open source hedge fund platform

# Documentation:
>>##### * For more detailed information read the <a href="http://pablo-tech.github.io/hedge-fund/JavaDoc/index.html">javadoc</a>

# Running the Application:

> ### * InvestorMain  
>>##### Logic: make investment decisions
>>##### Location: Porfolio package

> ### * BrokerMain
>> ##### Logic: gather market data and transmit buy/sell instructions
>> ##### Location: Execution package

> ### * HistorianMain 
>> ##### Logic: keeps a complete set of market historical data
>> ##### Location: Data package 

> ### * RiskMain 
>> ##### Logic: constraints transactions to meet risk management criteria
>> ##### Location: Risk package

> ### * TestMain (Test package)
>> ##### Logic: performs system QA
>> ##### Location: Test package

# System Requirements:

> ### * Third Party Software  
>>##### Interactive Brokers: the IB_Gateway or Trader Workstation is required.  To run it in the cloud, a special configuration is required.
>>##### Location: <a href="https://www.interactivebrokers.com/en/?f=%2Fen%2Fcontrol%2Fsystemstandalone-ibGateway.php%3Fos%3Dunix">Linux download for the IB_Gateway</a>


> ### * Amazon Web Services  


# Source Packages:

> ### * Admin
>> ##### Location: /src/com.onenow.admin
>> ##### Logic: system administration

> ### * Alpha
>> ##### Logic: Buy/sell modeling
>> ##### Location: /src/com.onenow.alpha

> ### * AWS
>> ##### Logic: Cloud instantiation
>> ##### Location: /src/com.onenow.AWS

> ### * Constant
>> ##### Location: /src/com.onenow.constant

> ### * Cost
>> ##### Logic: Transaction ROI
>> ##### Location: /src/com.onenow.cost

> ### * Data
>> ##### Logic: Ingestion
>> ##### Location: /src/com.onenow.data

> ### * Database
>> ##### Location: /src/com.onenow.database

> ### * Execution
>> ##### Fastest & minimal (*)
>> ##### Location: /src/com.onenow.execution

> ### * Instrument
>> ##### Logic: Financial vehicles (*)
>> ##### Location: /src/com.onenow.instrument

> ### * Portfolio
>> ##### Logic: Set of choices made
>> ##### Location: /src/com.onenow.portfolio

> ### * Research
>> ##### Logic: Human
>> ##### Location: /src/com.onenow.research

> ### * Risk
>> ##### Logic: Transaction downside
>> ##### Location: /src/com.onenow.risk

> ### * Test
>> ##### Logic: QA 
>> ##### Location: /src/com.onenow.test

> ### * Util
>> ##### Logic: Stats, etc.
>> ##### Location: /src/com.onenow.util

# Source by 3rd Parties:

> ### * Interactive Brokers
>> ##### Logic: Interactive Brokers API demo
>> ##### Location: /src-3rd-party/

# Repository Folders:

> ### * Simple Workflow
>> ##### Location: /gen-swf/

> ### * Salesforce 
>> ##### Location: /gen-sforce/

> ### * Dependencies

> ### * Config

> ### * Lib

> ### * SOAP

> ### * Target

# Other Folders:

> ### * Simple Workflow
>> ##### Location: /gen-swf/
