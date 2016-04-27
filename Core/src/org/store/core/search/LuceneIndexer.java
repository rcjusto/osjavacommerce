package org.store.core.search;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.store.core.beans.Product;
import org.store.core.beans.ProductLang;

import java.io.File;
import java.io.IOException;

/**
 * Rogelio Caballero
 * 17/05/12 19:47
 */
public class LuceneIndexer {

    private String basePath;
    private String[] languages;
    private String defLanguage;

    public LuceneIndexer(String basePath, String[] languages, String defLanguage) {
        this.basePath = basePath;
        this.languages = languages;
        this.defLanguage = defLanguage;
    }

    public void indexProduct(Product product, boolean isNew) {
        LuceneIndexer.indexProduct(product, isNew, this.basePath, languages, defLanguage);
    }
    
    public void deleteProduct(Product product) {
        try {
            File indexFolder = new File(this.basePath + File.separator + "index" + File.separator);
            FileUtils.forceMkdir(indexFolder);
            FSDirectory dir = FSDirectory.open(indexFolder);

            CustomAnalyzer analyzer = new CustomAnalyzer(Version.LUCENE_31);
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_31, analyzer);
            Term term = new Term("id", product.getIdProduct().toString());
            IndexWriter indexWriter = new IndexWriter(dir, config);
            indexWriter.deleteDocuments(term);
            indexWriter.close();

            optimizeIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void optimizeIndex() throws IOException {
        File indexFolder = new File(this.basePath + File.separator + "index" + File.separator);
        FSDirectory dir = FSDirectory.open(indexFolder);
        CustomAnalyzer analyzer = new CustomAnalyzer(Version.LUCENE_31);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_31, analyzer);
        IndexWriter indexWriter = new IndexWriter(dir, config);
        indexWriter.optimize();
        indexWriter.close();
    }

    public static void indexProduct(Product product, boolean isNew, String basePath, String[] languages, String defLanguage) {
        try {
            File indexFolder = new File(basePath + File.separator + "index" + File.separator);
            FileUtils.forceMkdir(indexFolder);
            FSDirectory dir = FSDirectory.open(indexFolder);

            // eliminar documento viejo
            if (!isNew) {
                Term term = new Term("id", product.getIdProduct().toString());
                CustomAnalyzer analyzer = new CustomAnalyzer(Version.LUCENE_31);
                IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_31, analyzer);
                IndexWriter writer = new IndexWriter(dir, config);
                writer.deleteDocuments(term);
                writer.close();
            }

            // crear documento nuevo
            if (product.getActive() && !product.getArchived() && !Product.TYPE_COMPLEMENT.equalsIgnoreCase(product.getProductType())) {
                CustomAnalyzer analyzer = new CustomAnalyzer(Version.LUCENE_31);
                IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_31, analyzer);
                IndexWriter writer = new IndexWriter(dir, config);
                for (String lang : languages) {
                    ProductLang pl = product.getLanguage(lang, defLanguage);
                    if (pl != null) {
                        Document doc = new Document();
                        doc.add(new Field("id", product.getIdProduct().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                        doc.add(new Field("lang", lang, Field.Store.YES, Field.Index.NOT_ANALYZED));
                        doc.add(new Field("partNumber", (StringUtils.isNotEmpty(product.getPartNumber())) ? product.getPartNumber().toLowerCase() : "", Field.Store.YES, Field.Index.NOT_ANALYZED));
                        doc.add(new Field("mfgPartNumber", (StringUtils.isNotEmpty(product.getMfgPartnumber())) ? product.getMfgPartnumber().toLowerCase() : "", Field.Store.YES, Field.Index.NOT_ANALYZED));
                        doc.add(new Field("stock", (product.getStock() > 0) ? "true" : "false", Field.Store.YES, Field.Index.NOT_ANALYZED));
                        doc.add(new Field("eta", StringUtils.isNotEmpty(product.getEta()) ? "true" : "false", Field.Store.YES, Field.Index.NOT_ANALYZED));

                        Field fieldManufacturer = new Field("manufacturer", (product.getManufacturer() != null) ? product.getManufacturer().getManufacturerName() : "", Field.Store.YES, Field.Index.ANALYZED);
                        fieldManufacturer.setBoost(3);
                        doc.add(fieldManufacturer);

                        StringBuilder userLevels = new StringBuilder();
                        if (product.getForUsers() != null && !product.getForUsers().isEmpty()) {
                            for (Long lid : product.getForUsers()) userLevels.append("l").append(lid).append("l ");
                        } else {
                            userLevels.append("public");
                        }
                        doc.add(new Field("forUser", userLevels.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));

                        Field fieldName = new Field("name", (StringUtils.isNotEmpty(pl.getProductName())) ? pl.getProductName() : "", Field.Store.YES, Field.Index.ANALYZED);
                        fieldName.setBoost(4);
                        doc.add(fieldName);

                        doc.add(new Field("keywords", (StringUtils.isNotEmpty(product.getSearchKeywords())) ? product.getSearchKeywords() : "", Field.Store.YES, Field.Index.ANALYZED));

                        StringBuilder buff = new StringBuilder("");
                        if (StringUtils.isNotEmpty(pl.getDescription())) buff.append(pl.getDescription()).append(" ");
                        if (StringUtils.isNotEmpty(pl.getFeatures())) buff.append(pl.getFeatures()).append(" ");
                        if (StringUtils.isNotEmpty(pl.getInformation())) buff.append(pl.getInformation()).append(" ");
                        doc.add(new Field("desc", buff.toString(), Field.Store.YES, Field.Index.ANALYZED));

                        writer.addDocument(doc);
                    }
                }
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
