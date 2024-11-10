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
    private List<String[]> inconsistent = new ArrayList<>();
    private Integer failed = 0;
    private Integer empty = 0;
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
            //System.out.println("Starting to parse XML file: " + xmlFile);
            long startTime = System.currentTimeMillis();
            sp.parse(new InputSource(reader), this);
            long endTime = System.currentTimeMillis();
            //System.out.println("Finished parsing actors XML with ISO-8859-1 encoding");
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
                empty += 1;
                System.out.println("empty actor, total empty num: " + empty);
            } else if(existingStars.containsKey(starName)) {
                failed += 1;
                System.out.println("Failed, star existed, total existed num: " + failed);

            } else {
                System.out.println("Adding new star to list: " + starName + ", " + (birthYear != null ? birthYear : "N/A"));
                newStars.add(new String[]{starName, birthYear != null ? birthYear.toString() : ""});
                existingStars.put(starName, birthYear);
            }

        } else if (qName.equalsIgnoreCase("stagename")) {
            starName = tempVal.toString();
            if (starName.matches(".*\\d.*")) { // Check for non-string content
                inconsistent.add(new String[]{"<stagename>" + starName});
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
                inconsistent.add(new String[]{"<dob>",birthYearStr});
                birthYear = null;  // Set to null if invalid
            }
        }
    }

    public List<String[]> getStarsList() {
        System.out.println("Returning list of parsed stars.");
        return newStars;

    }

    public void printresult(){
        System.out.println("Number of Star from parser: " + newStars.size());
        System.out.println("Number of Empty name: " + empty);
        System.out.println("Inconsistent Entries: " + inconsistent.size());
    }

    public void writeInconsistentEntries() {
        File outFile = new File("Inconsistent.txt");
        System.out.println("Inconsistent Entries: " + inconsistent.size());
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
//        parser.printInconsistentEntries();
//        parser.printresult();
//        System.out.println("Parsing time: " + (parser.exe_time) + " ms");
//
//    }

}