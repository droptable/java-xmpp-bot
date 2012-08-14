import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.Logger;

public class Main
{
  /**
   * entrypoint
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    if (args.length > 0) {
      short level;
      
      try {
        level = Short.parseShort(args[0]);
      } catch (NumberFormatException e) {
        level = 0;
      }
      
      Logger.setLevel(level);
    }
    
    Client client;
    
    try {
      client = new Client("quote2", "geheim-123456", "jabber.ccc.de", 5222);
      client.connect();
    } catch (Exception e) {
      Logger.error(e.getMessage());
      Logger.fatal("can not connect");
      return;
    }
    
    Logger.info("starting main-loop");
    
    client
      // .setReconnect(true)
      // .join("#rr-coding@conference.jabber.ccc.de", "!")
      // .join("#raidrush@conference.jabber.ccc.de", "!", "~rrbot")
      .listen();
  }
}
