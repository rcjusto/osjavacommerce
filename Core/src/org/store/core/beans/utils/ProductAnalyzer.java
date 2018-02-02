package org.store.core.beans.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.SnowballPorterFilterFactory;
import org.apache.solr.analysis.SynonymFilterFactory;
import org.hibernate.search.util.HibernateSearchResourceLoader;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Feb 12, 2010
 */
public class ProductAnalyzer extends Analyzer {
    public static Logger log = Logger.getLogger(ProductAnalyzer.class);

    private Map<String, Set> stopSet;
    private SynonymFilterFactory synonymFilterFactory;
    private Map<String, SnowballPorterFilterFactory> snowballPorterFilterFactoryMap;

    /**
     * Specifies whether deprecated acronyms should be replaced with HOST type.
     * This is false by default to support backward compatibility.
     *
     * @deprecated this should be removed in the next release (3.0).
     *             <p/>
     *             See https://issues.apache.org/jira/browse/LUCENE-1068
     */
    private boolean replaceInvalidAcronym = defaultReplaceInvalidAcronym;

    private static boolean defaultReplaceInvalidAcronym;

    // Default to true (fixed the bug), unless the system prop is set

    static {
        final String v = System.getProperty("org.apache.lucene.analysis.standard.StandardAnalyzer.replaceInvalidAcronym");
        if (v == null || v.equals("true"))
            defaultReplaceInvalidAcronym = true;
        else
            defaultReplaceInvalidAcronym = false;
    }

    /**
     * @return true if new instances of StandardTokenizer will
     *         replace mischaracterized acronyms
     *         <p/>
     *         See https://issues.apache.org/jira/browse/LUCENE-1068
     * @deprecated This will be removed (hardwired to true) in 3.0
     */
    public static boolean getDefaultReplaceInvalidAcronym() {
        return defaultReplaceInvalidAcronym;
    }

