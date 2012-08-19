package ws.raidrush.xmpp;

import java.util.Stack;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.utils.Task;
import ws.raidrush.xmpp.utils.AdminUtil;

@SuppressWarnings("unused")
public class Client implements MessageListener, ChatManagerListener
{
  //xmpp-connection
  private final XMPPConnection xmpp;
  
  // database storage
  private final Storage storage;
  
  // host, username and password for login
  private final String host, user, pass;
  
  // resource used for login
  private String resource;
  
  // port for login
  private final int port;
  
  // indicator that the client is ready to take actions
  private boolean ready = false;
  
  // true if the client should reconnect if a connection gets lost
  public boolean autoReconnect = true;
  
  // queue with actions waiting for `ready` to become true
  private final Stack<Task> tasks;
  
  // multi-user-chats
  private final HashMap<String, ManagedMultiUserChat> rooms;
  
  // loaded plugin-models
  private final HashMap<String, Class<Plugin>> pluginModels;
  
  // plugin lookup cache to prevent loading non-existent plugins twice 
  private final HashSet<String> pluginLookup;
  
  /**
   * Initializes a new CLient and XMPPConnection
   * 
   * @param host
   * @param port
   */
  public Client(String host, int port, String user, String pass)
  {
    super();
    
    //save setup for reconnect
    this.host = host;
    this.port = port;
    this.user = user;
    this.pass = pass;
    
    // init xmpp
    ConnectionConfiguration config = new ConnectionConfiguration(host, port);
    config.setSendPresence(true);
    
    xmpp = new XMPPConnection(config);
    xmpp.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
    xmpp.getChatManager().addChatListener(this);
    
    // init tasks and rooms
    tasks = new Stack<Task>();
    rooms = new HashMap<String, ManagedMultiUserChat>();
    
    // init storage
    storage  = new Storage();
    
    // init plugin-models and lookup
    pluginModels = new HashMap<String, Class<Plugin>>();
    pluginLookup = new HashSet<String>();
  }
  
  /**
   * Forwards to {@link XMPPConnection#connect()}
   * 
   * @return Client
   * @throws XMPPException
   */
  public Client connect() throws XMPPException
  {
    if (xmpp.isConnected())
      return this;
    
    Logger.getRootLogger().info("Connecting ...");
    xmpp.connect();
    Logger.getRootLogger().info("Connected!");
    
    return this;
  }
  
  /**
   * Forwards to {@link XMPPConnection#login(String, String)}
   * 
   * @param user
   * @param pass
   * @return
   * @throws XMPPException
   */
  public Client login(String resource) throws XMPPException 
  {
    if (xmpp.isAuthenticated())
      return this;
    
    Logger.getRootLogger().info("Sending logindata ...");
    
    // save for reconnect
    this.resource = resource;
    
    xmpp.login(user, pass, this.resource);
    ready = true;
    
    Logger.getRootLogger().info("Login successful!");
    
    for (Task task : tasks)
      task.executeSilent();
    
    tasks.clear();
    return this;
  }
  
  /**
   * Joins a {@link ManagedMultiUserChat} and adds it to the room-manager
   * 
   * @param room
   * @param nick
   * @param pass
   * @param trigger
   * @return
   */
  public Client join(final String room, final String nick, final String pass, final String trigger)
  {
    Task task = new Task("join-room-" + room) {
      @Override
      protected void perform()
      {
        ManagedMultiUserChat mmuc = new ManagedMultiUserChat(Client.this, xmpp, room, trigger);
        
        // create history to prevent delayed messages
        DiscussionHistory dh = new DiscussionHistory();
        dh.setMaxChars(0);
        dh.setMaxStanzas(0);
        
        try {
          mmuc.join(nick, pass, dh, 2000);
          
          // add room to list -of-rooms
          rooms.put(room, mmuc);
          
          Logger.getRootLogger().info("Joined room \"" + room + "\"");
        } catch (XMPPException e) {
          Logger.getRootLogger().error("Unable to join room \"" + room + "\"", e);
          return;
        }
      }
    };
    
    addTask(task);
    return this;
    
    // beerCount++
    // beerCount++
  }
  
