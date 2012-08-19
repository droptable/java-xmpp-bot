package ws.raidrush.xmpp.plugins;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Message;

import ws.raidrush.xmpp.Command;
import ws.raidrush.xmpp.ManagedMultiUserChat;

import ws.raidrush.shunt.Parser;
import ws.raidrush.shunt.Scanner;
import ws.raidrush.shunt.Context;
import ws.raidrush.shunt.Function;
import ws.raidrush.shunt.ParseError;
import ws.raidrush.shunt.SyntaxError;
import ws.raidrush.shunt.RuntimeError;

public class Calc extends Command
{
  private final Context ctx;
  
  public Calc(ManagedMultiUserChat chat)
  {
    super(chat);
    
    ctx = new Context();
    setup();
  }

  private void setup()
  {
    ctx.def("min", new Function() {
      @Override
      public double call(double[] args) { 
        if (args.length < 2)
          return 0.;
        
        return Math.min(args[0], args[1]); 
      } 
    });
    
    ctx.def("max", new Function() {
      @Override
      public double call(double[] args) { 
        if (args.length < 2)
          return 0.;
        
        return Math.max(args[0], args[1]); 
      } 
    });
    
    ctx.def("abs", new Function() {
      @Override
      public double call(double[] args) { 
        if (args.length < 1)
          return 0.;
        
        return Math.abs(args[0]); 
      } 
    });
    
    ctx.def("log", new Function() {
      @Override
      public double call(double[] args) 
      { 
        if (args.length < 1)
          return 0.;
        
        return Math.log(args[0]); 
      } 
    });
    
    ctx.def("exp", new Function() {
      @Override
      public double call(double[] args) 
      { 
        if (args.length < 1)
          return 0.;
        
        return Math.exp(args[0]); 
      } 
    });
    
    ctx.def("sqrt", new Function() {
      @Override
      public double call(double[] args) 
      { 
        if (args.length < 1)
          return 0.;
        
        return Math.sqrt(args[0]); 
      }
    });
  }
  
  @Override
  protected void perform(Message msg, String body)
  {
    try {
      double res = new Parser(new Scanner(body)).reduce(ctx);
      respond(String.valueOf(res));
    } catch (RuntimeError e) {
      Logger.getRootLogger().error("Runtime error", e);
      respond("runtime error: " + e.getMessage());
    } catch (ParseError e) {
      Logger.getRootLogger().error("Parse error", e);
      respond("parse error: " + e.getMessage());
    } catch (SyntaxError e) {
      Logger.getRootLogger().error("Syntax error", e);
      respond("syntax error: " + e.getMessage());
    }
  }
}