    /**
     * @param replaceInvalidAcronym Set to true to have new
     *                              instances of StandardTokenizer replace mischaracterized
     *                              acronyms by default.  Set to false to preseve the
     *                              previous (before 2.4) buggy behavior.  Alternatively,
     *                              set the system property
     *                              org.apache.lucene.analysis.standard.StandardAnalyzer.replaceInvalidAcronym
     *                              to false.
     *                              <p/>
     *                              See https://issues.apache.org/jira/browse/LUCENE-1068
     * @deprecated This will be removed (hardwired to true) in 3.0
     */
    public static void setDefaultReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
        defaultReplaceInvalidAcronym = replaceInvalidAcronym;
    }


    /**
     * An array containing some common English words that are usually not
     * useful for searching.
     */
    public static final String[] STOP_WORDS_EN = {
            "a", "ante", "bajo", "con", "contra", "de ", "desde", "en", "entre", "es", "este", "la", "lo", "no", "para", "por", "sin", "sobre", "y", "o", "el", "ella"
    };
    public static final String[] STOP_WORDS_ES = {
            "a", "ante", "bajo", "con", "contra", "de ", "desde", "en", "entre", "es", "este", "la", "lo", "no", "para", "por", "sin", "sobre", "y", "o", "el", "ella"
    };

    public void createSynonymFilterFactory() {
        if (synonymFilterFactory == null) {
            synonymFilterFactory = new SynonymFilterFactory();
            Map<String, String> synMap = new HashMap<String, String>();
            synMap.put("synonyms", "diccionario.txt");
            try {
                synonymFilterFactory.init(synMap);
                synonymFilterFactory.inform(new HibernateSearchResourceLoader());

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void createSnowballPorterFilterFactoryMap() {
        if (snowballPorterFilterFactoryMap == null) {
            snowballPorterFilterFactoryMap = new HashMap<String, SnowballPorterFilterFactory>();
            for (String lang : new String[]{"en", "es", "fr"}) {
                SnowballPorterFilterFactory snowballPorterFilterFactory = new SnowballPorterFilterFactory();
                Map<String, String> synMap = new HashMap<String, String>();
                if ("es".equalsIgnoreCase(lang)) synMap.put("language", "Spanish");
                else if ("fr".equalsIgnoreCase(lang)) synMap.put("language", "French");
                else synMap.put("language", "English");
                snowballPorterFilterFactory.init(synMap);
                snowballPorterFilterFactoryMap.put(lang, snowballPorterFilterFactory);
            }
        }
    }

    /**
     * Builds an analyzer with the default stop words.
     */
    public ProductAnalyzer() {
        stopSet = new HashMap<String,Set>();
        stopSet.put("en",new HashSet(Arrays.asList(STOP_WORDS_EN)));
        stopSet.put("es",new HashSet(Arrays.asList(STOP_WORDS_ES)));
        stopSet.put("fr",new HashSet(Arrays.asList(STOP_WORDS_ES)));
    //    createSynonymFilterFactory();
        createSnowballPorterFilterFactoryMap();
    }

    /**
     * Constructs a {@link StandardTokenizer} filtered by a {@link
     * StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}.
     */
    public TokenStream tokenStream(String fieldName, Reader reader) {
        StandardTokenizer tokenStream = new StandardTokenizer(Version.LUCENE_31, reader);
        tokenStream.setMaxTokenLength(maxTokenLength);
        TokenStream result = new StandardFilter(tokenStream);
        if (fieldName.startsWith("name_")) {
            String lang = StringUtils.substringAfter(fieldName, "_");
            result = new ISOLatin1AccentFilter(result);
            result = new LowerCaseFilter(result);
        //    result = synonymFilterFactory.create(result);
            if (snowballPorterFilterFactoryMap.containsKey(lang))
                result = snowballPorterFilterFactoryMap.get(lang).create(result);
            if (stopSet.containsKey(lang))
                result = new StopFilter(Version.LUCENE_31, result, stopSet.get(lang));
        }
        return result;
    }

    private static final class SavedStreams {
        StandardTokenizer tokenStream;
        TokenStream filteredTokenStream;
    }

    /**
     * Default maximum allowed token length
     */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

    /**
     * Set maximum allowed token length.  If a token is seen
     * that exceeds this length then it is discarded.  This
     * setting only takes effect the next time tokenStream or
     * reusableTokenStream is called.
     */
    public void setMaxTokenLength(int length) {
        maxTokenLength = length;
    }

    /**
     * @see #setMaxTokenLength
     */
    public int getMaxTokenLength() {
        return maxTokenLength;
    }

    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        SavedStreams streams = (SavedStreams) getPreviousTokenStream();
        if (streams == null) {
            streams = new SavedStreams();
            setPreviousTokenStream(streams);
            streams.tokenStream = new StandardTokenizer(Version.LUCENE_31, reader);
            streams.filteredTokenStream = new StandardFilter(streams.tokenStream);
            if (fieldName.startsWith("name_")) {
                String lang = StringUtils.substringAfter(fieldName, "_");
                streams.filteredTokenStream = new ISOLatin1AccentFilter(streams.filteredTokenStream);
                streams.filteredTokenStream = new LowerCaseFilter(streams.filteredTokenStream);
             //   streams.filteredTokenStream = synonymFilterFactory.create(streams.filteredTokenStream);
                if (snowballPorterFilterFactoryMap.containsKey(lang))
                    streams.filteredTokenStream = snowballPorterFilterFactoryMap.get(lang).create(streams.filteredTokenStream);
                if (stopSet.containsKey(lang))
                    streams.filteredTokenStream = new StopFilter(Version.LUCENE_31, streams.filteredTokenStream, stopSet.get(lang));
            }
        } else {
            streams.tokenStream.reset(reader);
        }
        streams.tokenStream.setMaxTokenLength(maxTokenLength);

        streams.tokenStream.setReplaceInvalidAcronym(replaceInvalidAcronym);

        return streams.filteredTokenStream;
    }

    private String getLuceneLanguage() {
        return "en";
    }

    /**
     * @return true if this Analyzer is replacing mischaracterized acronyms in the StandardTokenizer
     *         <p/>
     *         See https://issues.apache.org/jira/browse/LUCENE-1068
     * @deprecated This will be removed (hardwired to true) in 3.0
     */
    public boolean isReplaceInvalidAcronym() {
        return replaceInvalidAcronym;
    }

    /**
     * @param replaceInvalidAcronym Set to true if this Analyzer is replacing mischaracterized acronyms in the StandardTokenizer
     *                              <p/>
     *                              See https://issues.apache.org/jira/browse/LUCENE-1068
     * @deprecated This will be removed (hardwired to true) in 3.0
     */
    public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
        this.replaceInvalidAcronym = replaceInvalidAcronym;
    }
}
