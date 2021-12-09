package eventcenter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import exchange.core2.core.IEventsHandler.ReduceEvent;
import exchange.core2.core.IEventsHandler.RejectEvent;
import exchange.core2.core.IEventsHandler.Trade;
import exchange.core2.core.IEventsHandler.TradeEvent;
import exchange.core2.core.common.api.ApiMoveOrder;
import exchange.core2.core.common.api.ApiPlaceOrder;
import order.AnyOrder;
import trader.Trader;

/**
 * 
 * @Data
    class TradeEvent {
        public final int symbol;
        public final long totalVolume;
        public final long takerOrderId;
        public final long takerUid;
        public final OrderAction takerAction;
        public final boolean takeOrderCompleted;
        public final long timestamp;
        public final List<Trade> trades;
    }

    @Data
    class Trade {
        public final long makerOrderId;
        public final long makerUid;
        public final boolean makerOrderCompleted;
        public final long price;
        public final long volume;
    }

    @Data
    class ReduceEvent {
        public final int symbol;
        public final long reducedVolume;
        public final boolean orderCompleted;
        public final long price;
        public final long orderId;
        public final long uid;
        public final long timestamp;
    }

    @Data
    class RejectEvent {
        public final int symbol;
        public final long rejectedVolume;
        public final long price;
        public final long orderId;
        public final long uid;
        public final long timestamp;
    }

    @Data
    class CommandExecutionResult {
        public final int symbol;
        public final long volume;
        public final long price;
        public final long orderId;
        public final long uid;
        public final long timestamp;
    }
 * 
 * @return
 */

public class EventParser {




	private IntObjectHashMap<Trader> trader_map;
	public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS");
	
	
	
	/**
	 * Parses a trade event
	 * Returns the taker leg of the trade plus all complete and partial matches
	 * @param dt
	 * @param e
	 * @return
	 */
	public ArrayList<Event> parseTradeEvent(LocalDateTime dt, TradeEvent e) {
		
		
		Event ev = new Event(dt.format(dtf), (int)e.getTakerUid(), "TRADE", ""+e.takerOrderId, ""+e.getTakerAction(),  e.takeOrderCompleted, e.trades.get(0).getPrice(), e.getTotalVolume(), ""+e.symbol, "TAKER")
				.setTrader(trader_map.get((int)e.getTakerUid()).getName());
		
		ArrayList<Event> events = new ArrayList<Event>();
		events.add(ev);
		
		for(Trade t : e.getTrades()) {
			
			events.add(new Event(dt.format(dtf), (int)t.getMakerUid(), "FILL", ""+t.getMakerOrderId(), ""+e.getTakerAction().opposite(), t.isMakerOrderCompleted(), t.getPrice(), t.getVolume(), ""+e.symbol, "MAKER")
					.setTrader(trader_map.get((int)t.getMakerUid()).getName()));
		}
		
		return events;
	}
	
	
	/**
	 * Parse insert event
	 * 
	 * @param dt
	 * @param e
	 * @return
	 */
	public Event parseInsert(LocalDateTime dt, ApiPlaceOrder e) {	
		return new Event(dt.format(dtf), (int)e.uid, "INSERT", "" + e.orderId, "" + e.action, false, e.price, e.size, ""+e.symbol, "", ""+e.orderType).setTrader(trader_map.get((int)e.uid).getName());
	}
	
	
	/**
	 * Parse move/update event with new price on old order
	 * @param dt
	 * @param e
	 * @param old_order
	 * @return
	 */
	public Event parseUpdate(LocalDateTime dt, ApiMoveOrder e, AnyOrder old_order) {
		return new Event(dt.format(dtf), (int)e.uid, "UPDATE", "" + e.orderId, "" + old_order.getAction(), false, e.newPrice, old_order.getVolume(),
				""+e.symbol, "", ""+old_order.getType(), old_order.getDt().format(dtf), old_order.getPrice())
				.setTrader(trader_map.get((int)e.uid).getName());
	}
	
	/**
	 * Parse a reduce event (withdraw or reduce volume)
	 * @param dt
	 * @param e
	 * @param old_order
	 * @return
	 */
	public Event parseReduce(LocalDateTime dt, ReduceEvent e, AnyOrder old_order) {
		return new Event(dt.format(dtf), (int)e.getUid(), "WITHDRAW", ""+e.getOrderId(), ""+old_order.getAction(),  
				 e.isOrderCompleted(), old_order.getPrice(), e.getReducedVolume(), ""+e.symbol, "", ""+old_order.getType() , old_order.getDt().format(dtf))
				.setTrader(trader_map.get((int)e.getUid()).getName());
	}
	
	
	/**
	 * Parse a rejection of an IoC order
	 * @param dt
	 * @param e
	 * @return
	 */
	public Event parseReject(LocalDateTime dt, RejectEvent e, AnyOrder old_order) {

		return new Event(dt.format(dtf), (int)e.getUid(), "REJECT", ""+e.getOrderId(), ""+old_order.getAction(),  
				  e.getPrice(), e.getRejectedVolume(), ""+e.symbol, "IOC").setTrader(trader_map.get((int)e.getUid()).getName());
	}
	
	
	public IntObjectHashMap<Trader> getTrader_map() {
		return trader_map;
	}


	public void setTrader_map(IntObjectHashMap<Trader> trader_map) {
		this.trader_map = trader_map;
	}
	
	
}
