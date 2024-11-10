import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;

public class MainParser extends DefaultHandler {

    private List<MainItem> myMovies;
    private String tempVal;
    private MainItem tempMovie;
    private String currentDirector;

    public MainParser() {
        myMovies = new ArrayList<>();
    }
    private List<String[]> inconsistent = new ArrayList<>();

    public void runExample() {
        parseDocument();
        printData();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            FileInputStream fis = new FileInputStream("parse/main_test_set.xml");
            InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
            sp.parse(new InputSource(isr), this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void printData() {
        System.out.println("Number of Main Items: " + myMovies.size());
        for (MainItem movie : myMovies) {
            System.out.println(movie);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new MainItem();
            tempMovie.setDirector(currentDirector);
        } else if (qName.equalsIgnoreCase("director")) {
            currentDirector = "";
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equalsIgnoreCase("film")) {
                if (tempMovie.getFilmTitle() != null && tempMovie.getDirector() != null && tempMovie.getYear() > 0) {
                    if (!tempMovie.getGenres().isEmpty()) {
                        myMovies.add(tempMovie);
                    } else {
                        System.out.println("Skipping MainItem due to empty genres list.");
                    }
                } else {
                    System.out.println("Skipping MainItem due to missing film title, director, or year.");
                }
            } else if (qName.equalsIgnoreCase("fid")) {
                tempMovie.setFilmId(tempVal);
            } else if (qName.equalsIgnoreCase("t")) {
                tempMovie.setFilmTitle(tempVal);
            } else if (qName.equalsIgnoreCase("year")) {
                try {
                    int year = Integer.parseInt(tempVal);
                    tempMovie.setYear(year);
                } catch (NumberFormatException e) {
                    System.out.println("Ignoring invalid year value: " + tempVal);
                    inconsistent.add(new String[]{"Invalid Year", tempVal});
                }
            } else if (qName.equalsIgnoreCase("dirname")) {
                currentDirector = tempVal;
            } else if (qName.equalsIgnoreCase("cat")) {
                tempMovie.addGenre(tempVal);
            }
        } catch (Exception e) {
            System.out.println("Error processing element " + qName + ": " + e.getMessage());
        }
    }

    public void printInconsistentEntries() {
        File outFile = new File("Inconsistent.csv");

        System.out.println("Inconsistent Entries:");
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

    public static void main(String[] args) {
        MainParser parser = new MainParser();
        parser.runExample();
    }
}
