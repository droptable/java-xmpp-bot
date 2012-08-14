package ws.raidrush.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;

public class RoomChatHandler implements PacketListener 
{
  // our <master> client
  protected Client client;
  
  // the MutliUserChat
  protected MultiUserChat room;
  
  // our nick in that room
  protected String nick;
  
  // the trigger we should handle
  protected String trigger;
  
  public RoomChatHandler(Client client, MultiUserChat room, String nick, String trigger) 
  {
    this.client  = client;
    this.room    = room;
    this.nick    = nick;
    this.trigger = trigger;
    
    Logger.info("handler for room " + this.room.getRoom() + " created");
  }

  @Override
  public void processPacket(Packet pack) 
  {
    if (pack instanceof Message) {
      Message msg = (Message) pack;
      Logger.info(msg.getFrom() + " wrote a message in " + this.room.getRoom() + ": " + msg.getBody());
    }
    
  }

  public String getNick() 
  {
    return this.nick;
  }
  
  public String getRoom()
  {
    return this.room.getRoom();
  }
  
  public MultiUserChat getChat()
  {
    return this.room;
  }
  
  /**
   * returns the jid for a user inside the multi-user-chat
   * 
   * @param nick
   * @return
   */
  public synchronized String getUserJid(String nick)
  {
    if (nick.indexOf("/") == -1)
      nick = this.getRoom() + "/" + nick;
    
    Occupant user = this.room.getOccupant(nick);
    
    if (user == null)
      return null;
    
    return user.getJid();
  }
}
