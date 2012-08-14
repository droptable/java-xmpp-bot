package ws.raidrush.xmpp.plugin;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.handler.ChatQuery;
import ws.raidrush.xmpp.handler.ChatRoom;

public class Leave extends Admin
{
  public Leave() {}
  
  @Override
  public void execute(Message msg, String body, Chat chat, ChatQuery query) 
  {
    String[] args = parseArguments(body);
    
    if (args.length != 1) {
      respond(chat, "Fehlende Parameter: leave <room>");
      return;
    }
    
    // setup
    String jid = getJidFromContact(msg.getFrom(), chat, query);
    if (jid == null) return;
    
    Client client = query.getClient();
    ChatRoom room = client.getRoom(args[0]);
    
    if (room == null) {
      respond(chat, "Ich bin gar nicht in diesem Chatraum!");
      return;
    }
    
    MultiUserChat muc = room.getChat();
    
    if (!isModerator(muc, jid)) {
      respond(chat, "Du bist kein Moderator in diesem Chatraum!");
      return;
    }
    
    room.leave();    
    respond(chat, "Chatraum verlassen.");
  }  
}
