package org.store.core.globals;


import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.TextExtractor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.struts2.json.JSONUtil;
import org.store.core.beans.StoreProperty;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Apr 20, 2010
 */
public class SomeUtils {

    public static Logger log = Logger.getLogger(SomeUtils.class);
    private static SimpleDateFormat dateFormatEn = new SimpleDateFormat("MM/dd/yyyy");
    private static SimpleDateFormat dateFormatEs = new SimpleDateFormat("dd/MM/yyyy");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");


    public static List<String> getLines(String cad) {
        if (StringUtils.isEmpty(cad)) return null;
        List<String> res = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new StringReader(cad));
        String str;
        try {
            while ((str = reader.readLine()) != null) {
                res.add(str);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e); 
        }
        return res;
    }

    /**
     * Convierte un string a Double, si no puede devuelve null.
     *
     * @param cad s
     * @return BigDecimal
     */
    public static Double strToDouble(String cad) {
        Double res = null;
        try {
            res = NumberUtils.createDouble(cad);
        } catch (Exception ignored) {
        }
        return res;
    }

    /**
     * Convierte un string a Double a como sea.
     *
     * @param cad e
     * @return BigDecimal
     */
    public static Double forceStrToDouble(String cad) {
        Double res;
        int posC = cad.indexOf(",");
        int posP = cad.indexOf(".");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        if (posC > -1 && posP > -1) {
            if (posC < posP) {
                dfs.setDecimalSeparator('.');
                dfs.setGroupingSeparator(',');
            } else {
                dfs.setDecimalSeparator(',');
                dfs.setGroupingSeparator('.');
            }
        } else if (posP > -1) {
            dfs.setDecimalSeparator('.');
        } else if (posC > -1) {
            dfs.setDecimalSeparator(',');
        }
        DecimalFormat df = new DecimalFormat("0,00", dfs);
        try {
            res = df.parse(cad).doubleValue();
        } catch (Exception e1) {
            res = 0.0d;
        }
        return res;
    }

    /**
     * Convierte un string a Integer, si no puede devuelve null.
     *
     * @param cad s
     * @return Integer
     */
    public static Integer strToInteger(String cad) {
        Integer res = null;
        if (StringUtils.isNotEmpty(cad))
            try {
                res = NumberUtils.createInteger(cad);
            } catch (Exception ignored) {
            }

        return res;
    }

    public static Integer toInteger(Object cad) {
        if (cad==null) return null;
        if (cad instanceof String) return strToInteger((String)cad);
        if (cad instanceof Number) return ((Number)cad).intValue();
        return null;
    }

    /**
     * Convierte un string a Integer, si no puede devuelve null.
     *
     * @param cad s
     * @return BigDecimal
     */
    public static Long strToLong(String cad) {
        Long res = null;
        try {
            res = NumberUtils.createLong(cad);
        } catch (Exception ignored) {
        }
        return res;
    }

    public static Long[] strToLong(String[] cad) {
        Long[] res = new Long[0];
        if (cad == null || cad.length < 1) return null;
        List<Long> lista = new ArrayList<Long>();
        for (String aCad : cad) {
            Long v = SomeUtils.strToLong(aCad);
            if (v != null) lista.add(v);
        }
        return lista.toArray(res);
    }

    public static Date strToDate(String cad, String lang) {
        if (cad == null) {
            return null;
        }

        Date result;

        try {
            SimpleDateFormat df = ("es".equalsIgnoreCase(lang)) ? dateFormatEs : dateFormatEn;
            result = df.parse(cad);
        } catch (ParseException e) {
            result = null;
        }

        return result;
    }

    public static String dateToStr(Date d, String lang) {
        SimpleDateFormat df = ("es".equalsIgnoreCase(lang)) ? dateFormatEs : dateFormatEn;
        return (d != null) ? df.format(d) : null;
    }

    public static Date strToTime(String cad) {
        if (cad == null) {
            return null;
        }
        Date result;
        try {
            result = timeFormat.parse(cad);
        } catch (ParseException e) {
            result = null;
        }

        return result;
    }

    public static String formatDate(Date d, String lang) {
        SimpleDateFormat df = ("es".equalsIgnoreCase(lang)) ? dateFormatEs : dateFormatEn;
        return (d != null) ? df.format(d) : null;
    }

    public static String formatTime(Date d) {
        return (d != null) ? timeFormat.format(d) : null;
    }

    public static Date today() {
        return Calendar.getInstance().getTime();
    }

    public static String replaceForUrl(String name) {
        if (name == null || "".equals(name.trim())) return null;
        String codeName = null;
        try {
            codeName = slugify(name);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e); 
        }
        return codeName;
    }

    public static String slugify(String input) throws UnsupportedEncodingException {
        if (input == null || input.length() == 0) return "";
        String toReturn = normalize(input);
        toReturn = toReturn.replace(",", "");
        toReturn = toReturn.replace(";", "");
        toReturn = toReturn.replace(":", "");
        toReturn = toReturn.replace("'", "");
        toReturn = toReturn.replace("@", "");
        toReturn = toReturn.replace("#", "");
        toReturn = toReturn.replace("/", "");
        toReturn = toReturn.replace("%", "porcent");
        toReturn = toReturn.replace("\"", "");
        toReturn = toReturn.replace("&", "and");
        toReturn = toReturn.replace("+", "plus");
        toReturn = toReturn.replace("(", "");
        toReturn = toReturn.replace(")", "");
        toReturn = toReturn.replace("[", "");
        toReturn = toReturn.replace("]", "");
        toReturn = toReturn.replace("{", "");
        toReturn = toReturn.replace("}", "");
        toReturn = toReturn.replace("|", "");
        toReturn = toReturn.replace("!", "");
        toReturn = toReturn.replace("�", "");
        toReturn = toReturn.replace("�", "");
        toReturn = toReturn.replace("?", "");
        toReturn = toReturn.replace(" ", "-");
        toReturn = toReturn.replace(".", "-");
        toReturn = toReturn.replace("---", "-");
        toReturn = toReturn.replace("--", "-");
        if (toReturn.endsWith("-")) toReturn = toReturn.substring(0, toReturn.length() - 1);
        //      toReturn = toReturn.toLowerCase();
        toReturn = URLEncoder.encode(toReturn, "UTF-8");
        return toReturn;
    }

    private static String normalize(String input) {
        if (input == null || input.length() == 0) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public static Date dateIni(Date d) {
        Calendar cal = Calendar.getInstance();
        if (d != null) cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date dateEnd(Date d) {
        Calendar cal = Calendar.getInstance();
        if (d != null) cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static long dayDiff(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        if (d1 != null) c1.setTime(d1);
        c1.set(Calendar.HOUR, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        Calendar c2 = Calendar.getInstance();
        if (d2 != null) c2.setTime(d2);
        c2.set(Calendar.HOUR, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);
        return (c2.getTimeInMillis() - c1.getTimeInMillis()) / (24 * 60 * 60 * 1000);
    }

    public static long hourDiff(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        if (d1 != null) c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        if (d2 != null) c2.setTime(d2);
        return (c2.getTimeInMillis() - c1.getTimeInMillis()) / (60 * 60 * 1000);
    }

    public static String join(List<String> lista, String sep) {
        StringBuilder buff = new StringBuilder();
        if (lista != null && !lista.isEmpty() && StringUtils.isNotEmpty(sep)) {
            for (String cad : lista) {
                if (StringUtils.isNotEmpty(buff.toString())) buff.append(sep);
                if (StringUtils.isNotEmpty(cad)) buff.append(cad);
                else buff.append("");
            }
        }
        return buff.toString();
    }


    public static String encrypt3Des(String message, String myKey) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(myKey.getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8; ) {
            keyBytes[k++] = keyBytes[j++];
        }
        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        final byte[] plainTextBytes = message.getBytes("utf-8");
        final byte[] cipherText = cipher.doFinal(plainTextBytes);
        return new String(Hex.encodeHex(cipherText));
    }

    public static String decrypt3Des(String message, String myKey) throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(myKey.getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8; ) {
            keyBytes[k++] = keyBytes[j++];
        }
        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        decipher.init(Cipher.DECRYPT_MODE, key, iv);
        final byte[] plainText = decipher.doFinal(Hex.decodeHex(message.toCharArray()));
        return new String(plainText, "UTF-8");
    }

    public static String extractText(String s) {
        if (StringUtils.isEmpty(s)) return "";
        Source source = new Source(s.replace("\"",""));
        TextExtractor te = new TextExtractor(source) {
            @Override
            public boolean excludeElement(StartTag startTag) {
                return HTMLElementName.IMG.equalsIgnoreCase(startTag.getName()) || HTMLElementName.INPUT.equalsIgnoreCase(startTag.getName()) || HTMLElementName.BUTTON.equalsIgnoreCase(startTag.getName());
            }
        };
        return te.toString();
    }

    public static String extractKeywords(String text) {
        if (StringUtils.isEmpty(text)) return "";
        StringBuilder buff = new StringBuilder();
        StandardAnalyzer sa = new StandardAnalyzer(org.apache.lucene.util.Version.LUCENE_31);
        TokenStream ts = sa.tokenStream("pepe", new StringReader(text));
        try {
            while (ts.incrementToken()) {
                String s = ts.getAttribute(CharTermAttribute.class).toString();
                if (StringUtils.isNotEmpty(s)) buff.append(s).append(", ");
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return buff.toString();
    }

    public static String formatFileSize(long size) {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(1);

        StringBuilder buffer = new StringBuilder();

        if (size < 1024) {
            buffer.append(formatter.format(size));
            buffer.append(" B");
        } else if (size < 1048576) {
            buffer.append(formatter.format(size / 1024.0));
            buffer.append(" KB");
        } else {
            buffer.append(formatter.format(size / 1048576.0));
            buffer.append(" MB");
        }

        return buffer.toString();
    }

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) {
        String result = "";
        try {
            byte[] b = createChecksum(filename);
            for (byte aB : b)
                result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        return result;
    }

    public static boolean reCaptcha2(String secret, String ip, String response) {
        if (StringUtils.isNotEmpty(secret)) {
            boolean isHuman = false;
            PostMethod post = new PostMethod(StoreProperty.RECAPTCHA_URL);
            post.addParameter("secret", secret);
            post.addParameter("response", StringUtils.isNotEmpty(response) ? response : "");
            post.addParameter("remoteip", StringUtils.isNotEmpty(ip) ? ip : "");
            HttpClient httpclient = new HttpClient();
            try {
                httpclient.executeMethod(post);
                String res = post.getResponseBodyAsString();
                Map json = (Map) JSONUtil.deserialize(res);
                if (json.containsKey("success")) {
                    isHuman = (Boolean) json.get("success");
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
                isHuman = false;
            } finally {
                post.releaseConnection();
            }
            return isHuman;
        }
        return true;
    }

}
