
/**
 * 
 * @author QIAN HU
 * This class describes two orders trade at a certain price and quantity.
 *
 */
public class ITrade {
	String orderID1;
	String orderID2;
	double price;
	double quantity;
	
	/**
	 * Constructor of ITrade.
	 * @param o1 Order order_1
	 * @param o2 Order order_2
	 * @param p double the price at which the trade is performed
	 * @param q double the quantity in the trade
	 */
	public ITrade(Order o1, Order o2, double p, double q){	
		orderID1 = o1.getOrderID();
		orderID2 = o2.getOrderID();
		price = p;
		quantity = q;	
	}
	
	/**
	 * Returns the orderID of one order involved in the trade.
	 * @return String
	 */
	public String getID1(){
		return orderID1;
	}
	
	/**
	 * Returns the orderID of the other order involved in the trade.
	 * @return String
	 */
	public String getID2(){
		return orderID2;
	}

	/**
	 * This method will print the information of a trade.
	 */
	public void PrintTrade(){
		System.out.println("Order " + getID1() + " traded with order " + getID2() + "\n\t");
	}
	
	@Override
	public String toString(){
		return "Order " + getID1() + " traded with order " + getID2();
	}
}
