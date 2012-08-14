package ws.raidrush.xmpp.plugin;

import java.util.Iterator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;

import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.Plugin;
import ws.raidrush.xmpp.handler.ChatQuery;
import ws.raidrush.xmpp.handler.ChatRoom;

/**
 * admin commands
 * add / remove plugins for a channel and stuff
 *
 */
public class Admin extends Plugin
{
  @Override
  public void execute(Message msg, String body, MultiUserChat chat, ChatRoom room) { /* void */ }

  @Override
  public void execute(Message msg, String body, Chat chat, ChatQuery query) { /* void */ }

  @Override
  public Type getType() { return Type.ACTIVE; }
  
  /**
   * checks if a user is moderator/admin/owner in a muc
   * 
   * @param client
   * @param jid
   * @param room
   * @param nick
   * @return boolean
   */
  protected boolean isModerator(Client client, String jid, String room, String nick)
  {
    MultiUserChat muc = new MultiUserChat(client.getXMPP(), room);
    
    if (nick == null)
      nick = client.getUser();
    
    try {
      muc.join(nick);
    } catch (XMPPException e) {
      return false;
    }
    
    try {
      Thread.sleep(20000);
    } catch (InterruptedException e) {
      return false;
    }
    
    boolean okay = isModerator(muc, jid);
    
    muc.leave();
    return okay;
  }
  
  /**
   * checks if a user is moderator/admin/owner in a muc
   * 
   * @param muc
   * @param jid
   * @return
   */
  protected boolean isModerator(MultiUserChat muc, String jid)
  {
    Iterator<String> users = muc.getOccupants();
    boolean okay = false;
    
    while (users.hasNext()) {
      String roomNick = users.next();   
      
      Occupant occ = muc.getOccupant(roomNick);
      String roomJid = occ.getJid();
      
      int resIndex = roomJid.indexOf("/");
      
      if (resIndex > -1)
        roomJid = roomJid.substring(0, resIndex);
      
      if (roomJid.equals(jid)) {
        String roomRole = occ.getRole();
        
        if (roomRole.equals("moderator") ||
            roomRole.equals("admin") ||
            roomRole.equals("owner"))
          okay = true;
        
        break;
      }
    }
    
    return okay;
  }
}
