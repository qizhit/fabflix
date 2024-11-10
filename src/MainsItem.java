import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainsItem {
    private String filmId;        // <fid>
    private String filmTitle;     // <t>
    private String year;             // <year>
    private String director;      // <dirname>
    private ArrayList<String> genres; // <cats> containing <cat>
    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();
    static {
        CATEGORY_MAP.put("ctxx", "Uncategorized");
        CATEGORY_MAP.put("actn", "Violence");
        CATEGORY_MAP.put("camp", "Now - Camp");
        CATEGORY_MAP.put("comd", "Comedy");
        CATEGORY_MAP.put("disa", "Disaster");
        CATEGORY_MAP.put("epic", "Epic");
        CATEGORY_MAP.put("horr", "Horror");
        CATEGORY_MAP.put("noir", "Black");
        CATEGORY_MAP.put("scfi", "Sci-Fi");
        CATEGORY_MAP.put("west", "Western");
        CATEGORY_MAP.put("advt", "Adventure");
        CATEGORY_MAP.put("avga", "Avant Garde");
        CATEGORY_MAP.put("cart", "Cartoon");
        CATEGORY_MAP.put("cnr", "Cops and Robbers");
        CATEGORY_MAP.put("docu", "Documentary");
        CATEGORY_MAP.put("faml", "Family");
        CATEGORY_MAP.put("musc", "Musical");
        CATEGORY_MAP.put("porn", "Pornography");
        CATEGORY_MAP.put("surl", "Surreal");
        CATEGORY_MAP.put("dram", "Drama");
        CATEGORY_MAP.put("hist", "History");
        CATEGORY_MAP.put("myst", "Mystery");
        CATEGORY_MAP.put("romt", "Romantic");
        CATEGORY_MAP.put("susp", "Thriller");

    }

    public MainsItem() {
        genres = new ArrayList<>();
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getFilmTitle() {
        return filmTitle;
    }

    public void setFilmTitle(String filmTitle) {
        this.filmTitle = filmTitle;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public void addGenre(String genre) {
        String normalizedGenreCode = genre.toLowerCase();
        String newGenre = CATEGORY_MAP.getOrDefault(normalizedGenreCode, genre);
        String genreResult = newGenre.substring(0, 1).toUpperCase() + newGenre.substring(1);
        this.genres.add(genreResult);
    }


    public String toString() {
        return "MainsItem - " +
                "Film ID: " + filmId + ", " +
                "Film Title: " + filmTitle + ", " +
                "Year: " + year + ", " +
                "Director: " + director + ", " +
                "Genres: " + genres + ".";
    }
}
