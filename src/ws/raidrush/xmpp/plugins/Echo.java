package ws.raidrush.xmpp.plugins;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.Command;

public class Echo extends Command
{
  public Echo(Client client, MultiUserChat chat) { super(client, chat); }

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
