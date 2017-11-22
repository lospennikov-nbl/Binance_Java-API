## Unofficial client for working with [Binance](https://www.binance.com) cryptocurrency exchange API

### Project structure
* Main module contains classes to work with api (```BinanceApi``` and ```BinanceApiPublic```)
* MessageGenerator module contains generator for Java classes represented by server and client messages.
* MessagesConfig module contains xml files which describe all exchange messages.

## How to use
To use this library in your code download this library, run ```mvn clean install``` in the source folder, then simply add dependency to your ```pom.xml``` file
```
<dependency>
      <groupId>com.binance.api</groupId>
      <artifactId>api-client</artifactId>
      <version>1.0-SNAPSHOT</version>
</dependency>
```  
### Public api
Public api represents all actions users can do without registering. In other words, it allows you to work with market data. For available methods look at ```BinanceApiPublic.java``` class and in [api documentation](https://www.binance.com/restapipub.html#user-content-market-data-endpoints)
(Sections MarketData endpoints and websocket api (excluding user data))
#### Examples
Firstly, you need to create public api client.
```
BinanceApiPublic apiPublic = new BinanceApiPublic();
```
Now you can send market data messages to exchange. E.g. to get current time
```
com.binance.api.beans.Time time = apiPublic.getTime();
System.out.println(time.getServerTime());
```
This will print current exchange time in unix format.
To send more complex messages you need to create message object, passing mandatory parameters to the constructor. E.g.
```
DepthMessage message = new DepthMessage("ETHBTC");
Depth depth = apiPublic.getDepth(message);
System.out.println(depth);
```
Will print current depth if ETH/BTC trading pair.  
You can also set optional message parameters with corresponding setter.
Thus, following code
```
DepthMessage message = new DepthMessage("ETHBTC");
message.setLimit(5);
Depth depth = apiPublic.getDepth(message);
System.out.println(depth);
```
Will print only 5 depth messages.  
You can also work with resulted beans, calling getter of some property. Thus,
```
System.out.println(depth.getAsks());
```
Will print list of bids you received in depth message.   
### Signed api
If you want to send orders or working with your account, you need to create extended api class called ```BinanceApi```
To create an instance of this class you will need your private key and api key. You can get it [Binance account settings](https://www.binance.com/userCenter/myAccount.html), clicking "API Settings" button.
From the user's point of view working with signed api is the same as working with public one.
All that you need to do is create client class
```
BinanceApi api = new BinanceApi(privateKey, apiKey);
```
And send messages the same way you did it with public api. E.g.
```
System.out.println(api.getAccountInfo());
```
This code will print your account info.  
__You don't need to create separate clients for public and signed api. BinanceApi class can work with both and BinanceApiPublic with only public api.__
### Websocket
You can not only send and receive messages, but listen to socket for all information you are interested in. Binance provides 4 different urls: Depth, Candelsticks, Trades and User Data. Working with User Data are provided by signed api, and you can work with other 3 of it with BinanceApiPublic class.  
To work with websocket you need to create an instance of class extends socket of corresponding endpoint, then start session. All incoming messages will be handled in method, which your class overrides. E.g.
```
DepthSocket socket = new DepthSocket() {
  public void onMessage(DepthEndpoint depthEndpoint) {
    System.out.println(depthEndpoint);
  }
};
Session s = apiPublic.getDepthSession("ETHBTC", socket);
try {
  Thread.sleep(1500000);
} catch (InterruptedException e) {
}
s.close();
```
Will connect to depth endpoint and will listen to all events in depth feed of ETH/BTC trading pair. DepthSocket object will handle all incoming messages (in our case just print it)  
## Messages
List of all messages which exchange sends and can receive can be found in [official documentation page](https://www.binance.com/restapipub.html#grip-content). However, you can just look at xml files, describe all exchange messages.
### XML configuration
There are 3 different types of classes generated from XML files.
#### Enums
Enumerations that can be found in various messages. Each message have name and values it can take.
### Client-side messages
This messages are located in client folder. Each message have 0 or more mandatory parameters and 0 or more optional parameters. All parameters set by object will be sent as part of final query.
### Server-side messages
This messages are located in server folder. Each message client class receives in json format it maps to corresponding bean.
