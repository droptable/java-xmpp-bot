package ws.raidrush.xmpp.plugin;

/**
 * 
 * Class for holding basic plugin related data
 * 
 * @author Alex²
 * 
 */
public class PluginInformation {

  private final String name;
  private final String autor;
  private final String version;
  private final String description;

  public PluginInformation(String name, String autor, String version, String description) {
    super();
    this.name = name;
    this.autor = autor;
    this.version = version;
    this.description = description;
  }

  /**
   * @return the plugin name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the plugin autor
   */
  public String getAutor() {
    return autor;
  }

  /**
   * @return the plugin version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @return the plugin description
   */
  public String getDescription() {
    return description;
  }

}
