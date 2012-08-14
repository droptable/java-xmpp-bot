package ws.raidrush.xmpp.handler;

import java.util.Stack;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.Logger;
import ws.raidrush.xmpp.Plugin;

public class ChatQuery implements ChatManagerListener, MessageListener 
{
  // our <master>
  protected Client client;
  
  // chat-messages can not handle filters, since
  // commands are executed without a trigger.
  
  // note: query-commands are global!
  protected Stack<Plugin> plugins;
  
  /**
   * constructor
   * 
   * @param client
   */
  public ChatQuery(Client client)
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
