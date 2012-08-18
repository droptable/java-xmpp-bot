package ws.raidrush.xmpp.utils;

import org.apache.log4j.Logger;

public abstract class Task 
{
  /**
   * Indicates that this task was already executed - or not
   */
  private boolean executed = false;
  
  private String ident = "unnamed";
  
  /**
   * creates a named task
   * 
   * @param ident
   */
  public Task(String ident)
  {
    super();
    this.ident = ident;
  }
  
  /**
   * Executes the Task
   * 
   * @throws TaskException 
   */
  public void execute() throws TaskException 
  {
    if (executed == true)
      throw new TaskException("Task was already executed!");
    
    executed = true;
    perform();
  }
  
  /**
   * Executes the Task without throwing exceptions
   * 
   */
  public boolean executeSilent()
  {
    try {
      execute();
      Logger.getRootLogger().info("Successfuly executed task \"" + ident + "\"");
      return true;
    } catch (TaskException e) {
      Logger.getRootLogger().error("Unable to execute task \"" + ident + "\"", e);
    } catch (Exception e2) {
      Logger.getRootLogger().error("Error while executing task \"" + ident + "\"", e2);
    }
    
    return false;
  }
  
  /**
   * Business logic
   */
  abstract protected void perform();

  /**
   * getter for `execute`
   * 
   * @return true if this task was executed before, false if not
   */
  public boolean isExecuted() 
  {
    return executed;
  }

  /**
   * setter for `execute`
   * 
   * @param executed
   * @return the value you provided
   */
  public boolean setExecuted(boolean executed) 
  {
    this.executed = executed;
    return executed;
  }
  
  /**
   * returns the name (ident) of this task
   * 
   * @return ident
   */
  public String getIdent() { return ident; }
}
