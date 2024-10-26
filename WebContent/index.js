/**
 * Process the JSON data returned from the Servlet and fill genres and titles into HTML pages.
 */
function handleMainResult(resultData) {
    console.log("handleMainResult: populating genre names and titles from resultData");

    // Parse and fill genres
    let genreListElement = jQuery("#genre-list");
    let genres = resultData["genres"]; // Gets a genres array from JSON data
    genres.forEach((genre) => {
        let genreLink = `<a href="movie-list?genre=${encodeURIComponent(genre)}">${genre}</a> `;
        genreListElement.append(genreLink); // Add a genre hyperlink dynamically
    });

    // Parse and fill titles (0-9, A-Z, *)
    let titleListElement = jQuery("#title-list");
    let titles = resultData["titles"]; // Gets an array of titles from the JSON data
    titles.forEach((title) => {
        let titleLink = `<a href="movie-list?title=${encodeURIComponent(title)}">${title}</a> `;
        titleListElement.append(titleLink); // Add a title hyperlink dynamically
    });
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres-titles",
    success: (resultData) => handleMainResult(resultData)
});