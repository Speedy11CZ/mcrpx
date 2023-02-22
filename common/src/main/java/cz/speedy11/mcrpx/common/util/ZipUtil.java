/*
 * This file is part of MCRPX, licensed under the MIT License.
 *
 * Copyright (c) Michal Spišak (Speedy11CZ) <michalspisak53@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cz.speedy11.mcrpx.common.util;

import cz.speedy11.mcrpx.common.extraction.ExtractTaskListener;

import java.io.*;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * ZipUtil is utility class for extracting content from zip files into directory.
 *
 * @author Michal Spišak (Speedy11CZ)
 * @since 1.0.0
 */
public class ZipUtil {

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static final int BUFFER_SIZE = 4096;

    private ZipUtil() {
        throw new IllegalStateException("Class cannot be instantiated");
    }

    /**
     * Extracts Minecraft assets file into directory.
     *
     * @param file            Minecraft jar file or resource pack zip file
     * @param destDirectory   destination directory
     * @param listener        extraction listener
     * @return future of extraction
     */
    public static Future<?> extract(File file, File destDirectory, ExtractTaskListener listener) {
        return EXECUTOR.submit(() -> {
            try {
                if (isValidMinecraftJar(file)) {
                    listener.bytesTotal(getZipUncompressedSize(file, "assets/"));
                    extractMinecraft(file, destDirectory, listener);
                } else if (isValidResourcePack(file)) {
                    listener.bytesTotal(getZipUncompressedSize(file, null));
                    extractZip(file, destDirectory, listener);
                } else {
                    throw new IllegalArgumentException("Unsupported zip file! Must be resource pack or Minecraft jar file");
                }
            } catch (IOException exception) {
                listener.onError(exception);
            }
        });
    }

    /**
     * Extracts resource pack from zip file.
     *
     * @param file          resource pack
     * @param destDirectory destination directory
     * @param listener        extraction listener
     * @throws IOException  if an I/O error occurs
     * @throws ZipException if a ZIP format error has occurred
     */
    public static void extractZip(File file, File destDirectory, ExtractTaskListener listener) throws IOException {
        if (!destDirectory.exists()) {
            destDirectory.mkdir();
        }
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            listener.onMessage("Extracting resource pack " + file.getName() + " to directory " + destDirectory.getName());
            int elementCount = 0;
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (!zipEntry.isDirectory()) {
                    listener.onMessage("Extracting " + zipEntry.getName());
                    try (InputStream entryInputStream = zipFile.getInputStream(zipEntry)) {
                        String filePath = destDirectory + File.separator + zipEntry.getName();
                        File zipEntryFile = new File(filePath);
                        zipEntryFile.getParentFile().mkdirs();
                        listener.fileProcessed(entryInputStream.available());
                        extractFile(entryInputStream, zipEntryFile);
                        elementCount++;
                    }
                }
            }

            listener.onMessage("Successfully extracted " + elementCount + " elements");
            listener.taskCompleted();
        }
    }

    /**
     * Extracts resource pack from Minecraft jar file.
     *
     * @param file          Minecraft jar
     * @param destDirectory destination directory
     * @param listener        extraction listener
     * @throws IOException  if an I/O error occurs
     * @throws JarException if a JAR format error has occurred
     */
    public static void extractMinecraft(File file, File destDirectory, ExtractTaskListener listener) throws IOException {
        if (!destDirectory.exists()) {
            destDirectory.mkdir();
        }

        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<? extends JarEntry> jarEntries = jarFile.entries();
            listener.onMessage("Extracting resource pack from Minecraft " + file.getName() + " to directory " + destDirectory.getName());
            int elementCount = 0;
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.getName().startsWith("assets/") && !jarEntry.isDirectory()) {
                    listener.onMessage("Extracting " + jarEntry.getName());
                    try (InputStream entryInputStream = jarFile.getInputStream(jarEntry)) {
                        String filePath = destDirectory + File.separator + jarEntry.getName();
                        File jarEntryFile = new File(filePath);
                        jarEntryFile.getParentFile().mkdirs();
                        listener.fileProcessed(entryInputStream.available());
                        extractFile(entryInputStream, jarEntryFile);
                        elementCount++;
                    }
                }
            }

            listener.onMessage("Successfully extracted " + elementCount + " elements");
            listener.taskCompleted();
        }
    }

    /**
     * Extracts file from zip file into directory.
     *
     * @param entryInputStream input stream of zip entry
     * @param zipEntryFile     file to extract
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

    /**
     * Returns zip file uncompressed size.
     *
     * @param file         zip file
     * @param subdirectory subdirectory to count size of
     * @return uncompressed size of zip file
     * @throws IOException if an I/O error occurs
     */
    private static int getZipUncompressedSize(File file, String subdirectory) throws IOException {
        int uncompressedSize = 0;
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (subdirectory != null && !zipEntry.getName().startsWith(subdirectory)) {
                    continue;
                }
                uncompressedSize += zipEntry.getSize();
            }
        }
        return uncompressedSize;
    }

    /**
     * Checks if file is valid Minecraft jar file or resource pack file.
     *
     * @param file file to check
     * @return true if file is valid Minecraft jar file or resource pack file, false otherwise
     */
    public static boolean isValid(File file) {
        try {
            return isValidMinecraftJar(file) || isValidResourcePack(file);
        } catch (IOException exception) {
            return false;
        }
    }

    /**
     * Checks if file is valid resource pack file.
     *
     * @param file file to check
     * @return true if file is valid resource pack file, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private static boolean isValidResourcePack(File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.getName().equals("pack.mcmeta")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if file is valid Minecraft version file.
     *
     * @param file file to check
     * @return true if file is valid Minecraft jar file, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private static boolean isValidMinecraftJar(File file) throws IOException {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry manifest = jarFile.getJarEntry("META-INF/MANIFEST.MF");
            if (manifest != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(manifest)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("Main-Class: net.minecraft.client.Main")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}