package cz.speedy11.mcrpx.util;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * ZipUtil is utility class for extracting content from zip files into directory.
 *
 * @author Speedy11CZ
 * @since 1.0.0
 */
public class ZipUtil {

    /**
     * Buffer size for reading zip file.
     */
    private static final int BUFFER_SIZE = 4096;

    private ZipUtil() {
        throw new IllegalStateException("Class cannot be instantiated");
    }

    /**
     * Unzips zip file into directory.
     *
     * @param file zip file
     * @param destDirectory destination directory
     *
     * @throws IOException if an I/O error occurs
     * @throws ZipException if a ZIP format error has occurred
     */
    public static void unzip(File file, File destDirectory) throws IOException {
        if (!destDirectory.exists()) {
            destDirectory.mkdir();
        }

        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

        System.out.println("Extracting resource pack " + file.getName() + " to directory " + destDirectory.getName());
        int elementCount = 0;
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            System.out.println("Extracting " + zipEntry.getName());
            InputStream entryInputStream = zipFile.getInputStream(zipEntry);
            String filePath = destDirectory + File.separator + zipEntry.getName();
            File zipEntryFile = new File(filePath);
            zipEntryFile.getParentFile().mkdirs();
            extractFile(entryInputStream, zipEntryFile);
            elementCount++;
        }

        System.out.println("Successfully extracted " + elementCount + " elements");
        zipFile.close();
    }

    /**
     * Extracts file from zip file into directory.
     *
     * @param entryInputStream input stream of zip entry
     * @param zipEntryFile file to extract
     *
     * @throws IOException if an I/O error occurs
     */
    private static void extractFile(InputStream entryInputStream, File zipEntryFile) throws IOException {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(zipEntryFile));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read;
        while ((read = entryInputStream.read(bytesIn)) != -1) {
            bufferedOutputStream.write(bytesIn, 0, read);
        }
        bufferedOutputStream.close();
    }
}