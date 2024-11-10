import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.xml.sax.InputSource;

public class CastsParser extends DefaultHandler {

    private List<CastsItem> myCasts;
    private String tempVal;
    private CastsItem tempCast;
    private String currentDirector; // Variable to store current director

    public CastsParser() {
        myCasts = new ArrayList<>();
    }

    public void runExample() {
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
        System.out.println("Number of Cast Items: " + myCasts.size());
        for (CastsItem cast : myCasts) {
            System.out.println(cast);
        }
    }

    // Event Handlers
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = ""; // Reset tempVal at the start of each element
        if (qName.equalsIgnoreCase("filmc")) {
            // Start of a new film; create a new CastsItem
            tempCast = new CastsItem();
            tempCast.setDirector(currentDirector); // Set the current director
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("is")) {
            currentDirector = tempVal; // Update currentDirector when <is> tag ends
        } else if (qName.equalsIgnoreCase("filmc")) {
            // End of <filmc>, add tempCast to myCasts if it meets the conditions
            if (tempCast.getStarStageNames().isEmpty() || tempCast.getFilmTitle() == null || tempCast.getFilmTitle().isEmpty()) {
                // Do not add to myCasts if Star Stage Names list is empty or film title is missing
                System.out.println("Skipping CastsItem due to missing film title or empty star stage names list.");
            } else {
                myCasts.add(tempCast); // Add the completed CastsItem to the list
            }
        } else if (qName.equalsIgnoreCase("f")) {
            tempCast.setFilmId(tempVal); // Set film ID
        } else if (qName.equalsIgnoreCase("t")) {
            tempCast.setFilmTitle(tempVal); // Set film title (assumes all <t> in <filmc> are the same)
        } else if (qName.equalsIgnoreCase("a")) {
            // Only add Star Stage Name if it is not "sa" or empty
            if (!tempVal.equalsIgnoreCase("sa") && !tempVal.trim().isEmpty()) {
                tempCast.addStarStageName(tempVal); // Add star stage name to the list
            } else {
                System.out.println("Ignoring Star Stage Name: " + tempVal);
            }
        }
    }

    public static void main(String[] args) {
        CastsParser cp = new CastsParser();
        cp.runExample();
    }
}
