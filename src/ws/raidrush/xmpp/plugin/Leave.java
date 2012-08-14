package ws.raidrush.xmpp.plugin;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.handler.ChatQuery;
import ws.raidrush.xmpp.handler.ChatRoom;

public class Leave extends Join
{
  public Leave() {}
  
  @Override
  public void execute(Message msg, String body, Chat chat, ChatQuery query) 
  {
    String[] args = parseArguments(body);
    
    if (args.length != 1) {
      respond(chat, "missing arguments: leave <room>");
      return;
    }
    
    // setup
    String nick = msg.getFrom(),
           jid  = query.getClient().getUserJid(nick);
    
    Client client = query.getClient();
    ChatRoom room = client.getRoom(args[0]);
    
    if (room == null) {
      respond(chat, "i am not in that channel!");
      return;
    }
    
    MultiUserChat muc = room.getChat();
    
    if (!isModerator(muc, jid)) {
      respond(chat, "you are not a channel moderator/admin/owner. ask a staff member!");
      return;
    }
    
    room.leave();
    respond(chat, "left channel");
  }  
}
