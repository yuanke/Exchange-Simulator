import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 
 * @author QIAN HU
 * This can either be an ask-book for sellers or bid-book for buyers.
 * The difference is the order of the prices.
 *
 */
public class Book {
	
	protected TreeMap<Double,LinkedList<Order>> book = new TreeMap<Double,LinkedList<Order>> ();
	
	/**
	 * Constructor of Book
	 */
	public Book(){
		
	}
	
	
	/**
	 * Returns the quantity of an order by orderID
	 * @param lp double limit price
	 * @param id String orderID
	 * @return double
	 */
	public double getOrderQuant(double lp, String id){
		double quant = 0;
		for(Order o:book.get(lp)){
			if(o.getOrderID().equals(id)){
				quant = o.getQuantity();
			}
		}
		return quant;
	}
	
	/**
	 * This method can update the quantity of an order in the book
	 * @param id String orderID
	 * @param lp double limit price
	 * @param newQuant double new quantity
	 */
	public void updateOrderQuant(String id, double lp, double newQuant){
		LinkedList<Order> orderP = book.get(lp);
		LinkedList<Order> orderPCopy = new LinkedList<Order>();
		orderPCopy.addAll(orderP);
		for(Order o: orderP){
			if(o.getOrderID().equals(id)){
				orderPCopy.get(orderP.indexOf(o)).setQuantity(newQuant);
			}
			break;
		}
		orderP.clear();
		orderP.addAll(orderPCopy);
	}
	
	/**
	 * This method will delete an order from a book according the given symbol and orderID
	 * @param id String orderID of the target order
	 */
	public void deleteOrder(String id){
		TreeMap<Double,LinkedList<Order>>  bookCopy= new TreeMap<Double,LinkedList<Order>>();
		bookCopy.putAll(book);
		
		for(double price: book.keySet()){
			LinkedList<Order> order = book.get(price);
			if(order.size()==1){
				if(order.get(0).getOrderID().equals(id)){
					bookCopy.remove(price);
					break;
				}				
			}
			else{
				LinkedList<Order> orderP = new LinkedList<Order>();
				orderP.addAll(order);
				for(Order checkO: order){
					if(checkO.getOrderID().equals(id)){
						orderP.remove(checkO);
						break;
					}                  				
				}
				bookCopy.put(price, orderP);
			}
			
		}
		book.clear();
		book.putAll(bookCopy);
	}
	
	/**
	 * Returns the lowest price in the book which is the prevailing price for an ask-book.
	 * @return double
	 */
	public double getLowPrice(){
		return book.firstKey();
	}
	
	/**
	 * Returns the highest price in the book which is the prevailing price for a bid-book.
	 * @return double
	 */
	public double getHighPrice(){
		return book.lastKey();
	}
	
	/**
	 * Returns the market price for an order (market order).
	 * @param sign boolean indicate the type of the order: true->BUY, false->SELL
	 * @return double
	 */
	public double getMarketPrice(boolean sign){
		if(sign)
			return getLowPrice();
		else
			return getHighPrice();
	}
	
	
	/**
	 * This class can add an order at price p in symbol s in the book.
	 * @param o Order order to be inserted in the book
	 * @param p double price of the order
	 */
	public void addOrderSP(Order o, double p){
		LinkedList<Order> orderP = new LinkedList<Order>();
		// if such price exist, add order at p in s
		if(book.containsKey(p)){	
			orderP.addAll(book.get(p));			
		}
		orderP.add(o);
		book.put(p, orderP);
	}
	
	/**
	 * This method can delete orders whose quantity is 0 at price p of symbol s in the book.
	 */
	public void deleteEmptyOrder(){
		TreeMap<Double,LinkedList<Order>>  bookCopy= new TreeMap<Double,LinkedList<Order>>();
		bookCopy.putAll(book);
		
		for(double price: book.keySet()){
			LinkedList<Order> order = book.get(price);
			if(order.get(order.size()-1).getQuantity()==0){
				bookCopy.remove(price);
			}
			else{
				LinkedList<Order> orderP = new LinkedList<Order>();
				orderP.addAll(order);
				for(Order checkO: order){
					if(checkO.getQuantity()==0){
						orderP.remove(checkO);
						break;
					}                  				
				}
				bookCopy.put(price, orderP);
			}			
		}
		book.clear();
		book.putAll(bookCopy);		
	}

		
	/**
	 * Returns the sub-book for a limit order can be traded
	 * @param o Order limit order to be traded
	 * @param sign boolean indicate the type of the order: true->BUY, false->SELL
	 * @return NavigableMap<Double,LinkedList<Order>>
	 */
	public NavigableMap<Double,LinkedList<Order>> getCanTradeSubBook(Order o, boolean sign){
		NavigableMap<Double,LinkedList<Order>> canTradeSubBookCopy= new TreeMap<Double,LinkedList<Order>>();
		// BUY
		if(sign){
			for(double d: book.keySet()){
				if(d<=o.getLimitPrice()){
					canTradeSubBookCopy.put(d, book.get(d));
				}
			}
			return canTradeSubBookCopy;
		}
		//SELL
		else{
			for(double d: book.descendingKeySet()){
				if(d>=o.getLimitPrice()){
					canTradeSubBookCopy.put(d, book.get(d));
				}
			}			
		}
		return canTradeSubBookCopy.descendingMap();
	}
	
	/**
	 * Returns the sub-book for an market order can be traded in the right order.
	 * @param sign boolean indicate the type of the order: true->BUY, false->SELL
	 * @return NavigableMap<Double, LinkedList<Order>>
	 */
	public NavigableMap<Double, LinkedList<Order>> getSubBookforMarketOrder(boolean sign){
		TreeMap<Double,LinkedList<Order>> subBookforMarketOrder = book;
		// SELL
		if(!sign){
			return subBookforMarketOrder.descendingMap();
		}		
		// BUY
		else
			return subBookforMarketOrder;
	}	
	
	/**
	 * This method can print the top of the book for a certain symbol
	 */
	public void printAskTop(){
		if(book.isEmpty()){
			System.out.println();
		}
		else{
			double count = 0;
			for(Order or: book.get(book.firstKey())){
				count += or.getQuantity();
			}
			System.out.println(count + " @ $"+book.firstKey());
		}
	}
	
	/**
	 * This method can print the top of the book for a certain symbol.
	 */
	public void printBidTop(){
		if(book.isEmpty()){
			System.out.println();
		}
		else{
			double count = 0;
			for(Order or: book.get(book.lastKey())){
				count += or.getQuantity();
			}
			System.out.println(count + " @ $"+book.lastKey());
		}
	}
}
