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
      client = new Client(...);
      client.connect();
    } catch (Exception e) {
      Logger.error(e.getMessage());
      Logger.fatal("can not connect");
      return;
    }
    
    Logger.info("starting main-loop");
    
    client
      .join("#rr-coding@conference.jabber.ccc.de", "!")
      .join("#raidrush@conference.jabber.ccc.de", "!")
      .listen();
  }
}
