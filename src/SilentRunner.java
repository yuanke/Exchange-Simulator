import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;

import edu.nyu.cims.cims.compfin09.hw2.MessageIterator;
import edu.nyu.cims.cims.compfin09.hw2.interfaces.IMessage;

/**
 * 
 * @author QIAN HU
 * This class will replay the iterator but will not print anything to screen, and hence
 * will ensure that no time is spent on anything but processing.
 * This class will be used for efficiency testing.
 * 
 */
public class SilentRunner {
	private Map<String, Book> askBooks = new HashMap<String, Book>();
	private Map<String, Book> bidBooks = new HashMap<String, Book>();
	private final boolean BUY = true;
	private final boolean SELL = false;
	private Map<String,Order> lmtorders = new HashMap<String,Order>(); // Limit orders ever appeared
	
	/**
	 * This method can process the message coming to the exchange and then perform order or/and update books
	 * @param m IMessage message containing the information of an order
	 */
	public void processMessage(IMessage m){
		Order od = new Order(m);
		double orderQuant = od.getQuantity();
		String smb = od.getSymbol();
		String id = od.getOrderID();
		int sc = od.switchCode;
		Book askBook;
		Book bidBook;
		if(askBooks.containsKey(smb)){
			askBook = askBooks.get(smb);
			bidBook = bidBooks.get(smb);
		}
		else{
			askBook = new Book();
			bidBook = new Book();
			askBooks.put(smb, askBook);
			bidBooks.put(smb, bidBook);
		}
		
		switch (sc){
		case 0:   // Limit Order
			lmtorders.put(id, od);
			if(orderQuant>0){
				limitUpdateBook(bidBook,askBook,od,BUY);	
			}
			else{
				od.setQuantity(-od.getQuantity());
				limitUpdateBook(askBook,bidBook,od,SELL);
			}
			break;
		case 1:  // CxRx Order
			double lmtprice = od.getLimitPrice();
			double origlmtprice = lmtorders.get(id).getLimitPrice();
			smb = lmtorders.get(id).getSymbol();
			// Limit price has changed, delete original order in book and treat as new limit order
			if(lmtprice != origlmtprice){
				if(orderQuant>0){
					bidBook.deleteOrder(id);
					limitUpdateBook(bidBook,askBook,od,BUY);				
				}
				else{
					od.setQuantity(-orderQuant);
					askBook.deleteOrder(id);
					limitUpdateBook(askBook,bidBook,od,SELL);
				}
			}
			// limit price no change, just update corresponding book
			else{
				// BUY->update bid-book
				if(orderQuant>0){
					bidBook.updateOrderQuant(id, lmtprice, orderQuant);
				}
				// SELL->update ask-book
				else{
					od.setQuantity(-orderQuant);
					askBook.updateOrderQuant(id, lmtprice, -orderQuant);
				}				
			}			
			break;
		case 2:  // Market Order
			if(orderQuant>0){
				marketUpdateBook(bidBook, askBook, od, BUY);					
			}
			else{
				od.setQuantity(-od.getQuantity());
				marketUpdateBook(askBook, bidBook, od, SELL);	
			}
			break;
		}
	}
		
	/**
	 * This method deals with the limit order.
	 * @param book1 Book bid-book for BUY order and ask-book for SELL order
	 * @param book2 Book ask-book for BUY order and bid-book for SELL order
	 * @param od Order limit order
	 * @param sign boolean indicate the type of the order: true->BUY, false->SELL
	 */
	private void limitUpdateBook(Book book1, Book book2, Order od, boolean sign){
		String symbol = od.getSymbol();
		boolean canTrade;   // check whether can trade
		if(sign)
			canTrade = canTradeLimitWithAsk(symbol,od.getLimitPrice());
		else
			canTrade = canTradeLimitWithBid(symbol,od.getLimitPrice());		
		// This is match in book2, trade
		if(canTrade){
			NavigableMap<Double,LinkedList<Order>> canTradeSubBook = book2.getCanTradeSubBook(od, sign);
			tradeAndUpdateBook(book1, book2,canTradeSubBook, od, sign);				
		}
		
		// There is no match in book2, just update book1
		else{
			book1.addOrderSP(od, od.getLimitPrice());	
		}
	}

	
	/**
	 * This method deals with the market order.
	 * @param book1 Book bid-book for BUY order and ask-book for SELL order
	 * @param book2 Book ask-book for BUY order and bid-book for SELL order
	 * @param od Order market order
	 * @param sign boolean indicate the type of the order: true->BUY, false->SELL
	 */
	private void marketUpdateBook(Book book1, Book book2, Order od, boolean sign){		
		if(canTradeS(book2)){
			NavigableMap<Double,LinkedList<Order>> symbolSubBook = book2.getSubBookforMarketOrder(sign);
			tradeAndUpdateBook(book1, book2,symbolSubBook, od, sign);
		}
	}
	
