package trader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.collections.impl.map.mutable.primitive.IntLongHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import eventcenter.Event;
import exchange.core2.core.common.Order;
import exchange.core2.core.common.OrderAction;
import javafx.scene.control.cell.PropertyValueFactory;
import order.AnyOrder;

public class Trader {

	private int uid;
	private String name;
    public ArrayList<Currency> currency;    
	private HashMap<Long, AnyOrder> outstanding_orders;
	

	public ArrayList<Float> historical_pnl;
	public ArrayList<Float> market_impact_bid;
	public ArrayList<Float> market_impact_ask;
	
	/**
	 * trading strategy parameters
	 */
	private float observeness; //inclination to buy on volume imbalance
	private float aggressiveness;  //how close to spread (1 always market order, 0 far away from spread)
	private float risk_adverse;  //how big of bets I make (between 1 and 3)
	private float withdraw_rate; //how often I withdraw
	private float update_rate; //how often I update
	
	/**
	 * Summary stats
	 */
	private int ask_taker;
	private int ask_maker;
	private int bid_taker;
	private int bid_maker;
	
	private int ask_taker_vol;
	private int ask_maker_vol;
	private int bid_taker_vol;
	private int bid_maker_vol;
	
	private int ask_withdraws;
	private int bid_withdraws;
	private int ask_inserts;
	private int bid_inserts;
	private long ask_volume;
	private long bid_volume;	
	private int ask_rejects;
	private int bid_rejects;
	private int ask_updates;
	private int bid_updates;
	
	private long max_ask_insert;
	private long max_bid_insert;
	
	private float price_impact;
	private float ask_vwap_taker;
	private float bid_vwap_taker;
	private float ask_vwap_maker;
	private float bid_vwap_maker;
	private float PnL;
	
	private int current_asks;
	private int current_bids;
	private long current_ask_volume;
	private long current_bid_volume;
	private long bid_price_vol;
	private long ask_price_vol;
	


	private float current_ask_vwap;
	private float current_bid_vwap;

	private float trade_imb;
	private float maker_imb;
	private float bid_insert_imb;
	private float ask_insert_imb;
	private float performance;
	



	private float bid_taker_impact;
	private float ask_taker_impact;
	private float bid_maker_impact;
	private float ask_maker_impact;
	
	
	/*
	 * Taker buy (sell) trades / ask (bid) withdraws 
	 */
	private float ask_trade_withdraw_ratio;
	private float bid_trade_withdraw_ratio;

	private float max_avg_ask_volume;
	private float max_avg_bid_volume;
	
	public Trader() {
		currency = new ArrayList<Currency>();
		outstanding_orders = new HashMap<Long, AnyOrder>();
		historical_pnl = new ArrayList<Float>();
		market_impact_bid = new ArrayList<Float>();
		market_impact_ask = new ArrayList<Float>();
	}
    
	public int getUid() {
		return uid;
	}

	public Trader setUid(int uid) {
		this.uid = uid; 
		return this;
	}

	public ArrayList<Currency> getCurrency() {
		return currency;
	}

	public Trader setCurrency(Currency currency) {
		this.currency.add(currency); 
		return this;
	}


	public Trader setTotal_amount(int _currency, long _total_amount) {
		
		Currency mycurrency = new Currency();
		mycurrency.currency = _currency;		
		mycurrency.total_amount = _total_amount;
		
		currency.add(mycurrency);
		
		return this;
	}

	public class Currency {
		public float getPnl() {
			return pnl;
		}
		public void setPnl(float pnl) {
			this.pnl = pnl;
		}
		public int getCurrency() {
			return currency;
		}
		public void setCurrency(int currency) {
			this.currency = currency;
		}
		public long getTotal_amount() {
			return total_amount;
		}
		public void setTotal_amount(long total_amount) {
			this.total_amount = total_amount;
		}
		private int currency;
		private long total_amount;
		private float pnl;
		
		
	}
	
    public HashMap<Long, AnyOrder> getOutstanding_orders() {
		return outstanding_orders;
	}

	public void setOutstanding_orders(HashMap<Long, AnyOrder> outstanding_orders) {
		this.outstanding_orders = outstanding_orders;
	}
	
