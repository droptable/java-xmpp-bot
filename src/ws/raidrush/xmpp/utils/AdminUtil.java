package ws.raidrush.xmpp.utils;

import java.util.Iterator;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;

public class AdminUtil
{
  public static boolean isModeratorNick(MultiUserChat muc, String nick)
  {
    Occupant occ = muc.getOccupant(nick);
    
    if (occ == null)
      return false;
    
    String role = occ.getRole();
    return role.equals("moderator") || 
        role.equals("admin") || 
        role.equals("owner");
  }
  
  public static boolean isModeratorJid(MultiUserChat muc, String jid)
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
