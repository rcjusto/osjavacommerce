package org.store.ip2country.webhosting;

import org.store.core.globals.SomeUtils;
import org.store.core.utils.quartz.JobStoreThread;
import org.store.ip2country.webhosting.ip2c.IP2Country;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Rogelio Caballero
 * 24/07/11 0:26
 */
public class IP2CUtils extends JobStoreThread {

    private static final String CSV_NAME = "/WEB-INF/ip-to-country.csv";
    public static final String BIN_NAME = "/WEB-INF/ip-to-country.bin";
    private static final String URL_WEBHOSTING = "http://ip-to-country.webhosting.info/downloads/ip-to-country.csv.zip";

    private String pathApp;

    public IP2CUtils(String pathApp) {
        this.pathApp = pathApp;
    }

    @Override
    public void run() {
        String csvFileName = pathApp + CSV_NAME;
        String binFileName = pathApp + BIN_NAME;

        // Download database
        setExecutionMessage("Downloading IP database");
        setExecutionPercent(0d);
        try {
            File csvFile = new File(csvFileName);
            if (csvFile.exists()) csvFile.delete();
            URL url = new URL(URL_WEBHOSTING);
            ZipInputStream zin = new ZipInputStream(url.openStream());
            ZipEntry entry = zin.getNextEntry();
            long totalSize = entry.getSize();
            entry.getCompressedSize();
            FileOutputStream fos = new FileOutputStream(csvFileName);
            try {
                byte data[] = new byte[1024];
                int count, downloaded = 0;
                while ((count = zin.read(data, 0, 1024)) != -1) {
                    fos.write(data, 0, count);
                    downloaded += count;
                    setExecutionMessage("Downloading IP database " + SomeUtils.formatFileSize(downloaded) + " / " + SomeUtils.formatFileSize(totalSize) );
                    setExecutionPercent( downloaded*100d/totalSize);
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                fos.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // Convert to bin
        setExecutionMessage("Optimizing database ...");
        File csvFile = new File(csvFileName);
        if (csvFile.exists() && csvFile.canRead()) {
            try {
                IP2Country.convertCSVtoBIN(csvFileName, binFileName);
                csvFile.delete();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        setExecutionMessage("COMPLETE");
        setExecutionPercent(100d);
    }

}
