import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Handles the parsing of a single .vm file.
 * Reads VM commands, parses them, and provides access to their components.
 * Removes all white space and comments.
 */
public class VMParser {
    // Constants for command types
    public static final int ARITHMETIC = 0;
    public static final int PUSH = 1;
    public static final int POP = 2;
    public static final int LABEL = 3;
    public static final int GOTO = 4;
    public static final int IF = 5;
    public static final int FUNCTION = 6;
    public static final int RETURN = 7;
    public static final int CALL = 8;

    // Static list of arithmetic commands
    public static final ArrayList<String> arithmeticCmds = new ArrayList<>();

    static {
        arithmeticCmds.add("add");
        arithmeticCmds.add("sub");
        arithmeticCmds.add("neg");
        arithmeticCmds.add("eq");
        arithmeticCmds.add("gt");
        arithmeticCmds.add("lt");
        arithmeticCmds.add("and");
        arithmeticCmds.add("or");
        arithmeticCmds.add("not");
    }

    // Instance variables
    private Scanner cmds;
    private String currentCmd;
    private int argType;
    private String argument1;
    private int argument2;

    /**
     * Opens the input file and prepares it for parsing.
     * 
     * @param fileIn The input file to parse.
     */
    public VMParser(File fileIn) {
        argType = -1;
        argument1 = "";
        argument2 = -1;

        try {
            String preprocessed = preprocessFile(fileIn);
            cmds = new Scanner(preprocessed.trim());
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
    }

    /**
     * Checks if there are more commands to read.
     * 
     * @return True if there are more commands, false otherwise.
     */
    public boolean hasMoreCommands() {
        return cmds.hasNextLine();
    }

    /**
     * Reads the next command from the input and makes it the current command.
     * Should only be called when hasMoreCommands() returns true.
     */
    public void advance() {
        currentCmd = cmds.nextLine();
        argument1 = "";
        argument2 = -1;

        String[] segs = currentCmd.split(" ");
        if (segs.length > 3) {
            throw new IllegalArgumentException("Too many arguments!");
        }

        parseCommand(segs);
    }

    /**
     * Returns the type of the current command.
     * ARITHMETIC is returned for all arithmetic commands.
     * 
     * @return The command type.
     */
    public int commandType() {
        if (argType != -1) {
            return argType;
        } else {
            throw new IllegalStateException("No command!");
        }
    }

    /**
     * Returns the first argument of the current command.
     * For ARITHMETIC commands, returns the command itself.
     * Should not be called for RETURN commands.
     * 
     * @return The first argument.
     */
    public String arg1() {
        if (commandType() != RETURN) {
            return argument1;
        } else {
            throw new IllegalStateException("Cannot get arg1 from a RETURN type command!");
        }
    }

    /**
     * Returns the second argument of the current command.
     * Should only be called for PUSH, POP, FUNCTION, or CALL commands.
     * 
     * @return The second argument.
     */
    public int arg2() {
        if (commandType() == PUSH || commandType() == POP || commandType() == FUNCTION || commandType() == CALL) {
            return argument2;
        } else {
            throw new IllegalStateException("Cannot get arg2!");
        }
    }

    /**
     * Removes comments (text after "//") from a string.
     * 
     * @param strIn The input string.
     * @return The string without comments.
     */
    public static String noComments(String strIn) {
        int position = strIn.indexOf("//");
        return (position != -1) ? strIn.substring(0, position) : strIn;
    }

    /**
     * Removes spaces from a string.
     * 
     * @param strIn The input string.
     * @return The string without spaces.
     */
    public static String noSpaces(String strIn) {
        return strIn.replace(" ", "");
    }

    /**
     * Gets the file extension from a filename.
     * 
     * @param fileName The filename.
     * @return The file extension.
     */
    public static String getExt(String fileName) {
        int index = fileName.lastIndexOf('.');
        return (index != -1) ? fileName.substring(index) : "";
    }

    // Private helper methods

    /**
     * Preprocesses the input file by removing comments and empty lines.
     * 
     * @param fileIn The input file.
     * @return The preprocessed file content as a string.
     * @throws FileNotFoundException If the file is not found.
     */
    private String preprocessFile(File fileIn) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(fileIn);
        StringBuilder preprocessed = new StringBuilder();

        while (fileScanner.hasNext()) {
            String line = noComments(fileScanner.nextLine()).trim();
            if (!line.isEmpty()) {
                preprocessed.append(line).append("\n");
            }
        }

        fileScanner.close();
        return preprocessed.toString();
    }

    /**
     * Parses the current command and sets the appropriate fields.
     * 
     * @param segs The split command segments.
     */
    private void parseCommand(String[] segs) {
        if (arithmeticCmds.contains(segs[0])) {
            argType = ARITHMETIC;
            argument1 = segs[0];
        } else if (segs[0].equals("return")) {
            argType = RETURN;
            argument1 = segs[0];
        } else {
            parseNonArithmeticCommand(segs);
        }
    }

    /**
     * Parses non-arithmetic commands and sets the appropriate fields.
     * 
     * @param segs The split command segments.
     */
    private void parseNonArithmeticCommand(String[] segs) {
        argument1 = segs[1];

        switch (segs[0]) {
            case "push":
                argType = PUSH;
                break;
            case "pop":
                argType = POP;
                break;
            case "label":
                argType = LABEL;
                break;
            case "if":
                argType = IF;
                break;
            case "goto":
                argType = GOTO;
                break;
            case "function":
                argType = FUNCTION;
                break;
            case "call":
                argType = CALL;
                break;
            default:
                throw new IllegalArgumentException("Unknown Command Type!");
        }

        if (argType == PUSH || argType == POP || argType == FUNCTION || argType == CALL) {
            try {
                argument2 = Integer.parseInt(segs[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Argument2 is not an integer!");
            }
        }
    }
}