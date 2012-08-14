package ws.raidrush.xmpp.plugin;

/**
 * 
 * @author Alex²
 *
 */
public abstract class PluginActivationRule {

  private Object[] parameters;
  
  public abstract boolean applies();

  /**
   * Parameters which could be needed to determinate if the rule applies
   * @return the parameters
   */
  public Object[] getParameters() {
    return parameters;
  }

  /**
   * @param parameters the parameters to set
   */
  public void setParameters(Object[] parameters) {
    this.parameters = parameters;
  }
  
  

}
