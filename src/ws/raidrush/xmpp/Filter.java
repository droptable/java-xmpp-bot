package ws.raidrush.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

abstract public class Filter extends Plugin implements PacketListener
{
  protected boolean isAttached = false;
  
  /**
   * Constructor 
   * 
   * @param client
   * @param chat
   */
  public Filter(Client client, MultiUserChat chat) { super(client, chat); }
  
  /**
   * Adds itself as MessageListener
   * 
   */
  public void attach() 
  { 
    if (isAttached == true)
      return;
    
    chat.addMessageListener(this); 
    isAttached = true;
  }
  
  /**
   * Removes itself as MessageListener
   * 
   */
  public void remove() 
  { 
    if (isAttached == false)
      return;
    
    chat.removeMessageListener(this); 
    isAttached = false;
  }
  
  @Override
  public void processPacket(Packet packet)
  {
    if (!(packet instanceof Message))
      return;
    
    perform((Message) packet);
  }
  
  /**
   * Should handle the message
   * 
   * @param msg
   */
  abstract protected void perform(Message msg);
}
