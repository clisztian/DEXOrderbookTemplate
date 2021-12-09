package eventcenter;

/**
 * 
 * A generic event in the market. Anything that comes from the matcher is parsed into an event. 
 * Events are then kept historically in a table/DB for many purposes. Equivalent to Pretrade at DWH/SIX
 * 
 * Market Replay
 * Filtering
 * Spread analysis
 * Orderflow analysis 
 * 
 * 
 * @author lisztian
 *
 */
public class Event {

	/**
	 * when the event took place from the matcher
	 */
	private String timestamp;
	/**
	 * a reference to the trader/member
	 */
	public int trader_id;
	/**
	 * the type of event (trade, fill, insert, reject, alert, etc)
	 */
	private String event_type;
	/**
	 * reference to the order id
	 */
	private String order_id;
	/**
	 * Side of the orderbook (bid/ask)
	 */
	private String order_action;
	/**
	 * if a trade/fill was the order entirely filled
	 */
	private boolean fill_complete;
	/**
	 * price level
	 */
	private long price;
	/**
	 * number shares/contracts
	 */
	private long volume;
	/**
	 * base currency (CHF, EUR, BTC, ETC)
	 */
	private String currency;
	/**
	 * If trade, was the trader_id aggressor/poster (taker/maker)
	 */
	private String agg_post;
	/**
	 * Original order type
	 */
	private String order_type;
	/**
	 * Original timestamp of order (the insert, or timestamp before update)
	 */
	private String orig_timestamp;
	/**
	 * Calculation sign to determine if adding or subtracting from orderbook, if null, was an update. Used for replay
	 */
	private String calculation_sign;
	/**
	 * Reference to a trader name
	 */
	private String trader;
	/**
	 * On an update, reference to original price insert
	 */
	private long orig_price;
	/**
	 * A sequence number of when event inserted into table
	 */
	private int event_number;
	
	
	
	public Event(String timestamp, int trader_id, String event_type, String order_id, String order_action,
			boolean partial_fill, long price, long volume, String currency, String agg_post, String order_type) {
		super();
		this.timestamp = timestamp;
		this.trader_id = trader_id;
		this.event_type = event_type;
		this.order_id = order_id;
		this.order_action = order_action;
		this.fill_complete = partial_fill;
		this.price = price;
		this.volume = volume;
		this.currency = currency;
		this.agg_post = agg_post;
		this.order_type = order_type;
		
		calculation_sign = "+";
	}


	public Event(String timestamp, int trader_id, String event_type, String order_id, String order_action,
			boolean partial_fill, long price, long volume, String currency, String agg_post, String order_type,
			String orig_timestamp) {
		super();
		this.timestamp = timestamp;
		this.trader_id = trader_id;
		this.event_type = event_type;
		this.order_id = order_id;
		this.order_action = order_action;
		this.fill_complete = partial_fill;
		this.price = price;
		this.volume = volume;
		this.currency = currency;
		this.agg_post = agg_post;
		this.order_type = order_type;
		this.orig_timestamp = orig_timestamp;
		
		calculation_sign = fill_complete ? "-" : "u";
	}

	public Event(String timestamp, int trader_id, String event_type, String order_id, String order_action,
			boolean partial_fill, long price, long volume, String currency, String agg_post, String order_type,
			String orig_timestamp, long orig_price) {
		super();
		this.timestamp = timestamp;
		this.trader_id = trader_id;
		this.event_type = event_type;
		this.order_id = order_id;
		this.order_action = order_action;
		this.fill_complete = partial_fill;
		this.price = price;
		this.volume = volume;
		this.currency = currency;
		this.agg_post = agg_post;
		this.order_type = order_type;
		this.orig_timestamp = orig_timestamp;
		this.orig_price = orig_price;
		
		calculation_sign = "u";
	}

	public Event(String timestamp, int trader_id, String event_type, String order_id, String order_action,
			boolean partial_fill, long price, long volume, String currency, String agg_post, String order_type,
			String orig_timestamp, String calculation_sign) {
		super();
		this.timestamp = timestamp;
		this.trader_id = trader_id;
		this.event_type = event_type;
		this.order_id = order_id;
		this.order_action = order_action;
		this.fill_complete = partial_fill;
		this.price = price;
		this.volume = volume;
		this.currency = currency;
		this.agg_post = agg_post;
		this.order_type = order_type;
		this.orig_timestamp = orig_timestamp;
		this.calculation_sign = calculation_sign;
	}



	
	public Event(String timestamp, int trader_id, String event_type, String order_id, String order_action,
			boolean partial_fill, long price, long volume, String currency, String agg_post) {
		super();
		this.timestamp = timestamp;
		this.trader_id = trader_id;
		this.event_type = event_type;
		this.order_id = order_id;
		this.order_action = order_action;
		this.fill_complete = partial_fill;
		this.price = price;
		this.volume = volume;
		this.currency = currency;
		this.agg_post = agg_post;
		
		calculation_sign = fill_complete ? "-" : "u";
		
	}
	
	public Event(String timestamp, int trader_id, String event_type, String order_id, String order_action,
			 long price, long volume, String currency,  String type) {
		super();
		this.timestamp = timestamp;
		this.trader_id = trader_id;
		this.event_type = event_type;
		this.order_id = order_id;
		this.order_action = order_action;
		this.fill_complete = false;
		this.price = price;
		this.volume = volume;
		this.currency = currency;
		this.agg_post = "Taker";
		this.order_type = type;
		
		calculation_sign = "-";
		
	}	
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getTrader_id() {
		return ""+trader_id;
	}
	public void setTrader_id(int trader_id) {
		this.trader_id = trader_id;
	}
	public String getEvent_type() {
		return event_type;
	}
	public void setEvent_type(String event_type) {
		this.event_type = event_type;
	}
	public String getOrder_id() {
		return order_id;
	}
	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}
	public String getOrder_action() {
		return order_action;
	}
	public void setOrder_action(String order_action) {
		this.order_action = order_action;
	}
	public boolean isPartial_fill() {
		return fill_complete;
	}
	public void setPartial_fill(boolean partial_fill) {
		this.fill_complete = partial_fill;
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
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getAgg_post() {
		return agg_post;
	}
	public void setAgg_post(String agg_post) {
		this.agg_post = agg_post;
	}
	public String getOrder_type() {
		return order_type;
	}
	public void setOrder_type(String order_type) {
		this.order_type = order_type;
	}
	public String getOrig_timestamp() {
		return orig_timestamp;
	}
	public String getCalculation_sign() {
		return calculation_sign;
	}
	public void setCalculation_sign(String calculation_sign) {
		this.calculation_sign = calculation_sign;
	}
	public void setOrig_timestamp(String orig_timestamp) {
		this.orig_timestamp = orig_timestamp;
	}
	public long getOrig_price() {
		return orig_price;
	}


	public void setOrig_price(long orig_price) {
		this.orig_price = orig_price;
	}


	public int getEvent_number() {
		return event_number;
	}


	public void setEvent_number(int event_number) {
		this.event_number = event_number;
	}
	public String getTrader() {
		return trader;
	}


	public Event setTrader(String trader_name) {
		this.trader = trader_name;
		return this;
	}
}
