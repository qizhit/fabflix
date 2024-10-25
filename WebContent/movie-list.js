/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

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

        // Split the stars string and create individual hyperlinks
        const stars = resultData[i]["stars"].split(", ");
        const star_ids = resultData[i]["star_ids"].split(", ");
        // Create individual hyperlinks for each star name
        let starLinks = stars.map((name, index) => {
            const encodedStarId = encodeURIComponent(star_ids[index]); // Encode star ID
            return `<a href='single-star.html?id=${encodedStarId}'>${name}</a>`;
        }).join(", ");
        rowHTML += "<th>" + starLinks + "</th>";

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
    url: "api/movie_list", // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});