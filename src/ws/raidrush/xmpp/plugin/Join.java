package ws.raidrush.xmpp.plugin;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.Logger;
import ws.raidrush.xmpp.handler.ChatQuery;
import ws.raidrush.xmpp.handler.ChatRoom;

public class Join extends Admin
{
  public Join() {}
  
  @Override
  public void execute(Message p, String m, MultiUserChat c, ChatRoom r) 
  {
    // this command is only available via query
    respond(c, "error: use a private chat for that command");
  }

  @Override
  public void execute(Message msg, String body, Chat chat, ChatQuery query) 
  {
    String[] args = this.parseArguments(body);
    
    if (args.length <= 1) {
      respond(chat, "missing arguments: join <room> <trigger> [ <nick> [ <password> ] ]");
      return;
    }
        
    // setup
    String nick = msg.getFrom(),
           jid  = query.getClient().getUserJid(nick);
    
    if (jid == null) {
      respond(chat, "hi, thanks for your message! but... for this command i only "
          + "accept private messages from contacts in my contactlist. please add "
          + "me as contact (if you not already have) and write directly to me!");
      
      return;
    }
    
    Logger.info("got jid from contact: " + jid);
    respond(chat, "okay, i will join and check if you have moderator-privileges on that channel");
    respond(chat, "also, i need moderator-rights in that channel. you have 20 seconds till i leave");
    
    String jroom    = args[0],
           jtrigger = args[1],
           jnick    = null;
    
    if (args.length > 2)
      jnick = args[2];
    
    Client client = query.getClient();
    
    if (this.isModerator(client, jid, jroom, jnick) == false) {
      respond(chat, "unable to join or you are not a channel moderator/admin/owner. ask a staff member!");
      return;
    }
    
    respond(chat, "looks good, i'll come back soon");
    
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      // noop
    }
    
    if (args.length > 3)
      client.join(jroom, jtrigger, jnick, args[3]);
    else if (args.length > 2)
      client.join(jroom, jtrigger, jnick);
    else
      client.join(jroom, jtrigger);
    
    try {
      client.getRoom(jroom).getChat().sendMessage("here i am! have fun! :-)");
    } catch (XMPPException e) {
      // noop
    }
  }

  @Override
  public Type getType() { return Type.ACTIVE; }

}
