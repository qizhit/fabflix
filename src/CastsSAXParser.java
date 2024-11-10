import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;

// Parse casts124.xml, in order to link stars to movies (stars_in_movies).
public class CastsSAXParser extends DefaultHandler {

    private List<CastsItem> myCasts;
    private String tempVal;
    private CastsItem tempCast;
    private String currentDirector; // Variable to store current director
    private List<String> inconsistent = new ArrayList<>();

    public CastsSAXParser() {
        myCasts = new ArrayList<>();
    }

    public void run() {
        parseDocument();
        printData();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            FileInputStream fis = new FileInputStream("parse/casts124.xml");
            InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
            sp.parse(new InputSource(isr), this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void printData() {
//        for (CastsItem cast : myCasts) {
//            System.out.println(cast);
//        }
        System.out.println("Number of Cast Items: " + myCasts.size());
    }

    public List<CastsItem> getMyCasts() {
        return myCasts;
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = ""; // Reset tempVal at the start of each element
        if (qName.equalsIgnoreCase("filmc")) {
            // Start of a new film; create a new CastsItem
            tempCast = new CastsItem();
            tempCast.setDirector(currentDirector); // Set the current director
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal += new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("is")) {
            currentDirector = tempVal; // Update currentDirector when <is> tag ends
        } else if (qName.equalsIgnoreCase("filmc")) {
            // End of <filmc>, add tempCast to myCasts if it meets the conditions
            if (tempCast.getStarStageNames().isEmpty() || tempCast.getFilmTitle() == null || tempCast.getFilmTitle().isEmpty()) {
                // Do not add to myCasts if Star Stage Names list is empty or film title is missing
                inconsistent.add(tempCast.toString());
            } else {
                myCasts.add(tempCast); // Add the completed CastsItem to the list
            }
        } else if (qName.equalsIgnoreCase("f")) {
            tempCast.setFilmId(tempVal); // Set film ID
        } else if (qName.equalsIgnoreCase("t")) {
            tempCast.setFilmTitle(tempVal); // Set film title (assumes all <t> in <filmc> are the same)
        } else if (qName.equalsIgnoreCase("a")) {
            // Only add Star Stage Name if it is not "sa" or empty
            if (!tempVal.equalsIgnoreCase("sa") && !tempVal.equalsIgnoreCase("s a") && !tempVal.trim().isEmpty()) {
                tempCast.addStarStageName(tempVal); // Add star stage name to the list
            } else {
                inconsistent.add("Film ID: " + tempCast.getFilmId() + ", Star Stage Name: " + tempVal);
            }
        }
    }

    public void printInconsistentEntries() {
        File outFile = new File("CastsInconsistent.txt");

        System.out.println("Inconsistent Entries: " + inconsistent.size());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("Inconsistent Entries: " + inconsistent.size() + "\n");
            for (String entry : inconsistent) {
                writer.write(entry + "\n");
            }
            System.out.println("Inconsistent entries written to " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        CastsSAXParser parse = new CastsSAXParser();
        parse.run();
        parse.printInconsistentEntries();
    }
}
