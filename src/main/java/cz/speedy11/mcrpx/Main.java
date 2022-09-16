package cz.speedy11.mcrpx;

import cz.speedy11.mcrpx.util.ZipUtil;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipException;

public class Main {

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(Arrays.asList("?", "help"), "Show the help");
        OptionSpec<File> resourcePackFileSpec = parser.acceptsAll(Arrays.asList("i", "input"), "Input file with resource pack")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("Resource pack zip file")
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

        File resourcePackFile = options.valueOf(resourcePackFileSpec);
        File outputDirectory = options.valueOf(outputDirectorySpec);

        if(!resourcePackFile.exists()) {
            System.out.println("Input file does not exists");
            return;
        }

        if(outputDirectory.exists() && outputDirectory.listFiles().length > 0) {
            System.out.println("Output directory must be empty");
            return;
        }

        try {
            ZipUtil.unzip(resourcePackFile, outputDirectory);
        } catch (ZipException exception) {
            System.out.println("Input file is not valid resource pack");
        }
    }
}
