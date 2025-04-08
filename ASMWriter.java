import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Translates VM commands into HACK assembly code.
 */
public class ASMWriter {

    private int jumpFlagCounter; // Counter for unique jump labels
    private PrintWriter outPrinter;

    /**
     * Opens an output file and prepares it for writing assembly code.
     * 
     * @param fileOut The output file.
     */
    public ASMWriter(File fileOut) {
        try {
            outPrinter = new PrintWriter(fileOut);
            jumpFlagCounter = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the assembly code for the given arithmetic command.
     * 
     * @param command The arithmetic command (e.g., "add", "sub", "eq").
     */
    public void writeArithmetic(String command) {
        switch (command) {
            case "add":
                outPrinter.print(arithmeticTemplate1() + "M=D+M\n");
                break;
            case "sub":
                outPrinter.print(arithmeticTemplate1() + "M=M-D\n");
                break;
            case "and":
                outPrinter.print(arithmeticTemplate1() + "M=D&M\n");
                break;
            case "or":
                outPrinter.print(arithmeticTemplate1() + "M=D|M\n");
                break;
            case "gt":
                outPrinter.print(arithmeticTemplate2("JLE"));
                jumpFlagCounter++;
                break;
            case "lt":
                outPrinter.print(arithmeticTemplate2("JGE"));
                jumpFlagCounter++;
                break;
            case "eq":
                outPrinter.print(arithmeticTemplate2("JNE"));
                jumpFlagCounter++;
                break;
            case "not":
                outPrinter.print("@SP\nA=M-1\nM=!M\n");
                break;
            case "neg":
                outPrinter.print("D=0\n@SP\nA=M-1\nM=D-M\n");
                break;
            default:
                throw new IllegalArgumentException("Invalid arithmetic command: " + command);
        }
    }

    /**
     * Writes the assembly code for the given push or pop command.
     * 
     * @param command PUSH or POP.
     * @param segment The memory segment (e.g., "local", "argument").
     * @param index   The index within the segment.
     */
    public void writePushPop(int command, String segment, int index) {
        if (command == VMParser.PUSH) {
            outPrinter.print(generatePushCode(segment, index));
        } else if (command == VMParser.POP) {
            outPrinter.print(generatePopCode(segment, index));
        } else {
            throw new IllegalArgumentException("Invalid push/pop command: " + command);
        }
    }

    /**
     * Closes the output file.
     */
    public void close() {
        outPrinter.close();
    }

    // Private helper methods

    /**
     * Template for arithmetic commands like add, sub, and, or.
     * 
     * @return The assembly code template.
     */
    private String arithmeticTemplate1() {
        return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n";
    }

    /**
     * Template for comparison commands like gt, lt, eq.
     * 
     * @param jumpType The jump condition (e.g., "JLE", "JGE").
     * @return The assembly code template.
     */
    private String arithmeticTemplate2(String jumpType) {
        return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n" +
                "D=M-D\n" +
                "@FALSE" + jumpFlagCounter + "\n" +
                "D;" + jumpType + "\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=-1\n" +
                "@CONTINUE" + jumpFlagCounter + "\n" +
                "0;JMP\n" +
                "(FALSE" + jumpFlagCounter + ")\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=0\n" +
                "(CONTINUE" + jumpFlagCounter + ")\n";
    }

    /**
     * Generates the assembly code for a push command.
     * 
     * @param segment The memory segment.
     * @param index   The index within the segment.
     * @return The assembly code for the push command.
     */
    private String generatePushCode(String segment, int index) {
        switch (segment) {
            case "constant":
                return "@" + index + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n";
            case "local":
                return pushTemplate("LCL", index, false);
            case "argument":
                return pushTemplate("ARG", index, false);
            case "this":
                return pushTemplate("THIS", index, false);
            case "that":
                return pushTemplate("THAT", index, false);
            case "temp":
                return pushTemplate("R5", index + 5, false);
            case "pointer":
                return pushTemplate(index == 0 ? "THIS" : "THAT", index, true);
            case "static":
                return pushTemplate(String.valueOf(16 + index), index, true);
            default:
                throw new IllegalArgumentException("Invalid segment for push: " + segment);
        }
    }

    /**
     * Generates the assembly code for a pop command.
     * 
     * @param segment The memory segment.
     * @param index   The index within the segment.
     * @return The assembly code for the pop command.
     */
    private String generatePopCode(String segment, int index) {
        switch (segment) {
            case "local":
                return popTemplate("LCL", index, false);
            case "argument":
                return popTemplate("ARG", index, false);
            case "this":
                return popTemplate("THIS", index, false);
            case "that":
                return popTemplate("THAT", index, false);
            case "temp":
                return popTemplate("R5", index + 5, false);
            case "pointer":
                return popTemplate(index == 0 ? "THIS" : "THAT", index, true);
            case "static":
                return popTemplate(String.valueOf(16 + index), index, true);
            default:
                throw new IllegalArgumentException("Invalid segment for pop: " + segment);
        }
    }

    /**
     * Template for push commands.
     * 
     * @param segment  The memory segment.
     * @param index    The index within the segment.
     * @param isDirect Whether the addressing is direct.
     * @return The assembly code template.
     */
    private String pushTemplate(String segment, int index, boolean isDirect) {
        String addressCode = isDirect ? "" : "@" + index + "\nA=D+A\nD=M\n";
        return "@" + segment + "\nD=M\n" + addressCode +
                "@SP\nA=M\nM=D\n@SP\nM=M+1\n";
    }

    /**
     * Template for pop commands.
     * 
     * @param segment  The memory segment.
     * @param index    The index within the segment.
     * @param isDirect Whether the addressing is direct.
     * @return The assembly code template.
     */
    private String popTemplate(String segment, int index, boolean isDirect) {
        String addressCode = isDirect ? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";
        return "@" + segment + "\n" + addressCode +
                "@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n";
    }
}