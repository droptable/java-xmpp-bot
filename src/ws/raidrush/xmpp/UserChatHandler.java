package ws.raidrush.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

public class UserChatHandler implements ChatManagerListener, MessageListener 
{
  // our <master>
  protected Client client;
  
  /**
   * constructor
   * 
   * @param client
   */
  public UserChatHandler(Client client)
  {
    this.client = client;
  }
  
  @Override
  public void chatCreated(Chat chat, boolean local) 
  {
    // just add this class as listener
    chat.addMessageListener(this);
  }

  @Override
  public void processMessage(Chat chat, Message msg) 
  {
    String body = msg.getBody();
    
    if (body == null)
      return;
    
    String user = chat.getParticipant();    
    Logger.info("got a message from: " + user + ": " + body);
  }

}