	/**
	 * This method can perform trade for an order and update books in the Exchange. 
	 * @param book1 Book bid-book for BUY order and ask-book for SELL order
	 * @param book2 Book ask-book for BUY order and bid-book for SELL order
	 * @param symbolSubBook NavigableMap<Double,LinkedList<Order>> sub-book containing the orders can be traded with order od
	 * @param od Order to be traded
	 * @param sign boolean indicate the type of the order: true->BUY, false->SELL
	 */
	public void tradeAndUpdateBook(Book book1, Book book2, NavigableMap<Double,LinkedList<Order>> symbolSubBook, Order od, boolean sign){
		double aimQuant =  Math.abs(od.getQuantity());
		double reachQuant = 0;
		Iterator<LinkedList<Order>> itorderlist = symbolSubBook.values().iterator();
		while(itorderlist.hasNext()&& reachQuant<aimQuant){
			LinkedList<Order> orderlist = itorderlist.next();
			for(Order match: orderlist){
				if(reachQuant<aimQuant){
					double tradeQuant = Math.min(match.getQuantity(), aimQuant-reachQuant);
					ITrade trade = new ITrade(od, match,match.getPrice(),tradeQuant);
					match.setQuantity(match.getQuantity()-tradeQuant);
					reachQuant += tradeQuant;
				}
			}				
		}
		double marketPriceAfter;
		if(sign)
			marketPriceAfter = book1.getHighPrice();
		else
			marketPriceAfter = book1.getLowPrice();
		book2.deleteEmptyOrder();
		if(aimQuant-reachQuant>0){
			od.setQuantity(aimQuant-reachQuant);
			od.setPrice(marketPriceAfter);
			book1.addOrderSP(od, marketPriceAfter);
		}
	}
	
	/**
	 * Returns true if a limit order can trade with orders in bid book
	 * @param smb String symbol
	 * @param lmtp double limit price of a limit order
	 * @return boolean
	 */
	public boolean canTradeLimitWithBid(String smb, double lmtp){
		if(canTradeS(bidBooks.get(smb))){
			if(bidBooks.get(smb).getHighPrice()>= lmtp)
					return true;
		}
		return false;
	}
	
	/**
	 * Returns true if a limit order can trade with orders in ask book
	 * @param smb String symbol
	 * @param lmtp double limit price of a limit order
	 * @return boolean
	 */
	public boolean canTradeLimitWithAsk(String smb, double lmtp){
		if(canTradeS(askBooks.get(smb))){
			if(askBooks.get(smb).getHighPrice()<= lmtp)
					return true;
		}
		return false;
	}

	
	/**
	 * Returns true when the sub-book for a certain symbol is empty, false otherwise.
	 * @param b Book
	 * @return boolean
	 */
	public boolean canTradeS(Book b){
		return !b.book.isEmpty();
	}
	

	
	/**
	 * main method which simulate the performance of an Exchange given an iterator of test messages.
	 * @param args String[]
	 */
	public static void main(String[] args){
		Iterator<IMessage> im = MessageIterator.getMessageIterator();
		Runner runner = new Runner();
		while(im.hasNext()){
			runner.processMessage(im.next());
		}
	}		
}
