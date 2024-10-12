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
    // Split the stars string and create individual hyperlinks
    const stars = resultData[0]["stars"].split(", ");
    const star_ids = resultData[0]["star_ids"].split(", ");
    // Create individual hyperlinks for each star name
    let starLinks = stars.map((name, index) => {
        const encodedStarId = encodeURIComponent(star_ids[index]); // Encode star ID
        return `<a href='single-star.html?id=${encodedStarId}'>${name}</a>`;
    }).join(", ");
    rowHTML += "<th>" + starLinks + "</th>";  // Stars
    rowHTML += "<td>" + resultData[0]["rating"] + "</td>";  // Rating
    rowHTML += "</tr>";

    // Append the row to the table
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});