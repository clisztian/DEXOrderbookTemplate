package order;

import java.time.LocalDateTime;

import exchange.core2.core.common.OrderAction;
import exchange.core2.core.common.OrderType;

/**
 * 
 * A generic order for the order book
 * 
 * @author lisztian
 *
 */
public class AnyOrder {


	private long order_id;

	private OrderType type;
	private OrderAction action;
	private Side side;
	private long price;
	private long volume;
	public enum Side {
		BUY, SELL	
	}

	private LocalDateTime dt;




	public AnyOrder(long order_id, OrderType type, OrderAction action, long price, long volume,
			long trader_id, LocalDateTime dt) {
		super();
		this.order_id = order_id;
		this.type = type;
		this.action = action;
		this.price = price;
		this.volume = volume;
		this.dt = dt;
		this.trader_id = trader_id;
	}

	private long trader_id;
	
	
	public AnyOrder(long order_id, OrderType type, OrderAction action, long price, long volume, long trader_id) {
		super();
		this.order_id = order_id;
		this.type = type;
		this.action = action;
		this.price = price;
		this.volume = volume;
		this.trader_id = trader_id;
	}
	
	public AnyOrder(long order_id, OrderType type, Side side, long price, int volume) {
		super();
		this.order_id = order_id;
		this.type = type;
		this.side = side;
		this.price = price;
		this.volume = volume;
	}
	

	
	public AnyOrder(long order_id, OrderType type, long price, int volume) {
		super();
		this.order_id = order_id;
		this.type = type;
		this.price = price;
		this.volume = volume;
	}
	
	
	
	public long getOrder_id() {
		return order_id;
	}
	public void setOrder_id(long order_id) {
		this.order_id = order_id;
	}
	public OrderType getType() {
		return type;
	}
	public void setType(OrderType type) {
		this.type = type;
	}
	public long getPrice() {
		return price;
	}
	public void setPrice(long price) {
		this.price = price;
	}
	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}

	public Side getSide() {
		return side;
	}

	public long getTrader_id() {
		return trader_id;
	}

	public void setTrader_id(long trader_id) {
		this.trader_id = trader_id;
	}

	public void setSide(Side side) {
		this.side = side;
	}

	public OrderAction getAction() {
		return action;
	}

	public void setAction(OrderAction action) {
		this.action = action;
	}
	public LocalDateTime getDt() {
		return dt;
	}

	public void setDt(LocalDateTime dt) {
		this.dt = dt;
	}

	public void adjustSize(long total_vol) {
		volume = volume - total_vol;		
	}

}
