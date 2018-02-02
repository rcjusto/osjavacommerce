package test;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.html.simpleparser.StyleSheet;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.XfaForm;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.Node;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ccil.cowan.tagsoup.Parser;
import org.geonames.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.ImageResolverImpl;
import org.store.core.globals.SomeUtils;
import org.store.icecat.RealTimeRequest;
import org.store.icecat.RealTimeResponse;
import org.xhtmlrenderer.css.newmatch.Condition;
import org.xhtmlrenderer.css.newmatch.Selector;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio
 * Date: 11/09/2010
 * Time: 01:38:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyUtils {

    public static void main(String[] args) {
        /*
        File image = new File("D:\\proyectos\\stores\\version2\\web\\store\\uploads\\IMG_2736.JPG");
        processImage(image);
        try {
            FileUtils.forceDelete(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
          */
        try {
            test();
            // countries();
            //invoice();
            // otro();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void test() {




    }

    private static void countries() {
        List<CountryFactory.Country> list = CountryFactory.getCountries(new Locale("fr"), true);
        for(CountryFactory.Country c : list) {
            System.out.print("'"+c.getCode()+"'=>'"+c.getName()+"',");
        }
    }
    
    private static void cssparser() {
        File file = new File("/media/trabajo/proyectos/stores/version2/web/dewlexus/skins/default/css/site.css");
        Map map = TemplateCssParser1.parseCss(file, "http://prueba.com/");
        JSONObject json = new JSONObject(map);
        try {
            System.out.print(json.toString(8));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static String getSelectorName(Selector selector) {
        StringBuilder buff = new StringBuilder();
        if (selector != null) {
            if (StringUtils.isNotEmpty(selector.getName())) buff.append(selector.getName());
            if (selector.getConditions() != null && !selector.getConditions().isEmpty()) {
                for (Object o : selector.getConditions()) {
                    if (o instanceof Condition) {
                        buff.append(((Condition) o).getValue());
                    }
                }
            }
            if (selector.isPseudoClass(Selector.HOVER_PSEUDOCLASS)) buff.append(":hover");
            if (selector.isPseudoClass(Selector.VISITED_PSEUDOCLASS)) buff.append(":visited");
            if (selector.isPseudoClass(Selector.FOCUS_PSEUDOCLASS)) buff.append(":focus");
            if (selector.getChainedSelector() != null) {
                buff.append(" ").append(getSelectorName(selector.getChainedSelector()));
            }
        }
        return buff.toString();
    }


    public static void js() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("banner", "action1,action2,action3");
            obj.put("otro", "action4,action5,action6");
            System.out.println(obj.toString());
        } catch (org.json.JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public static void tt() {
        try {
            Parser tagsoupParser = new org.ccil.cowan.tagsoup.Parser();
            XmlSlurper xml = new XmlSlurper(tagsoupParser);
            GPathResult html = xml.parse(new File("/media/trabajo/proyectos/stores/version2/doc/parsers/1973.html"));
            Iterator it = html.childNodes();
            while (it.hasNext()) {
                Node n = (Node) it.next();
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void otro() {
        String[] roots = new String[]{"/media/trabajo/proyectos/stores/version2/web/WEB-INF/script/processing"};
        try {
            GroovyScriptEngine gse = new GroovyScriptEngine(roots);
            Binding binding = new Binding();
            gse.run("prueba1.groovy", binding);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void invoice1() {
        try {
            PdfReader reader = new PdfReader("/media/trabajo/proyectos/stores/version2/doc/invoice.pdf");
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("/media/trabajo/proyectos/stores/version2/doc/salida.pdf"));
            AcroFields form = stamper.getAcroFields();
            XfaForm xfa = form.getXfa();

            xfa.fillXfaForm(new InputSource(new FileReader("/media/trabajo/proyectos/stores/version2/doc/order.xml")));
            stamper.close();
        } catch (DocumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static void invoice() {

        try {

            String htmlTextForPDF = FileUtils.readFileToString(new File("/tmp/prueba.html"));
            OutputStream os = new FileOutputStream("/tmp/prueba.pdf");
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlTextForPDF);
            renderer.layout();
            renderer.createPDF(os);
            os.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void addCssTagStyle(StyleSheet styles, String tag, String propName, String propValue) {
        if ("color".equalsIgnoreCase(propName)) styles.loadTagStyle(tag, "color", propValue);
        if ("background-color".equalsIgnoreCase(propName)) styles.loadTagStyle(tag, "bgcolor", propValue);
        if ("border".equalsIgnoreCase(propName)) styles.loadTagStyle(tag, "border", propValue);

    }

    public static void processImage(File image) {
        BufferedImage inImage = null;
        try {
            int width = 0;
            int height = 0;

            inImage = javax.imageio.ImageIO.read(image);
            width = inImage.getWidth();
            height = inImage.getHeight();

            BufferedImage outImage = ImageResolverImpl.resize(inImage, 160, 120, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            writeJPEG(outImage, "D:\\proyectos\\stores\\version2\\web\\store\\uploads\\prueba.JPG", 0.75f);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeJPEG(BufferedImage input, String name, Float quality) throws IOException {
        Iterator iter = ImageIO.getImageWritersByFormatName("JPG");
        if (iter.hasNext()) {
            ImageWriter writer = (ImageWriter) iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality);
            File outFile = new File(name);
            FileImageOutputStream output = new FileImageOutputStream(outFile);
            writer.setOutput(output);
            IIOImage image = new IIOImage(input, null, null);
            writer.write(null, image, iwp);
            output.close();
        }
    }

    public static void readCountryFile() throws Exception, InvalidParameterException {

        String cad = FileUtils.readFileToString(new File("/media/trabajo/provinces.json"));
        JSONObject obj = new JSONObject(cad);
        JSONObject oEs = obj.has("ES") ? (JSONObject) obj.get("ES") : null;
        if (oEs != null) System.out.println(oEs);

        /*
        List<String> list = FileUtils.readLines(new File("/media/trabajo/paises.txt"));
        JSONObject mapAll = new JSONObject();
        Map<String,String> currentCountryMap = null;
        String currentCountryCode = null;
        for(String s : list) {
            if (StringUtils.indexOf(s,":")<0) {
                if (currentCountryMap!=null && StringUtils.isNotEmpty(currentCountryCode)) {
                    mapAll.put(currentCountryCode, currentCountryMap);
                }
                currentCountryMap = new HashMap<String,String>();
                currentCountryCode = s;
            } else if (currentCountryMap!=null) {
                String[] arr = StringUtils.split(s, ":");
                if (arr.length>1) currentCountryMap.put(arr[0],arr[1]);
            }
        }
        System.out.println(mapAll.toString());
        */
    }

    public static void geoName() throws Exception, InvalidParameterException {
        StringBuffer buff = new StringBuffer();
        List<CountryFactory.Country> list = CountryFactory.getCountries(new Locale("en"));
        List<String> listStr = new ArrayList<String>();
        int index = 0;
        for (CountryFactory.Country c : list) {
            System.out.println("Descargando " + (index++) + " de " + list.size());
            listStr.add(c.getCode());
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setCountryCode(c.getCode());
            searchCriteria.setFeatureClass(FeatureClass.A);
            searchCriteria.setFeatureCode("ADM1");
            searchCriteria.setStyle(Style.FULL);
            searchCriteria.setLanguage("en");
            ToponymSearchResult searchResult = WebService.search(searchCriteria);
            for (Toponym toponym : searchResult.getToponyms()) {
                listStr.add(toponym.getAdminCode1() + ":" + toponym.getAdminName1());
            }
        }
        FileUtils.writeLines(new File("/media/trabajo/paises.txt"), listStr);
    }

    public static void downloadZip() throws Exception {
        try {
            URL url = new URL("http://ip-to-country.webhosting.info/downloads/ip-to-country.csv.zip");
            ZipInputStream zin = new ZipInputStream(url.openStream());
            ZipEntry ze = zin.getNextEntry();
            System.out.println("Compresed: " + ze.getCompressedSize());
            System.out.println("Uncompresed: " + ze.getSize());
            FileOutputStream fos = new FileOutputStream("/media/trabajo/bd-ips.csv");
            byte data[] = new byte[1024];
            int count, total = 0;
            while ((count = zin.read(data, 0, 1024)) != -1) {
                fos.write(data, 0, count);
                total += count;
                System.out.println(total);
            }
            System.out.println("COMPLETO Bajado " + total);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void icecat() throws Exception {

        RealTimeRequest req = new RealTimeRequest();
        req.setLanguage("es");
        req.setManufacturer("Sony");
        req.setProductId("DSCTX9R.CEE8");
        RealTimeResponse resp = req.execute();
        if (resp != null) System.out.println(resp.getLongDesc());
    }

    public static void numberParser() throws Exception {
        String[] arr = new String[]{"123.12", "123.123,34", "4,324.3", "54345,7"};
        for (String cad : arr)
            System.out.println(cad + " -> " + SomeUtils.forceStrToDouble(cad));
    }

}
