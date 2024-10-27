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
    rowHTML += "<th>" + resultData[0]["title"] + "</th>"; // Title
    rowHTML += "<th>" + resultData[0]["year"] + "</th>";  // Year
    rowHTML += "<th>" + resultData[0]["director"] + "</th>";  // Director

    // Genres as hyperlinks, sorted alphabetically
    let genres = resultData[0]["genres"].split(", ");
    let genreLinks = genres.map(genre => {
        return `<a href='movie-list.html?browse_genre=${encodeURIComponent(genre)}'>${genre}</a>`;
    }).join(", ");
    rowHTML += `<th>${genreLinks}</th>`;

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
    rowHTML += "<th>" + resultData[0]["rating"] + "</th>";  // Rating
    // Add to Cart
    rowHTML += `<th class="text-center align-middle">
                <button class="btn btn-success add-to-cart" 
                data-id="${resultData[0]["movie_id"]}" data-title="${resultData[0]["title"]}" data-price="${resultData[0]["price"]}">Add</button></th>`;
    rowHTML += "</tr>";

    // Append the row to the table
    movieTableBodyElement.append(rowHTML);
}

// /**
//  * Event handler for "Add to Cart" button
//  */
$(document).on('click', '.add-to-cart', function () {
    const movieId = $(this).data('id');
    const movieTitle = $(this).data('title');
    const moviePrice = $(this).data('price');
    console.log("print button data");
    console.log(movieId);
    console.log(movieTitle);
    console.log(moviePrice)

    $.ajax({
        url: "api/checkout",
        method: "POST",
        data: {
            action: "add",
            movieId: movieId,
            title: movieTitle,
            price: moviePrice,
            quantity: 1
        },
        success: function (response) {
            alert(`"${movieTitle}" added to cart!`);
            console.log(`Movie with ID: "${movieId}" added to cart.`);
        },
        error: function () {
            alert("Failed to add movie to cart. Please try again.");
        }
    });
});

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by SingleMovieServlet.
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});