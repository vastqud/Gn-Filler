import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class pptxreader {

    public static void main(String[] args) {
        pptxreader programm = new pptxreader();
        try {
            String notesFile = "";
            String worksheetFile = "";
            String outputFile = "";

            Scanner scannerObj = new Scanner(System.in);
            System.out.println("Enter file path of PowerPoint file (must be saved as .txt)");

            notesFile = scannerObj.nextLine();

            System.out.println("Enter file path of worksheet file (also saved as .txt)");

            worksheetFile = scannerObj.nextLine();

            System.out.println("Enter file path of output file (must be a blank .txt file)");

            outputFile = scannerObj.nextLine();

            programm.start(notesFile, worksheetFile, outputFile);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static void write(String str, String file) {
        try {
            FileWriter myWriter = new FileWriter(file, true);
            myWriter.write(str + System.lineSeparator());
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String transformString(String str) {
        str = str.trim();
        if (str.length() >= 3) {
            if (str.substring(0, 1).matches("[0-9]")) {
                str = str.substring(3);
                str = str.trim();
                return str;
            }
        }
        return str;
    }

    public static boolean isWordBlacklisted(String word) {
        String[] blacklist = {
            "Defition:",
            "Examples:",
            "Fun Fact:",
            "Includes:",
            "Example:",
            "Include:",
            "Characteristics:",
            "Effects on plants:",
            "Prevention Methods:",
            "Treatment Methods:",
            "Life Cycle:",
            "Economic Impact:"
        };

        for (String thisWord : blacklist) {
            thisWord = thisWord.toLowerCase();
            if (thisWord.equals(word)) {
                return true;
            }
        }

        return false;
    }

    public static boolean wasHeaderUsed(ArrayList array, String entry) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(entry)) {
                if (!isWordBlacklisted(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getIndexOfNonWhitespaceAfterWhitespace(String string){
        char[] characters = string.toCharArray();
        boolean lastWhitespace = false;
        for(int i = 0; i < string.length(); i++){
            if(Character.isWhitespace(characters[i])){
                lastWhitespace = true;
            } else if(lastWhitespace){
                return i;
            }
        }
        return -1;
    }

    public static String getFormattedLine(int lineCount, String line, String actualLine) {
        int numWhitespace = getIndexOfNonWhitespaceAfterWhitespace(line);
        numWhitespace++;
        numWhitespace = numWhitespace + 4;
        String string = "";

        for (int i = 0; i < numWhitespace; i++) {
            string = string + " ";
        }

        return string + String.valueOf(lineCount) + ". " + actualLine;
    }

    public static boolean compareLineCounts(int currentLineCount) {
        if (currentLineCount > 90) {
            return false;
            // need to determine how many entries under each header somehow
        } else {
            return true;
        }
    }

    public void start(String notesFile, String worksheetFile, String outputFile) throws java.io.IOException {
        String currentHeader = "default";
        int lineCount = 0;
        int noteParseCount = 0;
        int worksheetParseCount = 0;
        ArrayList<String> pastHeaders = new ArrayList<String>();
        System.out.println("Beginning parsing...");
        for (String line : Files.readAllLines(Paths.get(notesFile))){
            String origLine = line;
            line = line.toLowerCase();
            noteParseCount++;
            worksheetParseCount = 0;
            boolean isHeader = false;
            for (String guideLine : Files.readAllLines(Paths.get(worksheetFile))){
                worksheetParseCount++;
                String origGuide = guideLine;
                guideLine = guideLine.toLowerCase();
                System.out.println("Parsing notes... " + "Line " + String.valueOf(worksheetParseCount));
                if (transformString(guideLine).equals(line.trim()) && (!guideLine.equals(currentHeader.toLowerCase())) && (!line.equals(currentHeader.toLowerCase())) && (!transformString(guideLine).equals(currentHeader.toLowerCase()))) {
                    if ((guideLine != null) && (!transformString(guideLine).trim().isEmpty()) && (wasHeaderUsed(pastHeaders, guideLine) != true)) {
                        isHeader = true;
                        currentHeader = origGuide;
                        pastHeaders.add(guideLine);
                        pastHeaders.add(transformString(guideLine));
                    } 
                }
            }
            
            System.out.println("Writing to file... " + "Line " + String.valueOf(noteParseCount));
            if (isHeader) {
                write(currentHeader, outputFile);
                lineCount = 0;
            } else {
                if ((line != null) && (!line.trim().isEmpty())) {
                    if ((isNumeric(line.trim()) != true) && (wasHeaderUsed(pastHeaders, line.trim()) != true)) {
                        lineCount++;
                        
                        if (compareLineCounts(lineCount) == true){
                            write(getFormattedLine(lineCount, currentHeader, line), outputFile);
                        }
                    }
                }
            }
        }
    }
}