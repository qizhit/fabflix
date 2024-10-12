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
// function handleStarResult(resultData) {
//     console.log("handleStarResult: populating star table from resultData");
//
//     // Populate the star table
//     // Find the empty table body by id "star_table_body"
//     let starTableBodyElement = jQuery("#star_table_body");
//
//     // Iterate through resultData, no more than 10 entries
//     for (let i = 0; i < Math.min(20, resultData.length); i++) {
//
//         // Concatenate the html tags with resultData jsonObject
//         let rowHTML = "";
//         rowHTML += "<tr>";
//         rowHTML +=
//             "<th>" +
//             // Add a link to single-star.html with id passed with GET url parameter
//             '<a href="single-star.html?id=' + resultData[i]['star_id'] + '">'
//             + resultData[i]["star_name"] +     // display star_name for the link text
//             '</a>' +
//             "</th>";
//         rowHTML += "<th>" + resultData[i]["star_dob"] + "</th>";
//         rowHTML += "</tr>";
//         // Append the row created to the table body, which will refresh the page
//         starTableBodyElement.append(rowHTML);
//     }
// }

function handleStarResult(resultData) {
    console.log("handleStarResult: populating movie table from resultData");
    let MovieTableBodyElement = jQuery("#movie_table_body");

    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th><a href='single-movie.html?id=" + resultData[i]['movie_id'] + "'>" + resultData[i]["title"] + "</a></th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["genres"] + "</th>";

        // Split the stars string into an array and create individual hyperlinks
        let starsArray = resultData[i]["stars"].split(", ");
        let starsHTML = starsArray.map(star => `<a href="single-star.html?id=${encodeURIComponent(star.trim())}">${star.trim()}</a>`).join(", ");
        rowHTML += "<th>" + starsHTML + "</th>";

        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += "</tr>";

        MovieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie_list", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});