	/**
	 * Adds event to trader and updates stats
	 * @param e event
	 */
	public void addEvent(Event e) {
		
		switch (e.getEvent_type()) {
	        case "INSERT": updateInsert(e); break;
	        case "WITHDRAW": updateWithdraw(e); break;
	        case "UPDATE": updateUpdate(e); break;
	        case "TRADE": updateTrade(e); break;
	        case "FILL": updateFill(e); break;
	        case "REJECT": updateRejects(e); break;
		}
	
	}
	
	public void addCurrentOrders(IntObjectHashMap<List<Order>> orders, int pair) {
		
		current_asks = 0;
		current_bids = 0;
		current_ask_volume = 0;
		current_bid_volume = 0;
		bid_price_vol = 0;
		ask_price_vol = 0;
		
		if(orders.get(pair) != null) {
			
			for(Order e : orders.get(pair)) {
				
				if(e.action == OrderAction.ASK) {
					current_asks++;
					current_ask_volume += e.size;
					ask_price_vol += e.size*e.price;
				}
				else if(e.action == OrderAction.BID) {
					current_bids++;
					current_bid_volume += e.size;
					bid_price_vol += e.size*e.price;
				}	
			}
			
			if(current_ask_volume > 0) current_ask_vwap = (float)ask_price_vol/(float)current_ask_volume;
			if(current_bid_volume > 0) current_bid_vwap = (float)bid_price_vol/(float)current_bid_volume;
			
		}
		
		
	}
	
	public void addAccounts(IntLongHashMap accounts) {
		
		for(Currency c : currency) {
			double pnl = ((double)accounts.get(c.getCurrency()) - (double)c.getTotal_amount())/(double)c.getTotal_amount();
			c.pnl = (float)pnl;
		}
		PnL = currency.get(0).pnl;
		historical_pnl.add(PnL);
	}
	
	private void updateInsert(Event e) {
		
		switch (e.getOrder_action()) {
			
			case "ASK": 
				ask_inserts++;
				ask_volume += e.getVolume();
				max_ask_insert = Math.max(max_ask_insert, e.getVolume());
				break;
			case "BID": 
				bid_inserts++;
				bid_volume += e.getVolume();
				max_bid_insert = Math.max(max_bid_insert, e.getVolume());
				break;
				
		}
		
	}
	
	
	
	private void updateWithdraw(Event e) {
		
		switch (e.getOrder_action()) {		
			case "ASK": 
				ask_withdraws++;
				break;
			case "BID": 
				bid_withdraws++;
				break;			
		}		
	}
	
	private void updateUpdate(Event e) {
		
		switch (e.getOrder_action()) {		
		case "ASK": 
			ask_updates++;
			break;
		case "BID": 
			bid_updates++;
			break;			
		}		
	}
	
	private void updateRejects(Event e) {
		
		switch (e.getOrder_action()) {		
		case "ASK": 
			ask_rejects++;
			break;
		case "BID": 
			bid_rejects++;
			break;			
		}		
	}
	
	private void updateTrade(Event e) {
		
		switch (e.getOrder_action()) {		
			
			case "ASK": 
				ask_taker++;
				ask_taker_vol += e.getVolume();
				ask_price_vol += e.getVolume()*e.getPrice();
				ask_vwap_taker = ask_price_vol/ask_taker_vol;
				
				ask_taker_impact = -(float)Math.log((float)e.getPrice()/ask_vwap_taker);
				market_impact_ask.add(ask_taker_impact);
				
				break;
			case "BID": 
				bid_taker++;
				bid_taker_vol += e.getVolume();
				bid_price_vol += e.getVolume()*e.getPrice();
				bid_vwap_taker = bid_price_vol/bid_taker_vol;
				
				bid_taker_impact = (float)Math.log((float)e.getPrice()/bid_vwap_taker);
				market_impact_bid.add(bid_taker_impact);
				
				break;			
		}		
		
	}
	
