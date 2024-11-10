import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class StarsSAXParser extends DefaultHandler {private StringBuilder tempVal;
    private String starName;
    private Integer birthYear;
    private HashMap<String, Integer> existingStars;
    private List<String[]> newStars;
    private List<String[]> inconsistent = new ArrayList<>();
    private Integer failed = 0;

    public StarsSAXParser(HashMap<String, Integer> existingStars) {
        this.existingStars = existingStars;
        this.newStars = new ArrayList<>();
        System.out.println("StarsSAXParser initialized with existing stars loaded.");
    }

    public void parseDocument(String xmlFile) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            FileInputStream fileInputStream = new FileInputStream(xmlFile);
            InputStreamReader reader = new InputStreamReader(fileInputStream, "ISO-8859-1");
            System.out.println("Starting to parse XML file: " + xmlFile);

            sp.parse(new InputSource(reader), this);
            System.out.println("Finished parsing actors XML with ISO-8859-1 encoding");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = new StringBuilder();
        if (qName.equalsIgnoreCase("actor")) {
            starName = null;
            birthYear = null;
            System.out.println("Processing new <actor> element.");
        }
    }

    public void characters(char[] ch, int start, int length) {
        tempVal.append(new String(ch, start, length).trim());
    }

    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("actor")) {
            System.out.println("End of <actor> element. Star Name: " + starName + ", Birth Year: " + (birthYear != null ? birthYear : "null"));

            if (starName == null || starName.isEmpty()){
                failed += 1;
                System.out.println("Failed, empty actor");
            } else if(existingStars.containsKey(starName)) {
                failed += 1;
                System.out.println("Failed, star existed");

            } else {
                System.out.println("Adding new star to list: " + starName + ", " + (birthYear != null ? birthYear : "N/A"));
                newStars.add(new String[]{starName, birthYear != null ? birthYear.toString() : ""});
                existingStars.put(starName, birthYear);
            }
        } else if (qName.equalsIgnoreCase("stagename")) {
            starName = tempVal.toString();
            if (starName.matches(".*\\d.*")) { // Check for non-string content
                inconsistent.add(new String[]{"Invalid Name", starName});
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
                System.out.println("Processed <dob>: " + birthYear);
            } else {
                System.out.println("Invalid birth year detected: " + birthYearStr);
                inconsistent.add(new String[]{"Invalid BirthYear", birthYearStr});
                birthYear = null;  // Set to null if invalid
            }
        }
    }

    public List<String[]> getStarsList() {
        System.out.println("Returning list of parsed stars.");
        return newStars;

    }

    public void printInconsistentEntries() {
        System.out.println("Inconsistent Entries:");
        for (String[] entry : inconsistent) {
            System.out.println("Inconsistent Entry: " + Arrays.toString(entry));
        }
    }

//    public static void main(String[] args) {
//        HashMap<String, Integer> existingStars = new HashMap<>();
//        StarsSAXParser parser = new StarsSAXParser(existingStars);
//        parser.parseDocument("/Users/x/Desktop/122B/2024-fall-cs-122b-cs122b-project1-ys/parse/actors63.xml");
//        List<String[]> parsedStars = parser.getStarsList();
//
//        System.out.println("Parsed Stars for Insertion:");
//        for (String[] star : parsedStars) {
//            System.out.println(Arrays.toString(star));
//        }
//        parser.printInconsistentEntries();
//
//
//    }

}