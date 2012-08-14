package ws.raidrush.xmpp.plugin.activation;

import ws.raidrush.xmpp.plugin.PluginActivationRule;

/**
 * 
 * Activation rule for passive plugins which are only loaded once (usually)
 * 
 * @author Alex²
 *
 */
public class PassivePluginActivationRule extends PluginActivationRule {
	
	@Override
	public boolean applies() {
		return true;
	}

}
