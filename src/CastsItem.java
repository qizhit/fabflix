import java.util.ArrayList;

public class CastsItem {
    private String filmId;          // f
    private String filmTitle;       // t
    private ArrayList<String> starStageNames; // list of <a> - List of star stage names
    private String director;        // is - Director's name

    public CastsItem() {
        starStageNames = new ArrayList<>();
    }

    public CastsItem(String filmId, String filmTitle, ArrayList<String> starStageNames, String director) {
        this.filmId = filmId;
        this.filmTitle = filmTitle;
        this.starStageNames = starStageNames;
        this.director = director;
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

    public ArrayList<String> getStarStageNames() {
        return starStageNames;
    }

    public void addStarStageName(String starStageName) {
        this.starStageNames.add(starStageName);
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String toString() {
        return "CastsItem Details - " +
                "Film ID: " + filmId + ", " +
                "Film Title: " + filmTitle + ", " +
                "Star Stage Names: " + starStageNames + ", " +
                "Director: " + director + ".";
    }
}
