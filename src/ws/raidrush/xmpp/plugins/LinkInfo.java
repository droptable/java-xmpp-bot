package ws.raidrush.xmpp.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.jivesoftware.smack.packet.Message;

import ws.raidrush.xmpp.Filter;
import ws.raidrush.xmpp.ManagedMultiUserChat;

import com.tecnick.htmlutils.htmlentities.HTMLEntities;

/**
 * Fetches the title of a board.raidrush.ws thread
 * 
 *
 */
public class LinkInfo extends Filter
{
  public LinkInfo(ManagedMultiUserChat chat) { super(chat);  }

  @Override
  protected void perform(Message msg) 
  {
    String body = msg.getBody();
    
    int pos = body.indexOf("http://");
    
    if (pos == -1)
      pos = body.indexOf("https://");
    
    if (pos == -1) return;
    
    String link = body.substring(pos);
    
    if ((pos = link.indexOf(" ")) > -1) 
      link = link.substring(0, pos);
    
    if (!link.matches("^https?://board\\.raidrush\\.ws(?:/.*)?$"))
      return;
    
    try {
      URL url = new URL(link);
      URLConnection con = url.openConnection();
      
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      String buffer = "";
      
      while ((inputLine = in.readLine()) != null) {        
        buffer += inputLine;
        
        if (inputLine.toLowerCase().contains("</title>"))
          break;
      }
      
      if (buffer.isEmpty())
        return;
      
      int titlePos = buffer.toLowerCase().indexOf("<title");
      if (titlePos == -1) return;
      
      String contentTitle = buffer.substring(titlePos);
      
      titlePos = contentTitle.indexOf(">");
      if (titlePos == -1) return;
      
      contentTitle = contentTitle.substring(titlePos + 1);
      
      titlePos = contentTitle.toLowerCase().indexOf("</title>");
      if (titlePos == -1) return;
      
      contentTitle = contentTitle.substring(0, titlePos);
      contentTitle = HTMLEntities.unhtmlentities(contentTitle);
      
      respond(contentTitle + " (" + link + ")");
    } catch (Exception e) {
      // noop
      return;
    }
    
  }
}

