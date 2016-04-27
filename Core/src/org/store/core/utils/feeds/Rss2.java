package org.store.core.utils.feeds;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Rss2 {

    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",new Locale("en"));

    public Rss2() {
    }

    public Rss2(String uri) throws IOException, SAXException {
        if (StringUtils.isNotEmpty(uri)) {
                Digester digester = new Digester();
                digester.push(this);
                
                digester.addCallMethod("rss/channel/item", "addItem", 4);
                digester.addCallParam("rss/channel/item/title", 0);
                digester.addCallParam("rss/channel/item/link", 1);
                digester.addCallParam("rss/channel/item/description", 2);
                digester.addCallParam("rss/channel/item/pubDate", 3);

                digester.parse(uri);
        }
    }

    public void addItem(String t, String l, String d, String p) {
        items.add(new Item(t,l,d,p));
    }

    private List<Item> items = new ArrayList<Item>();

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item {

        public Item() {
        }

        public Item(String title, String link, String description, String pubUpdate) {
            this.title = title;
            this.link = link;
            this.description = description;
            this.pubUpdate = pubUpdate;
        }

        private String title;
        private String link;
        private String description;
        private String pubUpdate;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPubUpdate() {
            return pubUpdate;
        }

        public void setPubUpdate(String pubUpdate) {
            this.pubUpdate = pubUpdate;
        }

        public Date getParsedPubUpdate() {
            if (StringUtils.isNotEmpty(pubUpdate)) {
                try {
                    return sdf.parse(pubUpdate);
                } catch (ParseException ignored) {}
            }
            return null;
        }
    }

}
