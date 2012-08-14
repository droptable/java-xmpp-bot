package ws.raidrush.xmpp.plugin;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.Logger;
import ws.raidrush.xmpp.handler.ChatQuery;

public class Join extends Admin
{
  public Join() {}

  @Override
  public void execute(Message msg, String body, Chat chat, ChatQuery query) 
  {
    String[] args = this.parseArguments(body);
    
    if (args.length <= 1) {
      respond(chat, "Fehlende Parameter: join <room> <trigger> [ <nick> [ <password> ] ]");
      return;
    }
   
    if (query.getClient().getRoom(args[0]) != null) {
      respond(chat, "Ich bin bereits in diesem Chatraum");
      return;
    }
    
    String jid = getJidFromContact(msg.getFrom(), chat, query);
    if (jid == null) return;
    
    Logger.info("got jid from contact: " + jid);
    respond(chat, "Okay, ich betrete nun den Chatraum um zu überprüfen ob Du auch Moderator bist.");
    respond(chat, "Ich benötige Moderatorenrechte in diesem Chatraum. Das ganze dauert 20 Sekunden.");
    
    String jroom    = args[0];
    String jtrigger = args[1];
    String jnick    = (args.length > 2) ? args[2] : null;
    
    Client client = query.getClient();
    
    if (isModerator(client, jid, jroom, jnick) == false) {
      respond(chat, "Fehler beim betreten des Chatraums. Möglicherweise bist du kein Moderator!");
      return;
    }
    
    respond(chat, "Sieht gut aus, ich komme in wenigen Augenblicken wieder um zu bleiben ;-)");
    
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
  }

  @Override
  public Type getType() { return Type.ACTIVE; }

}
