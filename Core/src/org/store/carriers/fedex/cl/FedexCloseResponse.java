package org.store.carriers.fedex.cl;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;
import org.apache.commons.codec.binary.Base64;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class FedexCloseResponse {

    public static Logger log = Logger.getLogger(FedexCloseResponse.class);
    private String errorCode;
    private String errorDescription;

    private String filename;
    private String manifest;


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    public FedexCloseResponse(InputStream stream) throws IOException, SAXException {
        /*         FileWriter fw = new FileWriter("d:\\fedex_ship.txt");
            InputStreamReader isr = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                {
                        fw.write(line);
                }
            fw.close();
            br.close();
            isr.close();

        */
        Digester digester = new Digester();
        digester.push(this);

        digester.addCallMethod("FDXCloseReply/Manifest", "addManifest", 2);
        digester.addCallParam("FDXCloseReply/Manifest/FileName", 0);
        digester.addCallParam("FDXCloseReply/Manifest/File", 1);

        digester.addCallMethod("FDXCloseReply/Error", "addError", 2);
        digester.addCallParam("FDXCloseReply/Error/Code", 0);
        digester.addCallParam("FDXCloseReply/Error/Message", 1);

        digester.addCallMethod("Error", "addError", 2);
        digester.addCallParam("Error/Code", 0);
        digester.addCallParam("Error/Message", 1);

        digester.parse(stream);
    }


    public void addManifest(String fn, String ma) {
        filename = fn;
        manifest = ma;
    }

    public void addError(String code, String desc) {
        setErrorCode(code);
        setErrorDescription(desc);
    }

    public boolean hasError() {
        return errorCode != null && !"".equals(errorCode);
    }

    public boolean saveManifest(String fileName) {
        try {
            byte[] encodeBytes = manifest.getBytes();
            byte[] decodeBytes = Base64.decodeBase64(encodeBytes);
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(decodeBytes, 0, decodeBytes.length);
            fos.close();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }



}
