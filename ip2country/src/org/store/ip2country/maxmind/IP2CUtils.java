package org.store.ip2country.maxmind;

import org.store.core.beans.Job;
import org.store.core.globals.SomeUtils;
import org.store.core.utils.quartz.JobStoreThread;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Rogelio Caballero
 * 24/07/11 0:26
 */
public class IP2CUtils extends JobStoreThread {

    public static final String TMP_NAME = "/WEB-INF/GeoIP.tmp";
    public static final String DB_NAME = "/WEB-INF/GeoIP.dat";
    private static final String URL_WEBHOSTING = "http://www.maxmind.com/app/update?license_key=";

    private String pathApp;

    public IP2CUtils(String pathApp) {
        this.pathApp = pathApp;
    }

    @Override
    public void run() {
        String licenseNumber = getJob().getJobProperty("licenseNumber");

        if (StringUtils.isEmpty(licenseNumber)) {
            addOutputMessage("Licence number is required");
            setJobExecutionResult(Job.STATUS_ERROR);
            return;
        }

        String dbFileName = pathApp + DB_NAME;
        String tmpFileName = pathApp + TMP_NAME;
        // Download database
        setExecutionMessage("Downloading IP database");
        setExecutionPercent(0d);
        try {
            String urlMd5Part = "";
            File dbFile = new File(dbFileName);
            if (dbFile.exists()) {
                String md5 = SomeUtils.getMD5Checksum(dbFileName);
                if (StringUtils.isNotEmpty(md5)) urlMd5Part = "&md5=" + md5;
            }

            // Eliminar temporal si existe
            File tmpFile = new File(tmpFileName);
            if (tmpFile.exists()) tmpFile.delete();

            // descargar bd en archivo temporal
            URL url = new URL(URL_WEBHOSTING + licenseNumber);
            GZIPInputStream zin = new GZIPInputStream(url.openStream());
            long totalSize = 2000000;
            FileOutputStream fos = new FileOutputStream(tmpFileName);
            byte data[] = new byte[1024];
            int count, downloaded = 0;
            while ((count = zin.read(data, 0, 1024)) != -1) {
                fos.write(data, 0, count);
                downloaded += count;
                setExecutionMessage("Downloading IP database " + SomeUtils.formatFileSize(downloaded) + " / " + SomeUtils.formatFileSize(totalSize));
                setExecutionPercent(downloaded * 100d / totalSize);
            }

            // copy temporal to db file
            if (tmpFile.exists() && tmpFile.canRead()) {
                if (dbFile.exists()) dbFile.delete();
                tmpFile.renameTo(dbFile);
            }

            setExecutionMessage("COMPLETE");
            setExecutionPercent(100d);
            setJobExecutionResult(Job.STATUS_OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            setExecutionMessage(e.getMessage());
            addOutputMessage(e.getMessage());
            setJobExecutionResult(Job.STATUS_ERROR);
        }

    }

}