	private void updateFill(Event e) {
		
		switch (e.getOrder_action()) {		
			
			case "ASK": 
				ask_maker++;
				ask_maker_vol += e.getVolume();
				ask_price_vol += e.getVolume()*e.getPrice();
				ask_vwap_maker = ask_price_vol/ask_maker_vol;
				ask_maker_impact = -(float)Math.log((float)e.getPrice()/ask_vwap_maker);				
				break;
			case "BID": 
				bid_maker++;
				bid_maker_vol += e.getVolume();
				bid_price_vol += e.getVolume()*e.getPrice();
				bid_vwap_maker = bid_price_vol/bid_maker_vol;				
				bid_maker_impact = (float)Math.log((float)e.getPrice()/bid_vwap_maker);
				break;			
		}		
		//System.out.println(e.getTrader_id() + " " + e.getOrder_action() + " " + ask_maker + " " + bid_maker);
	}

	
	
	public int getAsk_taker() {
		return ask_taker;
	}

	public void setAsk_taker(int ask_taker) {
		this.ask_taker = ask_taker;
	}

	public int getAsk_maker() {
		return ask_maker;
	}

	public void setAsk_maker(int ask_maker) {
		this.ask_maker = ask_maker;
	}

	public int getBid_taker() {
		return bid_taker;
	}

	public void setBid_taker(int bid_taker) {
		this.bid_taker = bid_taker;
	}

	public int getBid_maker() {
		return bid_maker;
	}

	public void setBid_maker(int bid_maker) {
		this.bid_maker = bid_maker;
	}

	public int getAsk_taker_vol() {
		return ask_taker_vol;
	}

	public void setAsk_taker_vol(int ask_taker_vol) {
		this.ask_taker_vol = ask_taker_vol;
	}

	public int getAsk_maker_vol() {
		return ask_maker_vol;
	}

	public void setAsk_maker_vol(int ask_maker_vol) {
		this.ask_maker_vol = ask_maker_vol;
	}

	public int getBid_taker_vol() {
		return bid_taker_vol;
	}

	public void setBid_taker_vol(int bid_taker_vol) {
		this.bid_taker_vol = bid_taker_vol;
	}

	public int getBid_maker_vol() {
		return bid_maker_vol;
	}

	public void setBid_maker_vol(int bid_maker_vol) {
		this.bid_maker_vol = bid_maker_vol;
	}

	public int getAsk_withdraws() {
		return ask_withdraws;
	}

	public void setAsk_withdraws(int ask_withdraws) {
		this.ask_withdraws = ask_withdraws;
	}

	public int getBid_withdraws() {
		return bid_withdraws;
	}

	public void setBid_withdraws(int bid_withdraws) {
		this.bid_withdraws = bid_withdraws;
	}

	public int getAsk_inserts() {
		return ask_inserts;
	}

	public void setAsk_inserts(int ask_inserts) {
		this.ask_inserts = ask_inserts;
	}

	public int getBid_inserts() {
		return bid_inserts;
	}

	public void setBid_inserts(int bid_inserts) {
		this.bid_inserts = bid_inserts;
	}

	public long getAsk_volume() {
		return ask_volume;
	}

	public void setAsk_volume(long ask_volume) {
		this.ask_volume = ask_volume;
	}

	public long getBid_volume() {
		return bid_volume;
	}

	public void setBid_volume(long bid_volume) {
		this.bid_volume = bid_volume;
	}

	public int getAsk_rejects() {
		return ask_rejects;
	}

	public void setAsk_rejects(int ask_rejects) {
		this.ask_rejects = ask_rejects;
	}

	public int getBid_rejects() {
		return bid_rejects;
	}

	public void setBid_rejects(int bid_rejects) {
		this.bid_rejects = bid_rejects;
	}

	public int getAsk_updates() {
		return ask_updates;
	}

	public void setAsk_updates(int ask_updates) {
		this.ask_updates = ask_updates;
	}

	public int getBid_updates() {
		return bid_updates;
	}

	public void setBid_updates(int bid_updates) {
		this.bid_updates = bid_updates;
	}

	public long getMax_ask_insert() {
		return max_ask_insert;
	}

	public void setMax_ask_insert(long max_ask_insert) {
		this.max_ask_insert = max_ask_insert;
	}

	public long getMax_bid_insert() {
		return max_bid_insert;
	}

	public void setMax_bid_insert(long max_bid_insert) {
		this.max_bid_insert = max_bid_insert;
	}

	public float getPrice_impact() {
		return price_impact;
	}

	public void setPrice_impact(float price_impact) {
		this.price_impact = price_impact;
	}

	public float getAsk_vwap_taker() {
		return ask_vwap_taker;
	}

