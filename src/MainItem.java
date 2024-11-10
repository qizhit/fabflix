import java.util.ArrayList;

public class MainItem {
    private String filmId;        // <fid>
    private String filmTitle;     // <t>
    private int year;             // <year>
    private String director;      // <dirname>
    private ArrayList<String> genres; // <cats> containing <cat>

    public MainItem() {
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
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
        this.genres.add(genre);
    }

    @Override
    public String toString() {
        return "MainItem Details - " +
                "Film ID: " + filmId + ", " +
                "Film Title: " + filmTitle + ", " +
                "Year: " + year + ", " +
                "Director: " + director + ", " +
                "Genres: " + genres + ".";
    }
}
