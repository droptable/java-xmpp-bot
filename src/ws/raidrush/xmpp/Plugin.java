package ws.raidrush.xmpp;

import java.util.Stack;
import java.util.HashMap;
import java.util.HashSet;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.handler.ChatRoom;
import ws.raidrush.xmpp.handler.ChatQuery;

abstract public class Plugin 
{
  public enum Type {
    ACTIVE,
    PASSIVE
  }
  
  // lookup cache
  protected static HashSet<String> 
    lookup = new HashSet<String>();
  
  // active plugins (key = exec-command!)
  protected static HashMap<String, Plugin> 
    active = new HashMap<String, Plugin>();
  
  // passive plugins (key = name of plugin)
  protected static HashMap<String, Plugin> 
    passive = new HashMap<String, Plugin>();
  
  /**
   * constructor
   * 
   * @param client
   * @param room
   */
  public Plugin() { }
  
  public static Plugin get(String name, Type type)
  {
    if (lookup.contains(name))
      return null;
    
    if (type == Type.ACTIVE) {
      Logger.info("searching for plugin " + name + " in the active-stack");
      
      if (!active.containsKey(name)) {
        Logger.info("trying to load it from source");
        
        if (load(name) != true) {
          Logger.info("could not load plugin from source!");
          lookup.add(name);
          return null;
        } 
      }
      
      if (!active.containsKey(name)) {
        Logger.info("plugin not found");
        lookup.add(name);
        return null;
      }
      
      Logger.info("plugin found!");
      return active.get(name);
    }
    
    Logger.info("searching for plugin " + name + " in the passive-stack");
    if (!passive.containsKey(name)) {
      Logger.info("trying to load it from source");
      
      if (load(name) != true) {
        Logger.info("could not load plugin from source!");
        lookup.add(name);
        return null;
      } 
    }
    
    if (!passive.containsKey(name)) {
      Logger.info("plugin not found");
      lookup.add(name);
      return null;
    }
    
    Logger.info("plugin found!");
    return passive.get(name);
  }
  
  /**
   * loads a plugin and adds it to the registry
   * 
   * @param name
   * @param client
   * @reutrn true if the plugin was found, false if not
   */
  @SuppressWarnings("unchecked")
  public static boolean load(String name)
  {
    Logger.info("loading " + name);
    
    String className = pluginToClassName(name, "ws.raidrush.xmpp.plugin");
    Class<Plugin> pluginClass;
    
    Logger.info("loading " + name + " from " + className);
    
    try {
      pluginClass = (Class<Plugin>) ClassLoader.getSystemClassLoader().loadClass(className);
      Logger.info("loaded plugin " + name + " @ " + className);
    } catch (ClassNotFoundException e) {
      Logger.info("unable to load plugin " + name + " @ " + className);
      return false;
    }
    
    // can this happen?
    if (pluginClass == null) {
      Logger.info("unable to load plugin " + name + " @ " + className + " (class-loader returned null)");
      return false;
    }
    
    Plugin plugin;
    
    try {
      plugin = (Plugin) pluginClass.getConstructors()[0].newInstance();
    } catch (Exception e) {
      Logger.info("loaded plugin " + name + " @ " + className + " instance failed!");
      return false;
    }

    if (plugin.getType() == Type.ACTIVE) {
      Logger.info("plugin is of type <active>");
      Plugin.active.put(name, plugin);
    } else {
      Logger.info("plugin is of type <passive>");
      Plugin.passive.put(name, plugin);
    }
    
    Logger.info(plugin.getMeta());
    
    return true;
  }
  
  /**
   * should execute its action
   * 
   * @param p   the original message object
   * @param m   the message-body without trigger 
   * @param c   the chat-room (MultiUserChat / Chat)
   * @param r   the room / query (origin)
   */
  abstract public void execute(Message p, String m, MultiUserChat c, ChatRoom r);
  abstract public void execute(Message p, String m, Chat c, ChatQuery q);
  
  // should return the type of this plugin
  abstract public Type getType();
  
  /**
   * Override this method for your own informations
   * 
   * @return
   */
  public String getMeta() { return "No Informations available"; }
  
  /**
   * parses arguments and returns them as String[]
   * 
   * @param text
   * @return
   */
  protected final String[] parseArguments(String text)
  {
    Stack<String> args = new Stack<String>();
    
    int     len = text.length();
    boolean str = false;
    String  buf = "";
    
    for (int i = 0; i < len; ++i) {
      char chr = text.charAt(i);
      
      if (str == true) {
        if (chr == '"') {
          str = false;
          continue;
        }
        
        buf += chr;
        continue;
      }
      
      if (chr == '"') {
        str = true;
        
        if (!buf.isEmpty()) {
          args.add(buf);
          buf = "";
        }
        
        continue;
      }
      
      if (chr == ' ') {
        if (!buf.isEmpty()) {
          args.add(buf);
          buf = "";
        }
        
        continue;
      }
      
      buf += chr;
    }
    
    if (!buf.isEmpty())
      args.add(buf);
    
    int l = args.size();
    String[] res = new String[l];
    
    for (int i = 0; i < l; ++i)
      res[i] = args.get(i);
    
    return res;
  }
  
  /**
   * formats the dashed plugin-name to a camelcased class-name
   * 
   * example: "foo-bar" -> "FooBar"
   * 
   * @param name
   * @param pgk
   * @return
   */
  protected static String pluginToClassName(String name, String pgk)
  {
    try {
    String[] parts = ("-" + name).split("-");
    int len = parts.length;
    
    String res = "";
    
    for (int i = 1; i < len; i++)
      res += Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
        
    return pgk + "." + res;
    } catch (Exception e) {
      Logger.info(e.getMessage());
      return null;
    }
  }
}
