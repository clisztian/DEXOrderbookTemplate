package exchange;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import eventcenter.DEXEventHandler;
import exchange.core2.core.ExchangeApi;
import exchange.core2.core.ExchangeCore;
import exchange.core2.core.SimpleEventsProcessor;
import exchange.core2.core.common.CoreSymbolSpecification;
import exchange.core2.core.common.SymbolType;
import exchange.core2.core.common.api.ApiAddUser;
import exchange.core2.core.common.api.ApiAdjustUserBalance;
import exchange.core2.core.common.api.binary.BatchAddSymbolsCommand;
import exchange.core2.core.common.cmd.CommandResultCode;
import exchange.core2.core.common.config.ExchangeConfiguration;
import order.AnyOrder;
import trader.Trader;
import trader.TraderNames;

public class DEX {

	/**
	 * Event handler
	 */
	private DEXEventHandler dex_events;
	/**
	 * Events processor (event generator from matcher)
	 */
	private SimpleEventsProcessor event_processor;
	/**
	 * Configurations for the exchange
	 */
	private ExchangeConfiguration conf;
	
	/**
	 * The Exchange DEX protocol (with matcher)
	 */
	private ExchangeCore exchangeCore;
	
	/**
	 * The access point for sending orders from traders/MMs
	 */
	private ExchangeApi api;
	
	/**
	 * Create a ticker symbol for trading (represented by an int)
	 */
    final int symbolXbtLtc = 241;
	


	/**
     * Security details 
     */
    private CoreSymbolSpecification SYMBOLSPEC;
	
    /**
     * For creating order etc
     */
    Random rng = new Random(13);
	
    
    /**
     * Keeping track of the traders/participants of exchnage
     */
    private Trader[] my_traders;
    /**
     * Reference trader by id
     */
	private IntObjectHashMap<Trader> trader_map;
	
	/**
	 * Default configurations of the exchange
	 * Creates the exchange, the event handler, creates a security, a list of participants
	 * 
	 */
	public DEX() {
		
		
		/**
		 * Initiate exchange event handler
		 */
		dex_events = new DEXEventHandler();
		
		/**
		 * Instantiate a SimpleEventsProcessor - this is a wrapper around a ObjLongConsumer
		 */
		event_processor = new SimpleEventsProcessor(dex_events);
		
		
		// default exchange configuration
        conf = ExchangeConfiguration.defaultBuilder().build();

        // build exchange core and register a simple events processor 
        exchangeCore = ExchangeCore.builder()
                .resultsConsumer(event_processor)
                .exchangeConfiguration(conf)
                .build();

        // start up the threads for the exchange, waiting for orders with empty book
        exchangeCore.startup();

        // get exchange API for publishing commands
        api = exchangeCore.getApi();
		
		
        /**
         * Create Symbol/Security to trade on for exchange (SIX already has these)
         */
		SYMBOLSPEC = CoreSymbolSpecification.builder()
				.symbolId(symbolXbtLtc)         // symbol id
                .type(SymbolType.CURRENCY_EXCHANGE_PAIR)
                .baseCurrency(Security.currencyCodeXbt) 
                .quoteCurrency(Security.currencyCodeLtc)   
                .baseScaleK(1_000_000L) 
                .quoteScaleK(10_000L)   
                .takerFee(1900L)        
                .makerFee(700L)         
                .build();
		
	}
	
	
	
	
	
	
	
	/**
	 * Register some participants (traders) to the exchange and give them some crypto 
	 * @param n_traders
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public void registerTradersToDEX(int n_traders) throws InterruptedException, ExecutionException {
		
		
		Future<CommandResultCode> future;
		
		future = api.submitBinaryDataAsync(new BatchAddSymbolsCommand(SYMBOLSPEC));
        
		
        my_traders = generateTraderArray(n_traders);
        trader_map = new IntObjectHashMap<Trader>();

        int count = 0;
        for(int i = 0; i < my_traders.length; i++) {
        	
        	/**
        	 * Submit participant 
        	 */
        	future = api.submitCommandAsync(ApiAddUser.builder()
                    .uid(my_traders[i].getUid())
                    .build());

        	System.out.println("ApiAddUser " + i + " result: " + future.get());

            for(trader.Trader.Currency curr : my_traders[i].getCurrency()) {
            	
            	
            	future = api.submitCommandAsync(ApiAdjustUserBalance.builder()
                        .uid(my_traders[i].getUid())
                        .currency(curr.getCurrency())
                        .amount(curr.getTotal_amount())
                        .transactionId(3000+count)
                        .build());     
            	
            	count++;
            	
            	System.out.println("ApiAdjustUserBalance 2 result: " + future.get());
            }
            
            
            trader_map.put(my_traders[i].getUid(), my_traders[i]);
        }		
	}
	
	/**
	 * Generate a bunch of traders
	 * and given then some crypto in their wallets
	 * @param n_traders
	 * @return
	 */
	private Trader[] generateTraderArray(int n_traders) {
		
		
		Trader[] traders = new Trader[n_traders];
		
		for(int i = 0; i < n_traders; i++) {			
			traders[i] = new Trader().setUid(1980+i).setTotal_amount(Security.currencyCodeLtc, 20_000_000_000L).setTotal_amount(Security.currencyCodeXbt, 20_000_000_000L);
			traders[i].setName(TraderNames.names[rng.nextInt(TraderNames.names.length - 1)].split("[ ]+")[1]);
			
			traders[i].setAggressiveness(rng.nextFloat());
			traders[i].setObserveness(rng.nextFloat());
			traders[i].setRisk_adverse(1f + 2f*rng.nextFloat());
			traders[i].setWithdraw_rate(.5f + .5f*rng.nextFloat());
			traders[i].setUpdate_rate(.5f);
			
		}
		
		return traders;
		
	}
	
	
	/**
	 * Reference to the outstanding orders
	 * @return
	 */
	public final ConcurrentHashMap<Long, AnyOrder> getOutstandingOrders() {
		return dex_events.getOutstnd_orders();
	}


	public ExchangeApi getApi() {
		return api;
	}

	public void setApi(ExchangeApi api) {
		this.api = api;
	}
	
	public Trader[] getMy_traders() {
		return my_traders;
	}

	public void setMy_traders(Trader[] my_traders) {
		this.my_traders = my_traders;
	}
	
    public int getSymbolXbtLtc() {
		return symbolXbtLtc;
	}
	
}
