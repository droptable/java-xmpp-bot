package ws.raidrush.xmpp.plugin.activation;

import java.util.regex.Pattern;

import ws.raidrush.xmpp.plugin.PluginActivationRule;

/**
 * 
 * Activation rule for Command or anything message trigger based
 * 
 * @author Alex²
 * 
 * 
 */
public class MessagePluginActivationRule extends PluginActivationRule {

	private final Pattern pattern;

	/**
	 * 
	 * @param regularExpression
	 *            regex which matches the trigger
	 * @param flags
	 *            java regex flags
	 */
	public MessagePluginActivationRule(String regularExpression, int flags) {
		pattern = Pattern.compile(regularExpression, flags);
	}

	/**
	 * if the regex matches the message the rule applies
	 */
	@Override
	public boolean applies() {
		String message = (String) getParameters()[0];
		return pattern.matcher(message).find();
	}

}
