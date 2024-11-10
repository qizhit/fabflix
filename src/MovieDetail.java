import java.util.List;

public class MovieDetail {
    private String filmTitle;
    private int year;
    private String director;
    private List<String> genres;
    private List<String> stars;

    public MovieDetail(String filmTitle, int year, String director, List<String> genres, List<String> stars) {
        this.filmTitle = filmTitle;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
    }

    public String getFilmTitle() {
        return filmTitle;
    }

    public int getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<String> getStars() {
        return stars;
    }

    public String toString() {
        return "MovieDetail{" +
                "filmTitle='" + filmTitle + '\'' +
                ", year=" + year +
                ", director='" + director + '\'' +
                ", genres=" + genres +
                ", stars=" + stars +
                '}';
    }
}
