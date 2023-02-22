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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Utility class for working with files.
 *
 * @author Michal Spišak (Speedy11CZ)
 * @since 1.1.0
 */
public class FileUtil {

    private FileUtil() {
        throw new IllegalStateException("Class cannot be instantiated");
    }

    /**
     * Checks if directory is empty.
     *
     * @param file directory to check
     * @return true if directory is empty, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public static boolean isEmpty(File file) throws IOException {
        if(!file.isDirectory()) {
            throw new IllegalArgumentException("File is not directory");
        }

        try(Stream<Path> directoryStream = Files.list(file.toPath())) {
            return !directoryStream.findFirst().isPresent();
        } catch (IOException e) {
            throw new IOException("Cannot check if directory is empty", e);
        }
    }
}
