package eventcenter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exchange.core2.core.IEventsHandler;
import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.api.ApiMoveOrder;
import exchange.core2.core.common.api.ApiPlaceOrder;
import exchange.core2.core.common.cmd.CommandResultCode;
import javafx.application.Platform;
import order.AnyOrder;

/**
 * Event handler for the main events that occur from the matcher. 
 * The events that can occur are 
 * 
 * Insert (place order)
 * Withdraw 
 * Update order
 * 
 * Keeps track of all outstanding orders in a ConcurrentHashMap
 * 
 * 
 * @author lisztian
 *
 */
public class DEXEventHandler implements IEventsHandler {

	/**
	 * Logging output handler
	 */
	private static Logger LOGGER = LoggerFactory.getLogger(DEXEventHandler.class);
	
	
	/**
     * A map of all outstanding orders, needs to be asychronized
     */
    private ConcurrentHashMap<Long, AnyOrder> outstnd_orders;
	
	
    /**
     * Initializes empty outstanding order list
     * 
     * TODO: Initialize Orderbook
     */
    public DEXEventHandler() {    
    	
    	outstnd_orders = new ConcurrentHashMap<Long, AnyOrder>();	
    	/**
    	 * Orderbook logic
    	 */ 
    }
    
    
	
	@Override
	public void commandResult(ApiCommandResult commandResult) {
		
        if(commandResult.getResultCode() == CommandResultCode.SUCCESS) {
        	
        	if(commandResult.getCommand() instanceof ApiPlaceOrder) {
        		/**
        		 * Insert order was succesfull, get order details
        		 */
        		ApiPlaceOrder place = ((ApiPlaceOrder)commandResult.getCommand());
        		LOGGER.info("Insert: " + place);	        		
        		/**
        		 * Create timestamp for new event
        		 */
        		LocalDateTime dt = LocalDateTime.now();        		
        		/**
        		 * Grab order id
        		 */
        		long order_id = place.orderId;
        		/**
        		 * Create a new order for the outstanding orders and orderbook
        		 */
        		AnyOrder new_order = new AnyOrder(order_id, place.orderType, place.action, place.price, place.size, place.uid, dt);
        		/**
        		 * Put into order hashmap of outstanding orders
        		 */	
        		outstnd_orders.put(order_id, new_order);
        		/**
            	 * Orderbook logic
            	 */             		
        	}     	
        	else if(commandResult.getCommand() instanceof ApiMoveOrder) {
        		        		
        		ApiMoveOrder move = ((ApiMoveOrder)commandResult.getCommand());
        		LOGGER.info("Update order: " + move);	  
        		/**
        		 * Get reference to old outstanding order and remove
        		 */
        		AnyOrder old_order = outstnd_orders.remove(move.orderId);      		
        		/**
        		 * Generate timestamp for event
        		 */
        		LocalDateTime dt = LocalDateTime.now();        		
        		/**
        		 * Create new order based on old order
        		 */
        		AnyOrder new_order = new AnyOrder(move.orderId, old_order.getType(), old_order.getAction(), move.newPrice, old_order.getVolume(), move.uid, dt);
        		/**
        		 * Put into outstanding orders
        		 */
        		outstnd_orders.put(move.orderId, new_order);
        		/**
            	 * Orderbook logic
            	 */
        	}      	
        }
		
		
	}

	/**
	 * Automatically called whenever a trade event happens from the matcher
	 * The outstanding orders and the orderbook are updated according to the trade details
	 */
	@Override
	public void tradeEvent(TradeEvent tradeEvent) {		
		/**
		 * Log the event
		 */
		LOGGER.info("Trade: " + tradeEvent);		
		/**
		 * Is the order completely filled on taker side
		 */
		boolean taker_completed = tradeEvent.isTakeOrderCompleted();		
		/**
		 * Get referenc to order id from taker
		 */
        long taker_order_id = tradeEvent.getTakerOrderId();        
        /**
         * Get list of fills on other side of book
         */
        List<Trade> trades = tradeEvent.getTrades();        
        /**
         * Get the volume (number shares/contracts) of the trade 
         */
        long total_vol = tradeEvent.getTotalVolume();
		
        /**
         * If taker side complete, remove order from outstanding orders
         * Otherwise, make a size adjustment to the order by subracting the volume
         */
        if(taker_completed) {
        	
        	AnyOrder old_order = outstnd_orders.remove(taker_order_id);            	
        	/**
        	 * Orderbook logic
        	 */
        }
        else {
        	
        	AnyOrder old_order = outstnd_orders.get(taker_order_id);  
        	//adjust order since partial fill
        	old_order.adjustSize(total_vol);
        	/**
        	 * Orderbook logic
        	 */
        }
        
        /**
         * Now process the maker side 
         */
        for(Trade trade : trades) {            	        	
        	/**
        	 * If maker side complete, remove, otherwise adjust size
        	 */
        	if(trade.isMakerOrderCompleted()) {
        		
        		AnyOrder old_order = outstnd_orders.remove(trade.getMakerOrderId());
        		/**
            	 * Orderbook logic
            	 */
        	}
        	else {
        		AnyOrder old_order = outstnd_orders.get(trade.getMakerOrderId());
        		old_order.adjustSize(trade.getVolume());
        		/**
            	 * Orderbook logic
            	 */
        	}
        }
	}

	/**
	 * Event that captures when an order had been rejected from the orderbook 
	 * Usually happens during an stop trading alert, not enough funds, unusual size etc 
	 */
	@Override
	public void rejectEvent(RejectEvent rejectEvent) {
		/**
		 * Remove the order from outstanding orders (usually from a withdraw)
		 */
    	AnyOrder old_order = outstnd_orders.remove(rejectEvent.orderId);
    	/**
    	 * Orderbook logic
    	 */	
	}

	/**
	 * Reduce event occurs during a withdraw/deletion of an order
	 */
	@Override
	public void reduceEvent(ReduceEvent reduceEvent) {
		
		/**
		 * Remove the order from outstanding orders (usually from a withdraw)
		 */
    	AnyOrder old_order = outstnd_orders.remove(reduceEvent.orderId);
    	/**
    	 * Orderbook logic
    	 */
	}

	@Override
	public void orderBook(OrderBook orderBook) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * Get handle on all outstanding orders 
	 * @return
	 */
	public ConcurrentHashMap<Long, AnyOrder> getOutstnd_orders() {
		return outstnd_orders;
	}



}
