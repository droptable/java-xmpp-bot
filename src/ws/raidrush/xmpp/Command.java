package ws.raidrush.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

abstract public class Command 
{
  public enum Type { 
    CHAT, 
    ROOM 
  }
  
  protected Type type;
  
  // private message chat
  protected Chat chat;
  
  // public(?) multi user chat
  protected MultiUserChat room;
  
  /**
   * constructor for private Chats
   * 
   * @param chat
   */
  public Command(Chat chat)
  {
    this.type = Type.CHAT;
    this.chat = chat;
  }
  
  /**
   * constructor for MultiUserChats
   * 
   * @param chat
   */
  public Command(MultiUserChat chat)
  {
    this.type = Type.ROOM;
    this.room = chat;
  }
  
  /**
   * should execute the command
   * 
   * @param args
   */
  abstract public void execute(String[] args);
  
  /**
   * use this method to send the result of `execute` back to its origin
   * 
   * @param msg
   */
  public void respond(String text)
  {
    Message msg = new Message(text);
    
    try {
      if (this.type == Type.CHAT)
        this.chat.sendMessage(msg);
      else
        this.room.sendMessage(msg);
    } catch (XMPPException e) {
      String origin = null;
      
      if (this.type == Type.CHAT)
        origin = this.chat.getParticipant();
      else
        origin = this.room.getRoom();
      
      Logger.warn("unable to send message to " + origin);
    }
  }
}
