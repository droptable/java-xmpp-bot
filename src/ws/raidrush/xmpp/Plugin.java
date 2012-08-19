package ws.raidrush.xmpp;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

@SuppressWarnings("unused")
abstract public class Plugin
{
  protected final ManagedMultiUserChat chat;
  
  /**
   * Constructor
   * 
   * @param client
   * @param chat
   */
  public Plugin(ManagedMultiUserChat chat)
  {
    super();
    
    this.chat = chat;
  }
  
  /**
   * Sends a message back to chat
   * 
   * @param msg
   */
  protected void respond(String msg)
  {
    try {
      chat.sendMessage(msg);
    } catch (XMPPException e) {
      Logger.getRootLogger().info("Unable to respond in chat: " + chat.getRoom(), e);
    }
  }
}
