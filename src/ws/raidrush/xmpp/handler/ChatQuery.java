package ws.raidrush.xmpp.handler;

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
  
  /**
   * constructor
   * 
   * @param client
   */
  public ChatQuery(Client client) { this.client = client; }
  
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
    
    String exec = body.substring(0, body.indexOf(" ")).toLowerCase();
    Plugin plugin = Plugin.get(exec, Plugin.Type.ACTIVE);
    
    if (plugin != null)
      this.execute(plugin, msg, body.substring(body.indexOf(" ") + 1), chat);
  }

  /**
   * returns the client
   * 
   * @return Client
   */
  public Client getClient()
  {
    return this.client;
  }
  
  /**
   * executes a plugin
   * 
   * @param plugin
   * @param msg
   * @param body
   */
  private void execute(final Plugin plugin, final Message msg, final String body, final Chat chat) 
  {
    final ChatQuery self = this;
    
    // start plugin in a new thread
    (new Thread(
        new Runnable() {
          @Override 
          public void run() { plugin.execute(msg, body, chat, self); } 
        }
    )).start();
  }
}