  /**
   * Leaves a room
   * 
   * @param room
   * @return
   */
  public Client leave(String room)
  {
    if (!rooms.containsKey(room))
      return this;
    
    MultiUserChat muc = rooms.get(room);
    muc.leave();
    
    // remove room from room-map and let the GC do the rest
    rooms.remove(room);
    return this;
  }
  
  /**
   * starts the main-loop and handles reconnection/shutdown
   * 
   * @void
   */
  public void listen()
  {
    Task task = new Task("client-listener") {
      @Override
      protected void perform()
      {
        for (;;) {
          if (!xmpp.isConnected())
            break;
          
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            Logger.getRootLogger().error("Unable to sleep", e);
            return; // note: return, not break
          }
        }
        
        Logger.getRootLogger().info("Connection lost");
        
        if (!autoReconnect)
          return;
                
        try {
          // wait a bit
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          Logger.getRootLogger().error("Unable to sleep", e);
          return;
        }
        
        if (!reconnect()) 
          return;
        
        perform(); // reuse task instead of creating a new one!
      }
    };
    
    addTask(task);
  }
  
  /**
   * Enables a plugin (or filter) in a room
   * 
   * @param room
   * @param name
   */
  @SuppressWarnings("unchecked")
  public Client enablePlugin(String room, String name)
  {
    if (!rooms.containsKey(room)) {
      Logger.getRootLogger().warn("Can not load plugin for a unknown chat-room");
      return this;
    }
    
    if (pluginLookup.contains(name)) {
      Logger.getRootLogger().warn("Plugin \"" + name + "\" was not found (cached)");
      return this;
    }
    
    Class<Plugin> pluginModel;
    
    if (pluginModels.containsKey(name)) {
      pluginModel = pluginModels.get(name);
      Logger.getRootLogger().info("Plugin successful loaded (cached)");
    } else {
      try {
        String[] nameParts = name.split("-");
        
        String  className = "ws.raidrush.xmpp.plugins.",
                upperName = "";
        
        for (int i = 0, l = nameParts.length; i < l; ++i)
          upperName += nameParts[i].substring(0, 1).toUpperCase() + nameParts[i].substring(1);
        
        className += upperName;
        Logger.getRootLogger().info("Loading plugin \"" + name + "\" from package " + className);
        
        try {
          pluginModel = (Class<Plugin>) ClassLoader.getSystemClassLoader().loadClass(className);
          Logger.getRootLogger().info("Plugin successful loaded");
        } catch (ClassNotFoundException e) {
          Logger.getRootLogger().warn("Unable to load plugin");
          pluginLookup.add(name);
          return this;
        }
      
        // can this happen?
        if (pluginModel == null) {
          Logger.getRootLogger().error("Unable to load plugin: ClassLoader returned NULL");
          pluginLookup.add(name);
          return this;
        }
        
        pluginModels.put(name, pluginModel);
      } catch (Exception e) {
        Logger.getRootLogger().error("Error while loading plugin", e);
        return this;
      }
    }
    
    // loading is done, lets get an instance
    
    Plugin plugin;
    ManagedMultiUserChat mmuc = rooms.get(room);
    
    try {
      plugin = (Plugin) pluginModel.getConstructors()[0].newInstance(mmuc);
      Logger.getRootLogger().info("Plugin successful contructed");
    } catch (Exception e) {
      Logger.getRootLogger().error("Error while constructing plugin", e);
      return this;
    }
        
    mmuc.addPlugin(name, plugin);
    return this;
    
    // beerCount++
  }
  
  /**
   * Enables a plugin (or filter) in all rooms
   * 
   * @param name
   * @return Client
   */
  public Client enablePlugin(String name)
  {
    for (String room : rooms.keySet())
      enablePlugin(room, name);
    
    return this;
  }
  
  /**
   * Disables a plugin (or filter) in a room
   * 
   * @return Client
   */
  public Client disablePlugin(String room, String name)
  {
    if (!rooms.containsKey(room)) {
      Logger.getRootLogger().warn("Can not unload plugin for a unknown chat-room");
      return this;
    }
    
    rooms.get(room).removePlugin(name);
    return this;
  }
  
  /**
   * Disables a plugin (or filter) in all rooms
   * 
   * @param name
   * @return Client
   */
  public Client disablePlugin(String name) 
  {
    for (ManagedMultiUserChat mmuc : rooms.values())
      mmuc.removePlugin(name);
    
    return this;
  }
  
  /**
   * Disables a plugin (or filter) in all rooms and removes it from the plugin-list
   * Note: Use disablePlugin() if you want re-enable it later
   * 
   * @param name
   * @return
   */
  public Client removePlugin(String name)
  {
    if (!pluginModels.containsKey(name))
      return this;
    
    disablePlugin(name);
    
    pluginLookup.add(name);
    pluginModels.remove(name);
    
    return this;
  }
  
  /**
   * TODO implement storage first!
   * 
   * - connect
   * - login
   * - join all channels
   * -------------------------
   * Storage needed:
   * - add plugins to channels
   * 
   * @return true if everything was successful, false if not
   */
  protected boolean reconnect()
  {
    
    return false;
    
    /*
    boolean connected = false, loggedin = false;
    
    // ------------------------------------------
    // connect
    
    Logger.getRootLogger().info("Reconnecting ...");
    
    for (int i = 1; i < 11; ++i) {
      Logger.getRootLogger().info("Connect attempt #" + i);
      
      try {
        connect();
        connected = true;
        Logger.getRootLogger().info("Connected!");
        break;
      } catch (XMPPException e) {
        Logger.getRootLogger().error("Reconnect failed ...", e);
        
        try {
          Thread.sleep(i * 10000);
        } catch (InterruptedException e1) {
          Logger.getRootLogger().error("Unable to sleep", e);
          return false;
        }
      }
    }
    
    if (!connected) {
      Logger.getRootLogger().error("Reconnect aborted");
      return false;
    }
    
    // ------------------------------------------
    // login
    
    Logger.getRootLogger().info("Logging in ...");
    
    for (int i = 1; i < 11; ++i) {
      Logger.getRootLogger().info("Login attempt #" + i);
      
      try {
        login(resource);
        loggedin = true;
        Logger.getRootLogger().info("Login successful");
        break;
      } catch (XMPPException e) {
        Logger.getRootLogger().error("Unable to login ...", e);
        
        try {
          Thread.sleep(i * 10000);
        } catch (InterruptedException e1) {
          Logger.getRootLogger().error("Unable to sleep", e);
          return false;
        }
      }
    }
    
    if (!loggedin) {
      Logger.getRootLogger().error("Login aborted");
      return false;
    }
    
    Logger.getRootLogger().info("Connection successfuly re-established");
    
    // join channels
    
    
    return true;
    */
  }
  
  /**
   * adds a task to the task-queue or automatically executes it of the client is ready
   * 
   * @param task
   */
  public Client addTask(Task task)
  {
    if (ready == true) {
      task.executeSilent();      
      return this;
    }
    
    tasks.add(task);
    Logger.getRootLogger().info("Added task \"" + task.getIdent() + "\"");
    
    return this;
  }
  
  @Override
  public void processMessage(Chat chat, Message message)
  {
    // testing
    
    try {
      chat.sendMessage("hello world");
    } catch (XMPPException e) {
      Logger.getRootLogger().error("Unable to send message back to user", e);
    }
  }

  @Override
  public void chatCreated(Chat chat, boolean createdLocally)
  {
    // add client as message listener
    chat.addMessageListener(this);
  }
}
