package ws.raidrush.xmpp.handler;

import java.util.HashSet;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;

import ws.raidrush.xmpp.Client;
import ws.raidrush.xmpp.Logger;
import ws.raidrush.xmpp.Plugin;

public class ChatRoom implements PacketListener 
{
  // our <master> client
  protected Client client;
  
  // the MutliUserChat
  protected MultiUserChat room;
  
  // our nick in that room
  protected String nick, ident;
  
  // the trigger we should handle
  protected String trigger;
  protected int tlength;
  
  // filters and commands
  protected HashSet<String> filters;
  protected HashSet<String> commands;
  
  /**
   * constructor
   * 
   * @param client
   * @param room
   * @param nick
   * @param trigger
   */
  public ChatRoom(Client client, MultiUserChat room, String nick, String trigger) 
  {
    this.client = client;
    this.room   = room;
    this.nick   = nick;
    this.ident  = this.room.getRoom() + "/" + this.nick;
    
    this.trigger = trigger;
    this.tlength = trigger.length();
    
    this.filters  = new HashSet<String>();
    this.commands = new HashSet<String>();
    
    this.commands.add("echo");
    this.commands.add("quote");
    
    Logger.info("handler for room " + this.room.getRoom() + " created");
  }
  
  /**
   * forwarded to Client#leave(MultiUserChat)
   * 
   */
  public void leave() { this.client.leave(this.room); }
  
  @Override
  public void processPacket(Packet pack) 
  {
    if (!(pack instanceof Message))
      return;
    
    final Message msg = (Message) pack;
    final String body = msg.getBody();
    String from = msg.getFrom();
    
    Logger.info("" + msg.getType());
    
    if (from.equals(this.ident))
      return;
    
    Logger.info(from + " wrote a message in " + this.room.getRoom() + ": " + msg.getBody());
    
    if (body.length() > this.tlength) {
      // apply trigger (active plugins)
      String pre = body.substring(0, this.tlength);
      
      if (pre.equals(this.trigger)) {
        int split = body.indexOf(" ");
        if (split == -1) split = body.length();
        
        String exec = body.substring(this.tlength, split).toLowerCase(),
               rest = body.substring(exec.length() + this.tlength);
        
        if (exec.equals("load")) {
          Plugin.load(rest);
          return;
        }
        
        Logger.info("check if plugin is registred in this chat: '" + exec + "'");
        
        if (this.commands.contains(exec)) {
          Logger.info("command is available!");
          
          final Plugin cmd = Plugin.get(exec, Plugin.Type.ACTIVE);
          
          if (cmd != null) {
            Logger.info("plugin is ready!");
            this.execute(cmd, msg, rest);
            return;
          }
        }
      }
    }
    
    // apply filters (passive plugins)
    for (String flt : this.filters)
      this.execute(Plugin.get(flt, Plugin.Type.PASSIVE), msg, body);
  }
  
  /**
   * executes a plugin
   * 
   * @param plugin
   * @param msg
   * @param body
   */
  private void execute(final Plugin plugin, final Message msg, final String body) 
  {
    Logger.info("executing plugin");
    
    final ChatRoom self = this;
    
    // start plugin in a new thread
    (new Thread(
        new Runnable() {
          @Override 
          public void run() { plugin.execute(msg, body, room, self); } 
        }
    )).start();
  }

  /**
   * returns the nick used in the chat-room
   * 
   * @return String
   */
  public String getNick() 
  {
    return this.nick;
  }
  
  /**
   * returns the name of the chat-room
   * 
   * @return String
   */
  public String getRoom()
  {
    return this.room.getRoom();
  }
  
  /**
   * returns the chat-room
   * 
   * @return MultiUserChat
   */
  public MultiUserChat getChat()
  {
    return this.room;
  }
  
  /**
   * returns the client
   * 
   * @return Client
   */
  public Client getClient()
  {
    return this.client;
  }
  
  /**
   * returns the jid for a user inside the multi-user-chat
   * 
   * @param nick
   * @return String
   */
  public String getUserJid(String nick)
  {
    if (nick.indexOf("/") == -1)
      nick = this.getRoom() + "/" + nick;
    
    Occupant user = this.room.getOccupant(nick);
    
    if (user == null)
      return null;
    
    return user.getJid();
  }
}
