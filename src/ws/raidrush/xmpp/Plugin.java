package ws.raidrush.xmpp;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

@SuppressWarnings("unused")
abstract public class Plugin
{
  protected final Client client;
  protected final MultiUserChat chat;
  
  /**
   * Constructor
   * 
   * @param client
   * @param chat
   */
  public Plugin(Client client, MultiUserChat chat)
  {
    super();
    
    this.client = client;
    this.chat   = chat;
  }
}
