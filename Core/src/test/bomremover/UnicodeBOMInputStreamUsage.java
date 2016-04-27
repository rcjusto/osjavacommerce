package test.bomremover;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

public final class UnicodeBOMInputStreamUsage {


    public static void main(final String[] args) throws Exception {
        File folder = new File("/media/trabajo/proyectos/stores/version2/resources/static/js/ckeditor");
        Collection l = FileUtils.listFiles(folder, new String[] {"css","js"}, true);
        if (l!=null && !l.isEmpty()) {
            for (Object o : l) {
                if (o instanceof File) removeBOM((File) o);
            }
        }
    }

    private static void removeBOM(File f) {
        if (f.exists() && f.canWrite()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
                InputStreamReader isr = new InputStreamReader(ubis);
                ubis.skipBOM();

                byte[] bytes = IOUtils.toByteArray(isr);

                isr.close();
                ubis.close();
                fis.close();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bytes);
                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

} // UnicodeBOMInputStreamUsage
