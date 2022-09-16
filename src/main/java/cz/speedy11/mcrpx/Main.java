package cz.speedy11.mcrpx;

import cz.speedy11.mcrpx.util.ZipUtil;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarException;
import java.util.zip.ZipException;

public class Main {

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(Arrays.asList("?", "help"), "Show the help");
        parser.acceptsAll(Arrays.asList("mc", "minecraft"), "Extract resource pack from Minecraft jar");
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

        if(!inputFile.exists()) {
            System.out.println("Input file does not exists");
            return;
        }

        if(outputDirectory.exists() && outputDirectory.listFiles().length > 0) {
            System.out.println("Output directory must be empty");
            return;
        }

        if(options.has("mc")) {
            try {
                ZipUtil.extractMinecraft(inputFile, outputDirectory);
            } catch (JarException exception) {
                System.out.println("Input file is not valid Minecraft jar");
            }
        } else {
            try {
                ZipUtil.extractZip(inputFile, outputDirectory);
            } catch (ZipException exception) {
                System.out.println("Input file is not valid resource pack");
            }
        }
    }
}
