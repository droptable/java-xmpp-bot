package ws.raidrush.xmpp;

public class Logger 
{
  public static final short 
    LEVEL_SILENT  = 0,
    LEVEL_VERBOSE = 1,
    LEVEL_WARNING = 2,
    LEVEL_INFO    = 3,
    LEVEL_ERROR   = 4,
    LEVEL_FATAL   = 5;
  
  protected static short level = LEVEL_INFO;
  
  /**
   * getter for `level`
   * 
   * @return
   */
  public static short getLevel()
  {
    return level;
  }
  
  /**
   * setter for `level`
   * 
   * @param lvl
   */
  public static void setLevel(short lvl)
  {
    if (lvl < 0 || lvl > 5)
      lvl = 0;
    
    level = lvl;
  }
  
  /**
   * alias for LEVEL_INFO
   * 
   * @param msg
   */
  public static void info(String msg)
  {
    log(msg, LEVEL_INFO);
  }
  
  /**
   * alias for LEVEL_WARNING
   * 
   * @param msg
   */
  public static void warn(String msg)
  {
    log(msg, LEVEL_WARNING);
  }
  
  /**
   * alias for LEVEL_ERROR
   * 
   * @param msg
   */
  public static void error(String msg)
  {
    log(msg, LEVEL_ERROR);
  }
  
  /**
   * alias for LEVEL_FATAL
   * 
   * @param msg
   */
  public static void fatal(String msg)
  {
    log(msg, LEVEL_FATAL);
  }
  
  /**
   * alias for LEVEL_VERBOSE
   * 
   * @param msg
   */
  public static void log(String msg)
  {
    log(msg, LEVEL_VERBOSE);
  }
  
  /**
   * writes a message to <stdout>
   * 
   * @param msg
   * @param lvl
   */
  public static void log(String msg, short lvl)
  {
    if (level > 0 && lvl >= level)
      System.out.println("[" + levelToString(lvl) + "] " + msg);
  }
  
  protected static String levelToString(short lvl)
  {
    switch (lvl) {
      case LEVEL_SILENT:
        return "SILENT";
        
      case LEVEL_VERBOSE:
        return "VERBOSE";
        
      case LEVEL_WARNING:
        return "WARNING";
        
      case LEVEL_INFO:
        return "INFO";
        
      case LEVEL_ERROR:
        return "ERROR";
        
      case LEVEL_FATAL:
        return "FATAL";
        
      default:
        return "UNKNOWN";
    }
  }
}
