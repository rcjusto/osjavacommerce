package org.store.core.utils.lucene;

import org.store.core.beans.Product;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;

public class LuceneUtils {

    public static void reIndex(Session s) {
        FullTextSession fullTextSession = Search.getFullTextSession(s);
        fullTextSession.purgeAll(Product.class);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        ScrollableResults results = fullTextSession.createCriteria(Product.class)
                .setFetchSize(20)
                .scroll(ScrollMode.FORWARD_ONLY);
        int index1 = 0;
        while (results.next()) {
            index1++;
            fullTextSession.index(results.get(0));
            if (index1 % 20 == 0) {
                fullTextSession.flushToIndexes();
                fullTextSession.clear();
            }
        }

    }

}
