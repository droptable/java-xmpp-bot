package ws.raidrush.xmpp.plugin;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.Logger;
import ws.raidrush.xmpp.Plugin;
import ws.raidrush.xmpp.plugin.activation.MessagePluginActivationRule;
import ws.raidrush.xmpp.plugin.activation.PassivePluginActivationRule;
import ws.raidrush.xmpp.plugin.PluginActivationRule;

/**
 * 
 * @author Alex²
 * 
 */
public class PluginManager {

	private final Map<PluginActivationRule, Class<Plugin>> plugins = new HashMap<PluginActivationRule, Class<Plugin>>();

	/**
	 * Tries to load a plugin class via dynamic class loading also tries to
	 * create a instance via reflection to check if the plugin is valid
	 * 
	 * @param qualifiedClassName
	 *            the plugin class to load
	 * @return true == registration successful | false == registration failed
	 */
	@SuppressWarnings("unchecked")
	public boolean registerPlugin(String qualifiedClassName) {

		Class<Plugin> pluginClass;
		try {
			pluginClass = (Class<Plugin>) ClassLoader.getSystemClassLoader().loadClass(qualifiedClassName);
		} catch (ClassNotFoundException e) {
			Logger.warn("Plugin class could not be found:" + qualifiedClassName);
			return false;
		}
		if (pluginClass != null) {
			try {
				Plugin plugin = getPluginInstance(pluginClass, null, null, null);

				if (validatePlugin(plugin, qualifiedClassName)) {
					plugins.put(plugin.getActivationRule(), pluginClass);

				} else {
					return false;
				}
				Logger.info("Plugin successfully registered: " + plugin.getPluginInformation().toString());
			} catch (Exception e) {
				Logger.warn("Plugin class could not be initialized:" + qualifiedClassName);
				return false;
			}
		}
		return true;
	}

	/**
	 * Determinate whenever a plugin is valid
	 * 
	 * @param plugin
	 *            the plugin to validate
	 * @param qualifiedClassName
	 *            the name of the plugins class
	 * @return true == valid | false == invalid
	 */
	private boolean validatePlugin(Plugin plugin, String qualifiedClassName) {
		if (plugin.getPluginInformation() == null) {
			Logger.warn("Plugin got no meta information:" + qualifiedClassName);
			return false;
		}

		if (plugin.getActivationRule() == null) {
			Logger.warn("Plugin got no activation rule:" + qualifiedClassName);
			return false;
		}

		return true;
	}

	/**
	 * @return the plugins
	 */
	public Map<PluginActivationRule, Class<Plugin>> getPlugins() {
		return plugins;
	}

	public void checkForMessageActivation(Message message, Chat chat, MultiUserChat room) {
		for (PluginActivationRule rule : plugins.keySet()) {
			if ((rule instanceof MessagePluginActivationRule)) {
				rule.setParameters(new Object[] { message.getBody() });
				if (rule.applies())
					getPluginInstance(plugins.get(rule), message, chat, room).executePlugin();
			}
		}
	}

	private Plugin getPluginInstance(Class<Plugin> pluginClass, Message message, Chat chat, MultiUserChat room) {
		try {
			return (Plugin) pluginClass.getConstructors()[0].newInstance(message, chat, room);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void startPassivePlugins(MultiUserChat room) {
		for (PluginActivationRule rule : plugins.keySet()) {
			if ((rule instanceof PassivePluginActivationRule)) {
				getPluginInstance(plugins.get(rule), null, null, room).executePlugin();
			}
		}
	}

}
