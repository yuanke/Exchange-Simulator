
import edu.nyu.cims.cims.compfin09.hw2.interfaces.ICxRxMessage;
import edu.nyu.cims.cims.compfin09.hw2.interfaces.ILimitOrder;
import edu.nyu.cims.cims.compfin09.hw2.interfaces.IMarketOrder;
import edu.nyu.cims.cims.compfin09.hw2.interfaces.IMessage;

/**
 * 
 * @author QIAN HU
 * MarketOrder: executed at the prevailing price at that moment.
 *              OrderID, Quantity, Symbol
 * LimitOrder: executed at a certain price or better.
 *             OrderID, Quantity, Symbol, limit price
 * CxRxOrder: cancel and replace an order, update limit price and quantity
 * 
 */
public class Order {
	private String orderID;
	private String symbol;
	private double quantity;	
	private double limitPrice;
	private String orderType;
	private double price;
	public int switchCode;
	
/**
 * Constructor of Order.
 * Generate an order from an given message.
 * Three types of orders: LimitOrder, CxRxOrder and MarketOrder.
 * @param m IMessge a message containing information about an order.
 */
	public Order(IMessage m){
		if(m instanceof ILimitOrder){
			ILimitOrder lo = (ILimitOrder) m;
			switchCode = 0;
			orderID = lo.getOrderID();
			symbol = lo.getSymbol();
			quantity = lo.getQuantity();
			limitPrice = lo.getLimitPrice();
			price = limitPrice;
			orderType = "LimitOrder";
		}
		else if(m instanceof ICxRxMessage){
			ICxRxMessage cr = (ICxRxMessage) m;
			switchCode = 1;
			orderID = cr.getOrderID();
			symbol = cr.getSymbol();
			quantity = cr.getNewQuantity();
			limitPrice = cr.getNewLimitPrice();
			price = limitPrice;
			orderType = "CxRxOrder";
		}
		else if(m instanceof IMarketOrder){
			IMarketOrder mo = (IMarketOrder) m;
			switchCode = 2;
			orderID = mo.getOrderID();
			symbol = mo.getSymbol();
			quantity = mo.getQuantity();
			orderType = "MarketOrder";
			if(quantity>0)
				limitPrice = Double.MAX_VALUE;
			else
				limitPrice = Double.MIN_VALUE;
		}
		else
			return;	
		
	}
	
	/**
	 * This method can set the price of an order.
	 * @param p double price for an order
	 */
	public void setPrice(double p){
		price = p;
	}
	
	/**
	 * Returns the price of an order
	 * @return double
	 */
	public double getPrice(){
		return price;
	}
	
	/**
	 * Returns the orderID of an order
	 * @return String
	 */
	public String getOrderID(){
		return orderID;		
	}
	
	/**
	 * Returns the symbol of an order
	 * @return String
	 */
	public String getSymbol(){
		return symbol;
	}
	
	/**
	 * Returns the quantity of an order.
	 * @return double
	 */
	public double getQuantity(){
		return quantity;
	}
	
	/**
	 * Returns the limitPrice of an order
	 * @return double
	 */
	public double getLimitPrice(){
		return limitPrice;
	}
	
	/**
	 * Returns the orderType of an order
	 * @return String
	 */
	public String getOrderType(){
		return orderType;
	}
	
	/**
	 * This method can set the quantity of an order.
	 * @param q double quantity
	 */
	public void setQuantity(double q){
		quantity = q;
	}
	
	@Override
	public String toString(){
		return orderID + ": "+ quantity;
	}
}
