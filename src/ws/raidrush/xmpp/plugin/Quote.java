package ws.raidrush.xmpp.plugin;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import ws.raidrush.xmpp.Plugin;
import ws.raidrush.xmpp.handler.ChatQuery;
import ws.raidrush.xmpp.handler.ChatRoom;
/**
 * The quote plugin. wont get commented yet
 * @author Alex²
 *
 */
public class Quote extends Plugin {
  private List<String> quotes;

  public Quote() 
  {
    quotes = new ArrayList<String>();
  }

  @Override
  public void execute(Message msg, String body, MultiUserChat chat, ChatRoom room) {
    System.out.println(body);
    
    String[] params = this.parseArguments(body);
    
    if (params.length == 0) {
      sendToRoom(chat, getRandomQuote());
      return;
    }

    if (params.length == 1) {
      for (String quote : getSpecificQuotes(params[0])) {
        sendToRoom(chat, quote);
      }
      return;
    }

    if (params.length == 2) {
      if ("-del".equals(params[0])) {
        delQuote(chat, params[1]);
        return;
      }
    }

    if (params.length > 3) {
      if ("-add".equals(params[0])) {
        String quote = "";
        for (int i = 3, l = params.length; i < l; ++i)
          quote += " " + params[i];
        
        addQuote(chat, params[1], params[2], quote.trim());
        return;
      }
    }

  }

  private void addQuote(MultiUserChat chat, String autor, String id, String quote) {

    String syntaxError = "Syntax: !quote -add <autor> <id> <quote>";
    
    if (autor.equals("") || id.equals("") || quote.equals("")) {
      sendToRoom(chat, syntaxError);
      return;
    }
    String entry = "\"" + quote + "\" -- " + autor + " (" + id + ")";

    quotes.add(entry);

    sendToRoom(chat, "Added: " + entry);

  }

  private void delQuote(MultiUserChat chat, String id) {
    List<String> newList = new ArrayList<String>();
    for (String quote : quotes) {
      if (quote.endsWith(" (" + id + ")")) {
        sendToRoom(chat, "DELETD: " + quote);
      } else {
        newList.add(quote);
      }
    }
    if (quotes.size() == newList.size())
      sendToRoom(chat, "No quote with id \"" + id + "\" found!");
    quotes = newList;
  }

  private List<String> getSpecificQuotes(String searchTerm) {
    List<String> specificQuotes = new ArrayList<String>();
    for (String quote : quotes) {
      if (quote.toLowerCase().contains(searchTerm.toLowerCase()))
        specificQuotes.add(quote);
    }
    return specificQuotes;
  }

  private String getRandomQuote() {
    return quotes.get(getRandomInt(quotes.size() - 1));
  }

  private void sendToRoom(MultiUserChat chat, String text) {
    try {
      chat.sendMessage(text);
    } catch (Exception e) {
    }

  }

  public static List<String> readFile(String file) {
    List<String> list = new ArrayList<String>();
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
      String strLine;
      while ((strLine = bufferedReader.readLine()) != null) {
        list.add(strLine);
      }
      bufferedReader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;

  }

  public static int getRandomInt(int max) {
    Random r = new Random(new Date().getTime());
    return r.nextInt(max);
  }

  @Override
  public void execute(Message p, String m, Chat c, ChatQuery q) 
  {
    // noop
  }

  @Override
  public Type getType() { return Type.ACTIVE; }
  
  @Override
  public String getMeta() { return "Quote plugin - Written by Alex - Version: 1.0 - The infamous quote plugin"; }

}
