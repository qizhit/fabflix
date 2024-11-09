/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("handleResult: populating star info from resultData");

    let birthYear = resultData[0]["birth_year"] || "N/A";
    let movies = resultData[0]["movies"] || "N/A";
    let movieIds = resultData[0]["movie_ids"] || "N/A";

    // populate the movie info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>" + resultData[0]["name"] + "</p>");

    console.log("handleResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + resultData[0]["name"] + "</th>"; // Name
    rowHTML += "<th>" + birthYear + "</th>";  // Birth Year
    // Movies as hyperlinks
    let movieLinks = "N/A";
    if (movies !== "N/A" && movieIds !== "N/A") {
        const movieTitles = movies.split(", ");
        const movieIdArray = movieIds.split(", ");
        movieLinks = movieTitles.map((title, index) => {
            const encodedMovieId = encodeURIComponent(movieIdArray[index]);
            return `<a href='single-movie.html?id=${encodedMovieId}'>${title}</a>`;
        }).join(", ");
    }
    rowHTML += "<th>" + movieLinks + "</th>";  // Movies
    rowHTML += "</tr>";

    // Append the row to the table
    starTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by SingleStarServlet.
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});