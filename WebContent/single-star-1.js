/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    jQuery("#star_name").text(resultData["star_name"]);
    jQuery("#star_dob").text(resultData["star_birthYear"]);

    let moviesListElement = jQuery("#star_movies_list");
    let movies = resultData["movies"].split(', ');

    //Iterate all movies that acted by this star
    for (let i = 0; i < movies.length; i++) {
        let rowHTML = "<li>" + movies[i] + "</li>";
        moviesListElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/stars?id=" + new URLSearchParams(window.location.search).get("id"), // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});