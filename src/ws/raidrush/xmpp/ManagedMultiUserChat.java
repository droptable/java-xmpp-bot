package ws.raidrush.xmpp;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.utils.AdminUtil;

/**
 * Acts as a thin wrapper for MultiUserChat-objects
 *
 */
public class ManagedMultiUserChat extends MultiUserChat implements PacketListener
{
  // the client
  private final Client client;
  
  // trigger / trigger-length
  private String trigger;
  private int    tlength;
  
  // plugins / commands
  private HashMap<String, Plugin> plugins;
  private HashSet<String>         commands;
  
  /**
   * Constructor
   * 
   * @param connection
   * @param room
   * @param trigger
   */
  public ManagedMultiUserChat(Client client, Connection connection, String room, String trigger)
  { 
    super(connection, room); 
    
    setTrigger(trigger);
    
    // assign client
    this.client = client;
    
    // init plugins
    plugins  = new HashMap<String, Plugin>();
    commands = new HashSet<String>();
    
    addMessageListener(this);
  }
  
  /**
   * Sets the trigger for this room
   * 
   * @param trigger
   */
  public void setTrigger(String trigger)
  {
    this.trigger = trigger;
    this.tlength = trigger.length();
  }
  
  /**
   * Adds a plugin
   * 
   * @param name
   * @param plugin
   */
 public void addPlugin(String name, Plugin plugin)
 {
   removePlugin(name);
   
   plugins.put(name, plugin);
   
   if (plugin instanceof Filter)
     ((Filter) plugin).attach();
   
   else if(plugin instanceof Command)
     commands.add(name);
   
   Logger.getRootLogger().info("Plugin \"" + name + "\" added in room \"" + getRoom() + "\"");
 }
 
 /**
  * Removes a plugin
  * 
  * @param name
  */
 public void removePlugin(String name)
 {
   if (!plugins.containsKey(name))
     return;
   
   Plugin plugin = plugins.get(name);
   
   if (plugin instanceof Filter) 
     ((Filter) plugin).remove();
   
   else if (plugin instanceof Command)
     commands.remove(name);
  
   Logger.getRootLogger().info("Plugin \"" + name + "\" in room \"" + getRoom() + "\" removed");
 }
 
  @Override
  public void processPacket(Packet packet)
  {
    if (!(packet instanceof Message))
      return;
    
    Message message = (Message) packet;
    
    if (message.getFrom().equals(getRoom() + "/" + getNickname()))
      return;
    
    Logger.getRootLogger().info("Got a message in " + getRoom());
    
    String body = message.getBody().trim();
    
    // check if message starts with `trigger`
    if (body.length() > tlength && body.substring(0, tlength).equals(trigger)) {
      Logger.getRootLogger().info("Plugin requested: " + body);
      
      String name = body.substring(tlength), args = "";
      
      int wspos = name.indexOf(" ");
      
      if (wspos > -1) {
        args = name.substring(wspos).trim();
        name = name.substring(0, wspos).trim();
      }
      
      name = name.toLowerCase();
      
      // build-in "set-trigger", "get-trigger", "enable" and "disable" commands
      if (AdminUtil.isModeratorNick(this, message.getFrom())) {
        if (name.equals("set-trigger")) {
          setTrigger(args);
          Logger.getRootLogger().info("Trigger in \"" + getRoom() + "\" is now: " + args);
          return;
        } else if (name.equals("get-trigger")) {
          try {
            sendMessage(trigger);
          } catch (Exception e) {
            Logger.getRootLogger().error("Unable to respond to \"" + getRoom() + "\"");
          }
          
          return;
        } else if (name.equals("enable")) {
          for (String pname : args.split(","))
            client.enablePlugin(getRoom(), pname.trim());
            
          return;
        } else if (name.equals("disable")) {
          for (String pname : args.split(","))
            client.disablePlugin(getRoom(), pname.trim());
          
          return;
        }
      }
      
      // best logging expressions evarr!!
      Logger.getRootLogger().info("Command \"" + name + "\" is " 
          + (commands.contains(name) ? "" : "not ") + "availabe");
      
      Logger.getRootLogger().info("The room \"" + getRoom() + "\" has " 
          + (plugins.containsKey(name) ? "" : "no ") + "access to this command");
      
      if (commands.contains(name) && plugins.containsKey(name)) {
        Logger.getRootLogger().info("Executing plugin \"" + name + "\" with arguments: " + args);
        
        try {
          // why? because f**k you, thats why :-P
          ((Command) plugins.get(name)).execute(message, args);
          
          // note: filters are attached as listener
          
        } catch (Exception e) {
          Logger.getRootLogger().error("Error while executing plugin \"" + name + "\"", e);
        }
      }
    }
  }

  // returns the client for this room
  public Client getClient() { return client; }
}