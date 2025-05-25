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

package cz.speedy11.mcrpx.cli;

import cz.speedy11.mcrpx.common.extraction.ExtractTaskAdapter;
import cz.speedy11.mcrpx.common.util.FileUtil;
import cz.speedy11.mcrpx.common.util.ZipUtil;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Main class of the CLI application.
 *
 * @author Michal Spišak (Speedy11CZ)
 * @since 1.0.0
 */
public class Main extends ExtractTaskAdapter {

    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(Arrays.asList("?", "help"), "Show the help");
        OptionSpec<File> inputFileSpec = parser.acceptsAll(Arrays.asList("i", "input"), "Input file to extract resource pack from")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("Input file")
                .required();

        OptionSpec<File> outputDirectorySpec = parser.acceptsAll(Arrays.asList("o", "output"), "Output directory for resource pack content")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("output"))
                .describedAs("Resource pack output directory");

        OptionSet options = null;
        try {
            options = parser.parse(args);
        } catch (OptionException exception) {
            System.out.println(exception.getLocalizedMessage());
        }


        if ((options == null) || (options.has("?"))) {
            try {
                parser.printHelpOn(System.out);
                return;
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        File inputFile = options.valueOf(inputFileSpec);
        File outputDirectory = options.valueOf(outputDirectorySpec);

        if (!inputFile.exists()) {
            System.err.println("Input file does not exists");
            return;
        }

        try {
            if (outputDirectory.exists() && !FileUtil.isEmpty(outputDirectory)) {
                System.err.println("Output directory must be empty");
                return;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return;
        }


        try {
            ZipUtil.extract(inputFile, outputDirectory, new ExtractTaskAdapter() {
                @Override
                public void onError(Exception exception) {
                    exception.printStackTrace();
                }

                @Override
                public void onMessage(String message) {
                    System.out.println(message);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
