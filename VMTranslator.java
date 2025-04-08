import java.io.File;

/**
 * VMTranslator used to parse .vm file and translate it to .asm file
 */
public class VMTranslator {

    // Main method to run the translator
    public static void main(String[] args) {

        // Basic input validation. Should be improved to handle more cases
        // Correct input format example: java VMTranslator sample.vm
        if (args.length != 1) {

            System.out.println("Input format: java VMTranslator [filename.vm]");
            return;

        }

        File fileIn = new File(args[0]);

        if (!fileIn.isFile() || !fileIn.getName().endsWith(".vm")) {

            System.out.println("Error: Input must be a .vm file");
            return;

        }

        // Create output asm file in the same directory
        String fileOutPath = fileIn.getAbsolutePath().substring(0, fileIn.getAbsolutePath().lastIndexOf(".")) + ".asm";
        File fileOut = new File(fileOutPath);
        ASMWriter writer = new ASMWriter(fileOut);

        VMParser parser = new VMParser(fileIn);

        int type = -1;

        while (parser.hasMoreCommands()) {

            parser.advance();

            type = parser.commandType();

            if (type == VMParser.ARITHMETIC) {

                writer.writeArithmetic(parser.arg1());

            } else if (type == VMParser.POP || type == VMParser.PUSH) {

                writer.writePushPop(type, parser.arg1(), parser.arg2());

            }

        }

        writer.close();

        System.out.println("File created : " + fileOutPath);
    }
}