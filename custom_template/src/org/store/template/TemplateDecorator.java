package org.store.template;

import org.store.core.globals.BaseAction;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 10/01/12 14:01
 */
public class TemplateDecorator {

    private BaseAction action;
    private TemplateControlList controls;
    private String templateCode;

    public TemplateDecorator(BaseAction action) {
        this.action = action;
        this.templateCode = action.getTemplate();
        this.controls = new TemplateControlList(action.getServletContext());
    }

    public TemplateDecorator(BaseAction action, String templateCode) {
        this.action = action;
        this.templateCode = templateCode;
        this.controls = new TemplateControlList(action.getServletContext());
    }

    public String getHTMLContent(String zone) {
        String template = getVelocityContent(zone);
        if (StringUtils.isNotEmpty(template)) {
            return action.proccessVelocityText(template, null);
        }
        return "";
    }

    public String getVelocityContent(String zone) {
        return action.proccessVelocityTemplate("/templates/"+templateCode+"/zones/"+zone+".vm");
    }

    private List<TemplateBlockElement> blockElements;
    public List<TemplateBlockElement> getZoneContent(String zone) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("zoneInfo",Boolean.TRUE);
        StringBuilder buffer = new StringBuilder("<blocks>");
        buffer.append(action.proccessVelocityTemplate("/templates/" + templateCode + "/zones/" + zone + ".vm", map));
        buffer.append("</blocks>");

        blockElements = new ArrayList<TemplateBlockElement>();
        try {
            Digester digester = new Digester();
            digester.push(this);
            digester.addObjectCreate("blocks/block", TemplateBlockElement.class);
            digester.addSetProperties("blocks/block");
            digester.addSetNext("blocks/block", "addBlock");
            digester.parse(new StringReader(buffer.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return blockElements;
    }
    public void addBlock(TemplateBlockElement tbe) {
        blockElements.add(tbe);
    }
    
    public String[] getLayoutForPage(String zone) {
        return new String[]{"left","right","center"};
    }

    public TemplateControlList getControls() {
        return controls;
    }
}
