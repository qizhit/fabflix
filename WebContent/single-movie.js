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

    console.log("handleResult: populating movie info from resultData");

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>" + resultData[0]["title"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<td>" + resultData[0]["title"] + "</td>"; // Title
    rowHTML += "<td>" + resultData[0]["year"] + "</td>";  // Year
    rowHTML += "<td>" + resultData[0]["director"] + "</td>";  // Director
    rowHTML += "<td>" + resultData[0]["genres"] + "</td>";  // Genres
    // Stars as hyperlinks
    let starsArray = resultData[0]["stars"].split(", ");
    // let starsHTML = starsArray.map(star => `<a href="single-star.html?id=${encodeURIComponent(star.trim())}">${star.trim()}</a>`).join(", ");
    let starsHTML = starsArray
        .map(star => `<a href="single-star.html?name=${encodeURIComponent(star.trim())}">${star.trim()}</a>`)
        .join(", ");
    rowHTML += "<th>" + starsHTML + "</th>";  // Stars
    rowHTML += "<td>" + resultData[0]["rating"] + "</td>";  // Rating
    rowHTML += "</tr>";

    // Append the row to the table
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieTitle = getParameterByName('title');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?title=" + movieTitle, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});