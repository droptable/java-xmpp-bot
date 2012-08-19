package ws.raidrush.xmpp;

import org.jivesoftware.smack.packet.Message;

abstract public class Command extends Plugin
{
  /**
   * Constructor
   * 
   * @param client
   * @param chat
   */
  public Command(ManagedMultiUserChat chat) { super(chat); }
  
  /**
   * Executes the plugin in a thread
   * 
   * @param msg
   * @param body
   */
  public void execute(final Message msg, final String body)
  {
    new Thread(new Runnable() {
      @Override
      public void run() { perform(msg, body); }
    }).start();
  }
  
  /**
   * Should handle the message
   * 
   * @param msg
   * @param body
   */
  abstract protected void perform(Message msg, String body);
}
