package org.store.campaigns;

import org.store.campaigns.beans.CampaignUser;
import org.store.core.beans.User;
import org.store.core.globals.LinkUrl;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 25/12/11 16:55
 */
public class CampaignUtils {

    public static Logger log = Logger.getLogger(CampaignUtils.class);

    public static String processCampaignMail(String body) {
        if (StringUtils.isNotEmpty(body)) {
            try {
                StringBuilder buffer = new StringBuilder();
                buffer.append("<html>");
                Source source = new Source(new StringReader(body));
                OutputDocument outputDocument = new OutputDocument(source);
                // Procesar estilos
                List<Element> styleElements = source.getAllElements(HTMLElementName.STYLE);
                if (styleElements != null && !styleElements.isEmpty()) {
                    buffer.append("<head>");
                    for (Element e : styleElements) {
                        buffer.append(e.toString());
                        outputDocument.remove(e);
                    }
                    buffer.append("</head>");
                }

                // Procesar enlaces. agragar el parametro de la campaña
                List<StartTag> linkStartTags = source.getAllStartTags(HTMLElementName.A);
                for (StartTag startTag : linkStartTags) {
                    Map<String, String> map = new HashMap<String, String>();
                    String href = startTag.getAttributeValue("href");
                    String attClass = startTag.getAttributeValue("class");
                    if (StringUtils.isNotEmpty(attClass)) map.put("class", attClass);
                    String attStyle = startTag.getAttributeValue("style");
                    if (StringUtils.isNotEmpty(attStyle)) map.put("style", attStyle);
                    String attId = startTag.getAttributeValue("id");
                    if (StringUtils.isNotEmpty(attId)) map.put("is", attId);
                    LinkUrl url = new LinkUrl(href);
                    url.addParameter("campaign", "[campaignId]");
                    String attHref = url.toString();
                    if (StringUtils.isNotEmpty(attHref)) map.put("href", attHref);
                    outputDocument.replace(startTag.getAttributes(), map);
                }
                StringWriter writer = new StringWriter();
                outputDocument.writeTo(writer);
                buffer.append("<body>");
                buffer.append("<openedTrigger>");
                buffer.append(writer.toString());
                buffer.append("</body></html>");

                return buffer.toString();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private static String[] source = {"[firstname]","[lastname]","[fullname]","[email]","<currentday>","<currentdayname>","<currentmonth>","<currentmonthname>","<currentyear>","<unsubscribe>","</unsubscribe>","<webversion>","</webversion>","[campaignId]","<openedTrigger>"};
    public static String personalizeCampaignMail(String body, User user, CampaignUser cu, String baseUrl ) {
        Calendar cal = Calendar.getInstance();
        String[] target = new String[15];
        target[0] = user.getFirstname();
        target[1] = user.getLastname();
        target[2] = user.getFullName();
        target[3] = user.getEmail();
        target[4] = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        target[5] = format("EEE", cal.getTime());
        target[6] = String.valueOf(cal.get(Calendar.MONTH));
        target[7] = format("MMMM",cal.getTime());
        target[8] = String.valueOf(cal.get(Calendar.YEAR));
        String linkCancel = baseUrl + "useraction.jsp?act=cancelnewsletter&code="+user.getAccessCode();
        target[9] = "<a href=\""+linkCancel+"\">";
        target[10] = "</a>";
        String linkWeb = baseUrl + "campaigncontent.jsp?id=" + ((cu!=null && cu.getIdCampaign()!=null) ? cu.getIdCampaign().toString() : "0");
        target[11] = "<a href=\""+linkWeb+"\">";
        target[12] = "</a>";
        target[13] = (cu!=null && cu.getId()!=null) ? cu.getId().toString() : "0";
        String linkOpened = baseUrl + "campaignopened.jsp?id=" + ((cu!=null && cu.getId()!=null) ? cu.getId().toString() : "0");
        target[14] = "<img src=\""+ linkOpened +"\"/>";
        return StringUtils.replaceEach(body, source, target);
    }

    private static String format(String format, Date date)
    {
        DateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

}