	public void setAsk_vwap_taker(float ask_vwap_taker) {
		this.ask_vwap_taker = ask_vwap_taker;
	}

	public float getBid_vwap_taker() {
		return bid_vwap_taker;
	}

	public void setBid_vwap_taker(float bid_vwap_taker) {
		this.bid_vwap_taker = bid_vwap_taker;
	}

	public float getAsk_vwap_maker() {
		return ask_vwap_maker;
	}

	public void setAsk_vwap_maker(float ask_vwap_maker) {
		this.ask_vwap_maker = ask_vwap_maker;
	}

	public float getBid_vwap_maker() {
		return bid_vwap_maker;
	}

	public void setBid_vwap_maker(float bid_vwap_maker) {
		this.bid_vwap_maker = bid_vwap_maker;
	}

	public float getPnL() {
		return PnL;
	}

	public void setPnL(float pnL) {
		PnL = pnL;
	}

	public int getCurrent_asks() {
		return current_asks;
	}

	public void setCurrent_asks(int current_asks) {
		this.current_asks = current_asks;
	}

	public int getCurrent_bids() {
		return current_bids;
	}

	public void setCurrent_bids(int current_bids) {
		this.current_bids = current_bids;
	}

	public long getCurrent_ask_volume() {
		return current_ask_volume;
	}

	public void setCurrent_ask_volume(long current_ask_volume) {
		this.current_ask_volume = current_ask_volume;
	}

	public long getCurrent_bid_volume() {
		return current_bid_volume;
	}

	public void setCurrent_bid_volume(long current_bid_volume) {
		this.current_bid_volume = current_bid_volume;
	}

	public long getBid_price_vol() {
		return bid_price_vol;
	}

	public void setBid_price_vol(long bid_price_vol) {
		this.bid_price_vol = bid_price_vol;
	}

	public long getAsk_price_vol() {
		return ask_price_vol;
	}

	public void setAsk_price_vol(long ask_price_vol) {
		this.ask_price_vol = ask_price_vol;
	}

