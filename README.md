# DEXOrderbookTemplate

DEX is a simple financial securities exchange that allows for a limit orderbook (LOB) style centralized exchange. 
THere are several packages written that will enable a complete simulation of an LOB. 

The files we will consider in this exercise are 
LimitAndMarketOrderExample.java, and the DEXEventHandler

The exercise consists of three parts

### Instructions
1. Build the project in your favorite IDE using the build.gradle file found in the repo. 
2. Write a LIMIT ORDERBOOK data structure in the class DEXEventHandler that automatically maps outstanding orders to a price ranking in both the bid and the ask anytime there is a new/canceled/modified/rejected order. 
You can use any data structure in java, but it should be as fast as possible and contain a print function that prints all outstanding orders at every price rank in order of descending price. A place holder with 
```java
/**
* Orderbook logic
*/
```
has been placed in the function DEXEventHandler to guide you on where adjustements to the orderbook should be made. The outstanding orders at any given time are found in 
```java
private ConcurrentHashMap<Long, AnyOrder> outstnd_orders;
```
3. Write a function that returns the price of the best bid and the best ask from the orderbook build in step 2. 
4. In the main function LimitAndMarketOrderExample.java, a market has been created with an initial set of limit orders on the orderbook. 
	a. Place 3 buy market orders each with 10, 12, 15 in volume size (you can do this buy placing a limit order well above the best ask. 
	b. Place a limit order on the ask two price levels above the best ask with a volume of 20
	c. Print the final orderbook to System.out

This concludes the exercise. Of course, there are many ways to build the real-time order book, some are better/more efficient in time/space than others.  
We would ultimately like to see how creative your solution is. 	 