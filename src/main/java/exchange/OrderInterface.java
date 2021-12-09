package exchange;

import java.util.Random;
import java.util.concurrent.Future;

import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;
import exchange.core2.core.common.api.ApiCancelOrder;
import exchange.core2.core.common.api.ApiMoveOrder;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.cmd.CommandResultCode;
import order.AnyOrder;
import trader.Trader;



/**
 * Simple order interface for creating initial market and outstanding orders
 * 
 * Withdraw/Canceling random orders
 * Updating random orders
 * Submitting GTC/FoK orders
 * 
 * @author lisztian
 *
 */
public class OrderInterface {

	
	Random rng = new Random(344);
	private DEX dex;
	
	/**
	 * Global ID for submitting orders
	 */
	private int last_id;
	
	/**
	 * Exchange with a defined set of traders for a given security
	 * @param exch
	 */
	public OrderInterface(DEX exch) {		
		this.dex = exch;	
	}
	
	
	/**
	 * Create initial market for security at DEX
	 * @param spread_mid_point current auction price
	 * @param n_bids number of bids to create
	 * @param n_asks number of asks to create
	 * @param price_levels number of price levels
	 * @param max_order_size largest ordersize in volume
	 */
	public void createInitialMarket(long spread_mid_point, int n_bids, int n_asks, int price_levels, int max_order_size) {
		

		int n_orders_bid = n_bids;
		int n_orders_ask = n_asks;
	    
		Future<CommandResultCode> future = null;
	    
	    /**
	     * Create the bid side  
	     */
		for(int i = 0; i < n_orders_bid; i++) {
      	
			int trader_id = rng.nextInt(dex.getMy_traders().length);
			int size = 1 + rng.nextInt(max_order_size);
			long price = spread_mid_point - Security.tick_size - rng.nextInt(price_levels)*Security.tick_size;
      	
			future = dex.getApi().submitCommandAsync(getGtcBidOrder(dex.getMy_traders()[trader_id], size, price));
      	
		}
      
		/**
		 * Create the ask side
		 */
		for(int i = 0; i < n_orders_ask; i++) {
      	
			int trader_id = rng.nextInt(dex.getMy_traders().length);
			int size = 1 + rng.nextInt(max_order_size);
			long price = spread_mid_point + Security.tick_size + rng.nextInt(price_levels)*Security.tick_size;
      	
			future = dex.getApi().submitCommandAsync(getGtcAskOrder(dex.getMy_traders()[trader_id], size, price));
      	
		}
  
   
		if(future != null)  while(!future.isDone()) { }
		
	}
	
	
	
	
	
	
	/**
	 * Gets a random order ID from current outstanding
	 * @return
	 */
	private long getRandomOrder() {
		
		int i = 0;
		//System.out.println("size: " + dex.getOutstandingOrders().size());
		
		if(dex.getOutstandingOrders().size() > 0) {
			
			int rand = rng.nextInt(dex.getOutstandingOrders().size());
			for(Long orderid : dex.getOutstandingOrders().keySet()) {
				
				if(i == rand) {
					return orderid;
				}
				i++;
			}
			return -1;
			
		}
		return -1;	
	}
	
	/**
	 * Cancels a random order, if orders remain
	 */
	public void cancelRandomOrder() {
		
		long orderid = getRandomOrder();
		
		if(orderid >= 0) {
			
			AnyOrder my_order = dex.getOutstandingOrders().get(orderid);
			
			//System.out.println(orderid + " " + dex.getOutstandingOrders().size());
			
			dex.getApi().submitCommandAsync(ApiCancelOrder.builder()
			        .uid(my_order.getTrader_id())
			        .orderId(orderid)
			        .symbol(dex.getSymbolXbtLtc())
			        .build());
			
		}
		else {
			System.out.println("No orders left");
		}
	}
	
	/**
	 * Updates a random order to a new random price level
	 * Will match if price offered at BBO
	 */
	public void updateRandomOrder() {
		
		long orderid = getRandomOrder();
		
		if(orderid >= 0) {
			
			AnyOrder my_order = dex.getOutstandingOrders().get(orderid);
			int n_ticks = -5 + rng.nextInt(10);
			if(n_ticks == 0) n_ticks++;
			
			long new_price = my_order.getPrice() + Security.tick_size*n_ticks;
				
			
			dex.getApi().submitCommandAsync(ApiMoveOrder.builder()
	                .uid(my_order.getTrader_id())
	                .orderId(orderid)
	                .newPrice(new_price)
	                .symbol(dex.getSymbolXbtLtc())
	                .build());
		}
		else {
			System.out.println("No orders left");
		}
		
	}
	

	
	/**
	 * Immediate or Cancel - equivalent to strict-risk market order
	 * @param anyTrader
	 * @param size
	 * @param price
	 * @return
	 */
	public ApiPlaceOrder getIoCBidOrder(Trader anyTrader, int size, long price) {
		
		last_id  = last_id + 1;
		return ApiPlaceOrder.builder()
				
			.uid(anyTrader.getUid())
	        .orderId(last_id)
	        .price(price)	        
	        .size(size) // order size is 12 lots
	        .action(OrderAction.BID)
	        .orderType(OrderType.FOK) // Good-till-Cancel
	        .symbol(dex.getSymbolXbtLtc())
	        .build();

	}
	
	/**
	 * Immediate or Cancel - equivalent to strict-risk market order
	 * @param anyTrader
	 * @param size
	 * @param price
	 * @return
	 */
	public ApiPlaceOrder getIoCAskOrder(Trader anyTrader, int size, long price) {
		
		last_id  = last_id + 1;
		return ApiPlaceOrder.builder()
				
			.uid(anyTrader.getUid())
	        .orderId(last_id)
	        .price(price)	        
	        .size(size) // order size is 12 lots
	        .action(OrderAction.ASK)
	        .orderType(OrderType.IOC) //Insert or cancel
	        .symbol(dex.getSymbolXbtLtc())
	        .build();

	}
	
	
	/**
	 * Place a good til close order on the bid side for a trader/size/price
	 * @param anyTrader
	 * @param size
	 * @param price
	 * @return
	 */
	public ApiPlaceOrder getGtcBidOrder(Trader anyTrader, int size, long price) {
		
		last_id  = last_id + 1;
		return ApiPlaceOrder.builder()
		
			.uid(anyTrader.getUid())
	        .orderId(last_id)
	        .price(price)
	        .reservePrice(Long.MAX_VALUE) // can move bid order up to the 1.56 LTC, without replacing it
	        .size(size) // order size is 12 lots
	        .action(OrderAction.BID)
	        .orderType(OrderType.GTC) // Good-till-Cancel
	        .symbol(dex.getSymbolXbtLtc())
	        .build();

	}
	
	/**
	 * Place a good til close order on the ask side for a trader/size/price
	 * @param anyTrader
	 * @param size
	 * @param price
	 * @return
	 */
	public ApiPlaceOrder getGtcAskOrder(Trader anyTrader, int size, long price) {
		
		last_id  = last_id + 1;
		return ApiPlaceOrder.builder()
		
			.uid(anyTrader.getUid())
	        .orderId(last_id)
	        .price(price)
	        .size(size) // order size is 12 lots
	        .action(OrderAction.ASK)
	        .orderType(OrderType.GTC) // Good-till-Cancel
	        .symbol(dex.getSymbolXbtLtc())
	        .build();

	}

	
	
	
	
	
}
