package ws.raidrush.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.plugin.PluginInformation;
import ws.raidrush.xmpp.plugin.PluginActivationRule;

/**
 * 
 * @author Alex²
 *
 */
public abstract class Plugin {

  private PluginInformation pluginInformation;

  private PluginActivationRule activationRule;
  
  @SuppressWarnings("unused")
  private final Client client;
  
  private final Chat chat;

  private final MultiUserChat room;

  private final Message message;

  /**
   * @param message
   *            the message wich triggered the execution of this plugin if
   *            this is a passive plugin this will be null
   * @param chat
   *            should always reference the user who called the plugin. if
   *            this is a passive plugin this will be null
   * @param room
   *            should always reference the room of the user who called the
   *            plugin. if this is a passive plugin it just contains the room
   *            it should interact with. if the bot was contacted via room
   *            unrelated query this will be null
   */

  public Plugin(Client client, Message message, Chat chat, MultiUserChat room) {
    this.message = message;
    this.chat = chat;
    this.room = room;
    
    this.client = client;

    initialise();
  }

  /**
   * Method which will be called external to start the plugin execution Note
   * that plugins will be ran their own thread
   */
  public void executePlugin() {
    Thread pluginThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          doActions();
        } catch (Exception e) {
          Logger.error("Plugin " + pluginInformation.getName() + " caused error");
          Logger.error(e.getMessage());
        }
      }
    });
    pluginThread.start();
  }

  /**
   * This method will be overwritten by the plugin creator
   * 
   * @throws Exception
   *             parent exception handling for anything happening here
   */
  protected abstract void doActions() throws Exception;

  protected abstract void initialise();

  /**
   * @return the {@link PluginInformation}
   */
  public PluginInformation getPluginInformation() {
    return pluginInformation;
  }

  /**
   * This method should be called at least and only ONCE for the plugin to be
   * valid.
   * 
   * @param pluginInformation
   *            the {@link PluginInformation} to set
   */
  protected void setPluginInformation(PluginInformation pluginInformation) {
    this.pluginInformation = pluginInformation;
  }

  /**
   * This object is for handling chats with single users e.g. the user who
   * called this plugin to act
   * 
   * @return the chat
   */
  protected Chat getChat() {
    return chat;
  }

  /**
   * This object is for handling chat rooms e.g. the room this plugin was
   * called out of
   * 
   * @return the room
   */
  protected MultiUserChat getRoom() {
    return room;
  }

  /**
   * @return the activationRule
   */
  public PluginActivationRule getActivationRule() {
    return activationRule;
  }

  /**
   * @param activationRule
   *            the activationRule to set
   */
  protected void setActivationRule(PluginActivationRule activationRule) {
    this.activationRule = activationRule;
  }

  /**
   * The message which triggered the execution of this plugin
   * 
   * @return the message
   */
  public Message getMessage() {
    return message;
  }

}
