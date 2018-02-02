package org.store.core.search;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.store.core.beans.Product;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.dao.HibernateDAO;
import org.store.core.globals.SomeUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Rogelio Caballero
 * 18/05/12 14:40
 */
public class LuceneSearcher {

    private String basePath;
    private String language;
    private Set stopWords;

    public LuceneSearcher(String basePath, String language, Set stopWords) {
        this.basePath = basePath;
        this.language = language;
        this.stopWords = (stopWords!=null && !stopWords.isEmpty()) ? stopWords : CustomAnalyzer.STOP_WORDS_SET;
    }

    public List<Product> search(String text, DataNavigator nav, HibernateDAO dao, Long userLevel, int maxResults) {
        List<Product> result = new ArrayList<Product>();
        try {
            File indexFolder = new File(this.basePath + File.separator + "index" + File.separator);
            FSDirectory dir = FSDirectory.open(indexFolder);

            IndexReader ireader = IndexReader.open(dir); // read-only=true
            IndexSearcher isearcher = new IndexSearcher(ireader);
            CustomAnalyzer analyzer = new CustomAnalyzer(Version.LUCENE_31, stopWords);

            BooleanQuery bq = new BooleanQuery();
            bq.add(new TermQuery(new Term("lang", language)), BooleanClause.Occur.MUST);

            // buscar el termino en los campos de texto
            BooleanQuery tq = new BooleanQuery();
            TokenStream ts = analyzer.tokenStream("name", new StringReader(text));
            while (ts.incrementToken()) {
                String s = ts.getAttribute(CharTermAttribute.class).toString();
                if (StringUtils.isNotEmpty(s)) {
                    tq.add(new FuzzyQuery(new Term("name", s), 0.8f), BooleanClause.Occur.SHOULD);
                    tq.add(new FuzzyQuery(new Term("manufacturer", s), 0.8f), BooleanClause.Occur.SHOULD);
                    tq.add(new FuzzyQuery(new Term("desc", s)), BooleanClause.Occur.SHOULD);
                    tq.add(new FuzzyQuery(new Term("keywords", s)), BooleanClause.Occur.SHOULD);
                    tq.add(new TermQuery(new Term("partNumber", s)), BooleanClause.Occur.SHOULD);
                    tq.add(new TermQuery(new Term("mfgPartNumber", s)), BooleanClause.Occur.SHOULD);
                }
            }
            bq.add(tq, BooleanClause.Occur.MUST);

            // adicionar filtro de stock
            String showUnavailable = dao.getStorePropertyValue(StoreProperty.PROP_PRODUCT_SHOW_UNAVAILABLE, StoreProperty.TYPE_GENERAL, StoreProperty.PROP_DEFAULT_PRODUCT_SHOW_UNAVAILABLE);
            if (StoreProperty.PROP_PRODUCT_HAS_STOCK.equalsIgnoreCase(showUnavailable)) bq.add(new TermQuery(new Term("stock", "true")), BooleanClause.Occur.MUST);
            else if (StoreProperty.PROP_PRODUCT_HAS_STOCK_OR_ETA.equalsIgnoreCase(showUnavailable)) {
                BooleanQuery query1 = new BooleanQuery();
                query1.add(new TermQuery(new Term("stock", "true")), BooleanClause.Occur.SHOULD);
                query1.add(new TermQuery(new Term("eta", "true")), BooleanClause.Occur.SHOULD);
                bq.add(query1, BooleanClause.Occur.MUST);
            }

            //adicionar filtro de usuario
            if (userLevel != null) {
                BooleanQuery query1 = new BooleanQuery();
                query1.add(new TermQuery(new Term("forUser", "l" + userLevel.toString() + "l")), BooleanClause.Occur.SHOULD);
                query1.add(new TermQuery(new Term("forUser", "public")), BooleanClause.Occur.SHOULD);
                bq.add(query1, BooleanClause.Occur.MUST);
            } else bq.add(new TermQuery(new Term("forUser", "public")), BooleanClause.Occur.MUST);

            ScoreDoc[] hits = isearcher.search(bq, maxResults).scoreDocs;
            nav.setTotalRows(hits.length);
            for (int i=nav.getFirstRow()-1; i<nav.getLastRow(); i++) {
                Document hitDoc = isearcher.doc(hits[i].doc);
                Product p = (Product) dao.get(Product.class, SomeUtils.strToLong(hitDoc.get("id")));
                if (p!=null) {
                     result.add(p);
                }
            }
            isearcher.close();
            ireader.close();
            dir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