	public void setCurrency(ArrayList<Currency> currency) {
		this.currency = currency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	


	
	public float getTrade_imb() {
		if(bid_taker + ask_taker == 0) return 0;
		return (float)bid_taker/((float)bid_taker + (float)ask_taker);
	}

	public void setTrade_imb(float trade_imb) {
		this.trade_imb = trade_imb;
	}

	public float getMaker_imb() {
		if(bid_maker + ask_maker == 0) return 0;
		return (float)bid_maker/((float)bid_maker + (float)ask_maker);
	}

	public void setMaker_imb(float maker_imb) {
		this.maker_imb = maker_imb;
	}

	public float getBid_insert_imb() {
		 
		 if(bid_inserts == 0) bid_insert_imb = 0f;
		 else bid_insert_imb = (float)bid_maker/(float)bid_inserts;
		
		 return bid_insert_imb;
	}

	public void setBid_insert_imb(float bid_insert_imb) {
		this.bid_insert_imb = bid_insert_imb;
	}

	public float getAsk_insert_imb() {
		if(ask_inserts == 0) return 0;
		return (float)ask_maker/(float)ask_inserts;
	}

	public void setAsk_insert_imb(float ask_insert_imb) {
		this.ask_insert_imb = ask_insert_imb;
	}

	public float getPerformance() {
		
		performance = (float)bid_taker/((float)bid_taker + (float)bid_maker);
		
		return performance;
	}

	public void setPerformance(float performance) {
		this.performance = performance;
	}
	
	public float getCurrent_ask_vwap() {
		return current_ask_vwap;
	}

	public void setCurrent_ask_vwap(float current_ask_vwap) {
		this.current_ask_vwap = current_ask_vwap;
	}

	public float getCurrent_bid_vwap() {
		return current_bid_vwap;
	}

	public void setCurrent_bid_vwap(float current_bid_vwap) {
		this.current_bid_vwap = current_bid_vwap;
	}
	
	public float getAsk_trade_withdraw_ratio() {
		if(ask_taker == 0) return 0;
		return (float)bid_withdraws/(float)ask_taker;
	}

	public void setAsk_trade_withdraw_ratio(float ask_trade_withdraw_ratio) {
		this.ask_trade_withdraw_ratio = ask_trade_withdraw_ratio;
	}

	public float getBid_trade_withdraw_ratio() {
		if(bid_taker == 0) return 0;
		return (float)ask_withdraws/(float)bid_taker;
	}

	public void setBid_trade_withdraw_ratio(float bid_trade_withdraw_ratio) {
		this.bid_trade_withdraw_ratio = bid_trade_withdraw_ratio;
	}
	
	public float getMax_avg_ask_volume() {
		if(ask_volume == 0) return 0;
		return (float)max_ask_insert/((float)ask_volume/(float)ask_inserts);
	}

	public void setMax_avg_ask_volume(float max_avg_ask_volume) {
		this.max_avg_ask_volume = max_avg_ask_volume;
	}

	public float getMax_avg_bid_volume() {
		if(bid_volume == 0) return 0;
		return (float)max_bid_insert/((float)bid_volume/(float)bid_inserts);
	}

	public void setMax_avg_bid_volume(float max_avg_bid_volume) {
		this.max_avg_bid_volume = max_avg_bid_volume;
	}
	
	public float getBid_taker_impact() {
		return 100f*bid_taker_impact;
	}

	public void setBid_taker_impact(float bid_taker_impact) {
		this.bid_taker_impact = bid_taker_impact;
	}

	public float getAsk_taker_impact() {
		return 100f*ask_taker_impact;
	}

	public void setAsk_taker_impact(float ask_taker_impact) {
		this.ask_taker_impact = ask_taker_impact;
	}

	public float getBid_maker_impact() {
		return 100f*bid_maker_impact;
	}

	public void setBid_maker_impact(float bid_maker_impact) {
		this.bid_maker_impact = bid_maker_impact;
	}

	public float getAsk_maker_impact() {
		return 100f*ask_maker_impact;
	}

	public void setAsk_maker_impact(float ask_maker_impact) {
		this.ask_maker_impact = ask_maker_impact;
	}
	
	public float[] getStats() {
		
		
		float[] stat = new float[15];
		
		stat[0] = getAsk_trade_withdraw_ratio();
		stat[1] = getBid_trade_withdraw_ratio();
		stat[2] = getMax_avg_ask_volume();
		stat[3] = getMax_avg_bid_volume();
		stat[4] = getTrade_imb();
		stat[5] = getMaker_imb();
		stat[6] = getAsk_insert_imb();
		stat[7] = getBid_insert_imb();
		stat[8] = getAsk_taker_impact();
		stat[9] = getBid_taker_impact();
		stat[10] = getAsk_maker_impact();
		stat[11] = getBid_maker_impact();
		stat[12] = getCurrent_ask_volume();
		stat[13] = getCurrent_bid_volume();
		stat[14] = getPnL();
		
		return stat;
	}

	public float getUpdate_rate() {
		return update_rate;
	}

	public void setUpdate_rate(float update_rate) {
		this.update_rate = update_rate;
	}

	public float getWithdraw_rate() {
		return withdraw_rate;
	}

	public void setWithdraw_rate(float withdraw_rate) {
		this.withdraw_rate = withdraw_rate;
	}

	public float getRisk_adverse() {
		return risk_adverse;
	}

	public void setRisk_adverse(float risk_adverse) {
		this.risk_adverse = risk_adverse;
	}

	public float getAggressiveness() {
		return aggressiveness;
	}

	public void setAggressiveness(float aggressiveness) {
		this.aggressiveness = aggressiveness;
	}

	public float getObserveness() {
		return observeness;
	}

	public void setObserveness(float observeness) {
		this.observeness = observeness;
	}
	
	public ArrayList<Float> getHistorical_pnl() {
		return historical_pnl;
	}

	public void setHistorical_pnl(ArrayList<Float> historical_pnl) {
		this.historical_pnl = historical_pnl;
	}

	public ArrayList<Float> getMarket_impact_bid() {
		return market_impact_bid;
	}

	public void setMarket_impact_bid(ArrayList<Float> market_impact_bid) {
		this.market_impact_bid = market_impact_bid;
	}

	public ArrayList<Float> getMarket_impact_ask() {
		return market_impact_ask;
	}

	public void setMarket_impact_ask(ArrayList<Float> market_impact_ask) {
		this.market_impact_ask = market_impact_ask;
	}
}

