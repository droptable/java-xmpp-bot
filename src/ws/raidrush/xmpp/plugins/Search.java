package ws.raidrush.xmpp.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Message;

import ws.raidrush.xmpp.Command;
import ws.raidrush.xmpp.ManagedMultiUserChat;

import com.tecnick.htmlutils.htmlentities.HTMLEntities;

/**
 * Performs a search at board.raidrush.ws
 * 
 *
 */
public class Search extends Command
{
  public Search(ManagedMultiUserChat chat)  { super(chat); }
  
  @Override
  protected void perform(Message msg, String body) 
  {
    try {
      URL                 url  = new URL("http://board.raidrush.ws/search.php?do=process");
      URLConnection       conn = url.openConnection();
      conn.setDoOutput(true);
      
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      
      String data = "s=1&do=process&&sortby=lastpost&order=descending&query=" + URLEncoder.encode(body, "UTF-8"); 
      
      wr.write(data);
      wr.flush();
      
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      
      String line;
      StringBuilder build = new StringBuilder();
      
      while ((line = rd.readLine()) != null)
        build.append(line);
      
      String result = build.toString();
      int start = result.indexOf(" id=\"thread_title_");
      
      if (start == -1) {
        respond("nothing found");
        return;
      }
      
      String res = result.substring(start + 18);
      res = res.substring(0, res.indexOf("\""));
      
      String title = result.substring(start + 18);
      title = title.substring(0, title.indexOf("<"));
      title = title.substring(title.indexOf(">") + 1);
      title = HTMLEntities.unhtmlentities(title);
      
      respond(title.trim() + " -> http://board.raidrush.ws/showthread.php?t=" + res);
    } catch (Exception e) {
      Logger.getRootLogger().error("Error while executing command", e);
    }
  }
}
