package ws.raidrush.xmpp.plugin;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.Logger;
import ws.raidrush.xmpp.Plugin;
import ws.raidrush.xmpp.handler.ChatRoom;
import ws.raidrush.xmpp.handler.ChatQuery;

public class Echo extends Plugin 
{  
  /**
   * constructor
   * 
   */
  public Echo() { }

  @Override
  public Type getType() { return Type.ACTIVE; }
  
  @Override
  public String getMeta() { return "Simple ECHO Command - Written by droptable, Version: 1.0";  }
  
  @Override
  public void execute(Message p, String m, MultiUserChat chat, ChatRoom r) 
  {
    Logger.info("echo plugin executed in chat-room");
    
    try {
      chat.sendMessage(m);
    } catch (XMPPException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void execute(Message p, String m, Chat chat, ChatQuery q) 
  {
    Logger.info("echo plugin executed in chat-query");
    
    try {
      chat.sendMessage(m);
    } catch (XMPPException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }  
}
