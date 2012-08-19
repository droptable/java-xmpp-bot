package ws.raidrush.xmpp.plugins;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import ws.raidrush.xmpp.Command;
import ws.raidrush.xmpp.ManagedMultiUserChat;

public class Echo extends Command
{
  public Echo(ManagedMultiUserChat chat) { super(chat); }

  @Override
  protected void perform(Message msg, String body)
  {
    // testing
    
    try {
      chat.sendMessage(body);
    } catch (XMPPException e) {
      Logger.getRootLogger().error("Error while sending message", e);
    }
  }
  
}
