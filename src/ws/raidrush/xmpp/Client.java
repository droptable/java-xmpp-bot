package ws.raidrush.xmpp;

import java.util.Stack;
import java.util.HashMap;

import ws.raidrush.xmpp.handler.ChatRoom;
import ws.raidrush.xmpp.handler.ChatQuery;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.packet.Presence;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.DiscussionHistory;

public class Client
{
  // connection handle
  protected XMPPConnection xmpp;
  
  // our chat rooms
  protected HashMap<String, ChatRoom> mucs;
  protected HashMap<String, String>   nicks;
  
  // some informations about this client
  protected String user, pass, auth;
  
  // ready or not?
  protected boolean ready = false;
  
  // stuff to do before the client is ready
  protected Stack<Runnable> queue;
  
  // auto-reconnect
  protected boolean reconnect = false;
  
  // user-requested disconnect to prevent reconnect
  protected boolean shutdown = false;
  
  // if listen(true): this thread runs the main-loop
  protected Thread master = null;
  
  /**
   * constructor
   * 
   * @param user
   * @param pass
   * @param auth
   * @param port
   */
  public Client(String user, String pass, String auth, int port)
  {
    this.user = user;
    this.auth = auth;
    this.pass = pass;
    
    this.mucs  = new HashMap<String, ChatRoom>();
    this.nicks = new HashMap<String, String>();
    this.queue = new Stack<Runnable>();
    
    // prepare connection
    ConnectionConfiguration config = new ConnectionConfiguration(auth, port);
    // config.setSendPresence(true);
    
    this.xmpp = new XMPPConnection(config);
    
    // auto-subscribes to everything, because we love you all :-D
    this.xmpp.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
    
    // handle private messages
    this.xmpp.getChatManager().addChatListener(new ChatQuery(this));
  }
  
  public XMPPConnection getXMPP() { return this.xmpp; }
  public String getUser() { return this.user; }
  
  /**
   * trys to find the JID from a user an a room
   * 
   * @param nick
   * @param room
   */
  public String getUserJid(String nick, String room)
  {
    Logger.info("fetching jid for " + nick + " in room " + room);
    
    for (String key : this.mucs.keySet())
      Logger.info(key);
    
    if (!this.mucs.containsKey(room)) {
      Logger.info("client is not in this room!");
      return null;
    }
    
    return this.mucs.get(room).getUserJid(nick);
  }
  
  /**
   * trys to find the JID from a user in the contact-manager
   * 
   * @param nick
   * @return String
   */
  public String getUserJid(String nick)
  {
    // remove resource
    int res = nick.indexOf("/");
    if (res > -1) nick = nick.substring(0, res);
    
    Logger.info("fetching jid for " + nick);
    
    Roster roster = this.xmpp.getRoster();
    
    if (!roster.contains(nick))
      return null;
    
    return roster.getEntry(nick).getUser();
  }
  
  /**
   * returns a joined room
   * 
   * @param room
   * @return ChatRoom
   */
  public ChatRoom getRoom(String room)
  {
    if (this.mucs.containsKey(room))
      return this.mucs.get(room);
    
    return null;
  }
  
  /**
   * setter for `reconnect`
   * 
   * @param rc
   * @return
   */
  public Client setReconnect(boolean rc)
  {
    this.reconnect = true;
    return this;
  }
  
  /**
   * getter for `reconnect`
   * 
   * @return
   */
  public boolean getReconnect()
  {
    return this.reconnect;
  }
  
  /**
   * adds a task
   * 
   * @param task
   */
  public void addTask(Runnable task)
  {
    this.queue.add(task);
  }
  
  /**
   * forwarded to {@link XMPPConnection#connect()}
   * 
   * @throws XMPPException
   */
  public void connect() throws XMPPException
  {
    Logger.info("connecting ...");
    xmpp.connect();
  }
  
  /**
   * disconnects the bot
   * 
   * @param msg
   */
  public void disconnect(String msg)
  {
    Logger.info("disconnecting (shutdown)");
    
    this.shutdown = true;
    
    if (this.master != null)
      this.master.interrupt();
    
    this.ready = false;
    
    if (msg != null) {
      Presence pres = new Presence(Presence.Type.unavailable, msg, 0, Presence.Mode.away);
      this.xmpp.disconnect(pres);
    } else
      this.xmpp.disconnect();
    
    // reset, maybe you want to reuse this client
    this.shutdown = false;
  }
  
