package ws.raidrush.xmpp.handler;

import java.util.Stack;
import java.util.HashMap;

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
  protected String nick;
  
  // the trigger we should handle
  protected String trigger;
  protected int tlength;
  
  // filters and commands
  protected Stack<Plugin> filters;
  protected HashMap<String, Plugin> commands;
  
  public ChatRoom(Client client, MultiUserChat room, String nick, String trigger) 
  {
    this.client = client;
    this.room   = room;
    this.nick   = nick;
    
    this.trigger = trigger;
    this.tlength = trigger.length();
    
    
    Logger.info("handler for room " + this.room.getRoom() + " created");
  }

  @Override
  public void processPacket(Packet pack) 
  {
    if (!(pack instanceof Message))
      return;
    
    Message msg = (Message) pack;
    String body = msg.getBody();
    
    Logger.info(msg.getFrom() + " wrote a message in " + this.room.getRoom() + ": " + msg.getBody());
    
    if (body.length() > this.tlength) {
      // apply trigger (active plugins)
      String pre = body.substring(0, this.tlength);
      
      if (pre.equals(this.trigger)) {
        String exec = body.substring(body.indexOf(" "))
            .substring(0, this.tlength).toLowerCase();
        
        if (this.commands.containsKey(exec)) {
          // parse message and lookup command
          Plugin cmd = this.commands.get(exec);
          // cmd.execute(); ?
          
          return;
        }
      }
    }
    
    // apply filters (passive plugins)
    for (Plugin flt : this.filters)
      ; // flt.execute(); ?
  }
  
  protected String[] parseArguments(String text)
  {
    Stack<String> args = new Stack<String>();
    
    int     len = text.length();
    boolean str = false;
    String  buf = "";
    
    for (int i = 0; i < len; ++i) {
      char chr = text.charAt(i);
      
      // § if a string-literal is open
      if (str == true) {
        // § if the next char is a quotation mark
        if (chr == '"') {
          // § close the string-literal and wait for a whitespace char, quotation mark or EOF
          str = false;
          continue;
        }
        
        // § append char to buffer and continue with next char
        buf += chr;
        continue;
      }
      
      // § if a string-literal is not open and the next char is a quotation mark
      if (chr == '"') {
        // § open string-literal
        str = true;
        
        // § if the current buffer is not empty
        if (!buf.isEmpty()) {
          // § push it onto the stack
          args.add(buf);
          buf = "";
        }
        
        // § continue with next char
        continue;
      }
      
      // § if a string-literal is not open and the next char is a whitespace char
      if (chr == ' ') {
        // § if the current buffer is not empty
        if (!buf.isEmpty()) {
          // § push it onto the stack
          args.add(buf);
          buf = "";
        }
        
        // § continue with next char
        continue;
      }
      
      // § if a string-literal is not open and next char is not a whitespace char
      // § add it to the current buffer
      buf += chr;
    }
    
    // § if EOF is reached and buffer is not empty
    if (!buf.isEmpty())
      // § push it onto the stack
      args.add(buf);
    
    // § done
    return (String[]) args.toArray();
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
   * returns the jid for a user inside the multi-user-chat
   * 
   * @param nick
   * @return String
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
