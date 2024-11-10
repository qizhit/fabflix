import java.io.*;
import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


// Parse actors63.xml, in order to add stars (stars).
public class StarsSAXParser extends DefaultHandler {private StringBuilder tempVal;
    private String starName;
    private Integer birthYear;
    private HashMap<String, Integer> existingStars;
    private List<String[]> newStars;

    //Write file use
    private List<String[]> inconsistent = new ArrayList<>();
    private List<String[]> faildueEmpty= new ArrayList<>();
    private List<String[]> faildueDupicated = new ArrayList<>();

    public long exe_time = 0;

    public StarsSAXParser(HashMap<String, Integer> existingStars) {
        this.existingStars = existingStars;
        this.newStars = new ArrayList<>();
    }

    public void parseDocument(String xmlFile) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            FileInputStream fileInputStream = new FileInputStream(xmlFile);
            InputStreamReader reader = new InputStreamReader(fileInputStream, "ISO-8859-1");
            long startTime = System.currentTimeMillis();
            sp.parse(new InputSource(reader), this);
            long endTime = System.currentTimeMillis();
            exe_time = endTime - startTime;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = new StringBuilder();
        if (qName.equalsIgnoreCase("actor")) {
            starName = null;
            birthYear = null;
        }
    }

    public void characters(char[] ch, int start, int length) {
        tempVal.append(new String(ch, start, length).trim());
    }

    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("actor")) {
            //System.out.println("End of <actor> element. Star Name: " + starName + ", Birth Year: " + (birthYear != null ? birthYear : "null"));

            if (starName == null || starName.isEmpty()){
                faildueEmpty.add(new String[]{"Empty <stagename> ", starName});
            } else if(existingStars.containsKey(starName)&& Objects.equals(existingStars.get(starName), birthYear)) {
                faildueDupicated.add(new String[]{"Duplicate <stagename> " + starName + " with birth year: " + birthYear});
            } else {
                newStars.add(new String[]{starName, birthYear != null ? birthYear.toString() : ""});
                existingStars.put(starName, birthYear);
            }

        } else if (qName.equalsIgnoreCase("stagename")) {
            starName = tempVal.toString();
            if (starName.matches(".*\\d.*")) { // Check for non-string content
                inconsistent.add(new String[]{"Inconsistent: <stagename> " + starName});
                starName = null;
            }
        } else if (qName.equalsIgnoreCase("dob")) {
            String birthYearStr = tempVal.toString().trim();
            if (birthYearStr.isEmpty()) {
                birthYear = null;  // Allow null for empty birthYear
            } else if (birthYearStr.equals("n.a.")) {
                birthYear = null;
            } else if (birthYearStr.matches("\\d+")) {  // Check if birthYear is all digits
                birthYear = Integer.parseInt(birthYearStr);  // Parse as integer
            } else {
                inconsistent.add(new String[]{"Inconsistent: <dob> " + birthYearStr});
                birthYear = null;  // Set to null if invalid
            }
        }
    }

    public List<String[]> getStarsList() {
        System.out.println("Returning list of parsed stars.");
        return newStars;

    }

    public void writeAndDisplayResult(){
        System.out.println("Writing and Displaying Stars Results...");
        System.out.println("Star inconsistent Entries: " + inconsistent.size());
        writeInconsistentEntries();
        System.out.println("Star Empty Entries: " + faildueEmpty.size());
        writeEmptyEntries();
        System.out.println("Star Duplicated Entries: " + faildueDupicated.size());
        writeDuplicateEntries();

    }

    public void writeInconsistentEntries() {
        File outFile = new File("StarInconsistentEntries.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("Inconsistent Entries:\n");
            for (String[] entry : inconsistent) {
                writer.write("Inconsistent Entry: " + Arrays.toString(entry) + "\n");
            }
            System.out.println("Inconsistent entries written to " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public void writeEmptyEntries() {
        File outFile = new File("StarEmptyEntries.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("Star Empty Entries:\n");
            for (String[] entry : faildueEmpty) {
                writer.write("Empty Entry: " + Arrays.toString(entry) + "\n");
            }
            System.out.println("Star Empty entries written to " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public void writeDuplicateEntries() {
        File outFile = new File("StarDuplicateEntries.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("Star Duplicate Entries:\n");
            for (String[] entry : faildueDupicated) {
                writer.write("Duplicate Entry: " + Arrays.toString(entry) + "\n");
            }
            System.out.println("Star Duplicate entries written to " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }



//    public static void main(String[] args) {
//        HashMap<String, Integer> existingStars = new HashMap<>();
//        StarsSAXParser parser = new StarsSAXParser(existingStars);
//        parser.parseDocument("parse/actors63.xml");
//        List<String[]> parsedStars = parser.getStarsList();
//
//
//        System.out.println("Parsed Stars for Insertion:");
//        for (String[] star : parsedStars) {
//            System.out.println(Arrays.toString(star));
//        }
//        parser.writeAndDisplayResult();
//        System.out.println("Parsing time: " + (parser.exe_time) + " ms");
//
//    }

}