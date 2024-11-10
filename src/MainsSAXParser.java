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

// Parse mains243.xml, in order to add movies, link to genres (movies, genres_in_movies).
public class MainsSAXParser extends DefaultHandler {

    private List<MainsItem> myMovies;
    private String tempVal;
    private MainsItem tempMovie;
    private String currentDirector;

    public MainsSAXParser() {
        myMovies = new ArrayList<>();
    }
    private List<String> inconsistent = new ArrayList<>();

    public void runExample() {
        parseDocument();
        printData();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            FileInputStream fis = new FileInputStream("parse/mains243.xml");
            InputStreamReader isr = new InputStreamReader(fis, "ISO-8859-1");
            sp.parse(new InputSource(isr), this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void printData() {
        for (MainsItem movie : myMovies) {
            System.out.println(movie);
        }
        System.out.println("Number of Main Items: " + myMovies.size());
    }


    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new MainsItem();
            tempMovie.setDirector(currentDirector);
        } else if (qName.equalsIgnoreCase("director")) {
            currentDirector = "";
        }
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal += new String(ch, start, length);
    }


    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equalsIgnoreCase("film")) {
                if (tempMovie.getFilmTitle() != null && tempMovie.getDirector() != null && tempMovie.getYear() != null) {
                    if (tempMovie.getGenres().isEmpty()) {
                        inconsistent.add(tempMovie.toString());
                        System.out.println("Skipping MainsItem due to empty genres list.");
                    } else {
                        try {
                            int year = Integer.parseInt(tempMovie.getYear());
                            myMovies.add(tempMovie);
                        } catch (NumberFormatException e) {
                            System.out.println("Ignoring invalid year value: " + tempVal);
                            inconsistent.add(tempMovie.toString());
                        }
                    }
                } else {
                    inconsistent.add(tempMovie.toString());
                    System.out.println("Skipping MainsItem due to missing film title, director, or year." + tempMovie);
                }
            } else if (qName.equalsIgnoreCase("fid")) {
                tempMovie.setFilmId(tempVal);
            } else if (qName.equalsIgnoreCase("t")) {
                tempMovie.setFilmTitle(tempVal);
            } else if (qName.equalsIgnoreCase("year")) {
                tempMovie.setYear(tempVal.trim());
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
        File outFile = new File("MovieInconsistent.txt");

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
        MainsSAXParser parser = new MainsSAXParser();
        parser.runExample();
        parser.printInconsistentEntries();
    }
}