  /**
   * leaves a chat-room
   * 
   * @param room
   */
  public Client leave(MultiUserChat room)
  {
    String name = room.getRoom();
    Logger.info("leaving " + name);
    Logger.info("a = joined: " + (room.isJoined() ? "a" : "b"));
    
    if (!this.mucs.containsKey(name))
      return this;
    
    //room.removeMessageListener(this.mucs.get(name));
    
    try {
      room.leave();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    
    this.mucs.remove(name);
    return this;
  }
  
  /**
   * forwarded to {@link Client#join(String, String, String, String)}
   * 
   * @param room
   * @return Client
   */
  public Client join(String room, String trigger)
  {
    join(room, trigger, this.user, "");
    return this;
  }
  
  /**
   * forwarded to {@link Client#join(String, String, String, String)}
   * 
   * @param room
   * @param nick
   * @return Client
   */
  public Client join(String room, String trigger, String nick)
  {
    join(room, trigger, nick, "");
    return this;
  }
  
  /**
   * joins a MultiUserChat room
   * 
   * @param room
   * @param nick
   * @param pass
   * @return Client
   */
  public synchronized Client join(final String room, final String trigger, final String nick, final String pass)
  {
    if (mucs.containsKey(room)) {
      Logger.info("already joined muc " + room + " with nick \"" 
          + mucs.get(room).getNick() + "\"");
      
      return this;
    }
    
    final Client self = this;
    
    Runnable task = new Runnable() {
      @Override 
      public void run() 
      {
        MultiUserChat muc = new MultiUserChat(xmpp, room);
        ChatRoom      rmh = new ChatRoom(self, muc, nick, trigger);
        
        muc.addMessageListener(rmh);
        
        mucs.put(room, rmh);
        
        try {
          DiscussionHistory dh = new DiscussionHistory();
          dh.setMaxChars(0);
          muc.join(nick, pass, dh, 2000);
        } catch (XMPPException e) {
          Logger.error("unable to join muc " + room);
          Logger.error(e.getMessage());
          return;
        } finally {        
          Logger.info("joined room " + room);
        }
      }
    };
    
    if (this.ready != true) 
      this.queue.add(task);
    else
      task.run();
    
    return this;
  }
  
  /**
   * forwarded to {@link XMPPConnection#login(String, String)}
   * 
   * @return Client
   */
  public Client login()
  {
    if (this.xmpp.isAuthenticated()) {
      Logger.info("already logged in");
      return this;
    }
    
    try {
      this.xmpp.login(this.user, this.pass);
      Logger.info("login successful");
      
      // apply tasks
      for (Runnable task : this.queue)
        task.run();
      
      this.queue.clear();
    } catch (XMPPException e) {
      Logger.warn("login failed");
      Logger.warn(e.getMessage());
    }
    
    this.ready = true;
    return this;
  }
  
  /**
   * forwarded to {@link Client#listen(boolean)}
   * 
   * @void
   */
  public void listen()
  {
    this.listen(false);
  }
  
  /**
   * starts the main-loop
   * 
   * @param bgp
   */
  public void listen(final boolean bgp)
  {
    if (this.ready != true)
      this.login();
    
    final Client self = this;
    
    Runnable loop = new Runnable() {
      @Override
      public void run()
      {
        for (;;) {
          // block
          if (!xmpp.isConnected())
            break;
          
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            Logger.fatal("unable to sleep! i need a break, good bye");
            return;
          }
        }
        
        Logger.info("connection lost");
        
        if (self.shutdown == true)
          return; // disconnect was requested
        
        if (self.reconnect == true) {
          boolean okay = false;
          
          Logger.info("trying to reconnect");
          
          for (int i = 0; i < 10; ++i) {
            try {
              self.connect();
              okay = true;
              break;
            } catch (XMPPException e) {
              int next = 10000 * i;
              Logger.warn("reconnect failed, waiting " + next + " seconds for next attempt");
              
              try {
                Thread.sleep(next);
              } catch (InterruptedException e1) {
                Logger.fatal("unable to sleep");
                return;
              }
            }
          }
          
          if (okay == false) {
            Logger.error("unable to reconnect. server might be down");
            return;
          }
          
          self.listen(bgp);
        }
      }
    };
    
    if (bgp == false) {
      loop.run();
      return;
    }
    
    this.master = new Thread(loop);
    this.master.start();
  }
}
