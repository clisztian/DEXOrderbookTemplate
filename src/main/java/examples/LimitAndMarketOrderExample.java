package examples;

import java.util.concurrent.ExecutionException;

import exchange.DEX;
import exchange.OrderInterface;

/**
 * A simple example of submitting orders to the DEX exchange
 * @author lisztian
 *
 */
public class LimitAndMarketOrderExample {

	static long spread_mid_point = 10_000; //10 ltc 
	static int n_bid_orders = 50;
	static int n_ask_orders = 50;
	static int n_price_levels = 20;
	static int max_size_order = 10;
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		/**
		 * Instantiate an exchange
		 */
		DEX dex = new DEX();		
		/**
		 * Register some participants/traders
		 */
		dex.registerTradersToDEX(40);
		/**
		 * Instantiate order interface to the exchange
		 */
		OrderInterface order_interface = new OrderInterface(dex);		
		/**
		 * Create an initial outstanding market/auction point 
		 */
		order_interface.createInitialMarket(spread_mid_point, n_bid_orders, n_ask_orders, n_price_levels, max_size_order);
		
		/**
		 * Add any further orders here
		 */
		
		
		/**
		 * Print out orderbook solution here
		 */
		
		
	}
	
}
