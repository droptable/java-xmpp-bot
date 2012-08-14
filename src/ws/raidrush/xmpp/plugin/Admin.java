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
  public void execute(Message p, String m, MultiUserChat c, ChatRoom r) 
  {
    // this command is only available via query
  }

  @Override
  public void execute(Message msg, String body, Chat chat, ChatQuery query) 
  { 
    String[] args = parseArguments(body);
    
    if (args.length < 2) {
      respond(chat, "Fehlende Parameter: admin <room> <exec> [ <...> ]");
      return;
    }
    
    // setup
    String jid = getJidFromContact(msg.getFrom(), chat, query);
    if (jid == null) return;
    
    String room = args[0], exec = args[1];
    Client client = query.getClient();
    
    if (room.equals("-") && exec.equals("quit")) {
      if (jid.equals("murdoc@jabber.ccc.de")) {
        client.disconnect("Shutdown...");
        return;
      }
      
      respond(chat, "Fehlende Parameter: admin <room> <exec> [ <...> ]");
      return;
    }
    
    // channel commands
    ChatRoom croom = client.getRoom(room);
    
    if (croom == null) {
      respond(chat, "Ich bin gar nicht in diesem Chatraum!");
      return;
    }
    
    if (isModerator(croom.getChat(), jid) == false) {
      respond(chat, "Du bist nicht Moderator in diesem Chatraum!");
      return;
    }
    
    // plugin enable
    if (exec.equals("enable")) {
      if (args.length != 3) {
        respond(chat, "Du musst ein Plugin angeben");
        return;
      }
      
      if (croom.enable(args[2]) == true) {
        respond(chat, "Plugin erfolgreich aktiviert!");
        return;
      }
      
      respond(chat, "Plugin nicht verfügbar!");
      return;
    }
    
    // plugin disable
    if (exec.equals("disable")) {
      if (args.length != 3) {
        respond(chat, "Du musst ein Plugin angeben");
        return;
      }
      
      if (croom.disable(args[2]) == true) {
        respond(chat, "Plugin erfolgreich deaktiviert!");
        return;
      }
      
      respond(chat, "Plugin nicht verfügbar!");
      return;
    }
  }

  @Override
  public Type getType() { return Type.ACTIVE; }
  
  protected String getJidFromContact(String nick, Chat chat, ChatQuery query)
  {
    String jid = query.getClient().getUserJid(nick);
    
    if (jid == null) {
      respond(chat, "Hallo, danke für deine Nachricht! Aber... für diese Anfrage "
          + "akzeptiere ich nur private Nachrichten von Kontakten in meiner Kontaktliste. "
          + "Bitte füge mich als Kontakt hinzu (falls nicht schon passiert) und schreibe mich "
          + "direkt an! Und keine Angst, ich nehme jede Kontaktanfrage an ;-)");
      
      return null;
    }
    
    return jid;
  }
  
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
