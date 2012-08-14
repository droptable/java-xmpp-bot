package ws.raidrush.xmpp;

import java.util.Stack;
import java.util.HashMap;

import ws.raidrush.xmpp.RoomChatHandler;

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
  protected HashMap<String, RoomChatHandler> mucs;
  protected HashMap<String, String>        nicks;
  
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
    
    this.mucs  = new HashMap<String, RoomChatHandler>();
    this.nicks = new HashMap<String, String>();
    this.queue = new Stack<Runnable>();
    
    // prepare connection
    ConnectionConfiguration config = new ConnectionConfiguration(auth, port);
    this.xmpp = new XMPPConnection(config);
    
    // auto-subscribes to everything, because we love you all :-D
    this.xmpp.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
    
    // handle private messages
    this.xmpp.getChatManager().addChatListener(new UserChatHandler(this));
  }
  
  /**
   * trys to find the JID from a user an a room
   * 
   * @param nick
   * @param room
   */
  public synchronized String getUserJid(String nick, String room)
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
   * forwarded to {@link Client#join(String, String)}
   * 
   * @param room
   * @return Client
   */
  public Client join(String room, String trigger)
  {
    join(room, trigger, this.user);
    return this;
  }
  
  /**
   * forwarded to {@link Client#join(String, String, String)}
   * 
   * @param room
   * @param nick
   * @return Client
   */
  public Client join(String room, String trigger, String nick)
  {
    this.join(room, trigger, nick, "");
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
  public Client join(final String room, final String trigger, final String nick, final String pass)
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
        MultiUserChat   muc = new MultiUserChat(xmpp, room);
        RoomChatHandler rmh = new RoomChatHandler(self, muc, nick, trigger);
        
        muc.addMessageListener(rmh);
        
        mucs.put(room, rmh);
        
        try {
          DiscussionHistory dh = new DiscussionHistory();
          dh.setMaxChars(0);
          
          muc.join(nick, pass, dh, 20);
          Logger.info("joined room " + room);
        } catch (XMPPException e) {
          Logger.warn("unable to join muc " + room);
          Logger.warn(e.getMessage());
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
            Thread.sleep(10);
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
          
          for (int i = 0; i < 10; ++i) {
            try {
              self.connect();
              okay = true;
              break;
            } catch (XMPPException e) {
              int next = 10 * i;
              Logger.warn("reconnect failed, waiting " + next + " seconds for next attempt");
              
              try {
                Thread.sleep(next);
              } catch (InterruptedException e1) {
                Logger.fatal("unable to sleep");
                break;
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
