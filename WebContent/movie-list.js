/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

let currentPage = 1;   // Track current page
let pageSize = 10;     // Default page size
let currentSortOption = "title";
let currentSortOrder = "ASC";

function handleStarResult(resultData) {
    console.log("handleStarResult: populating movie table from resultData");
    let MovieTableBodyElement = jQuery("#movie_table_body");
    MovieTableBodyElement.empty();

    // Populate the table with movie data from resultData
    resultData.forEach((movie) => {
        let rowHTML = "<tr>";

        // Title linked to the single movie page
        rowHTML += `<th><a href='single-movie.html?id=${movie['movie_id']}'>${movie["title"]}</a></th>`;
        rowHTML += `<th>${movie["year"]}</th>`;
        rowHTML += `<th>${movie["director"]}</th>`;

        let genres = movie["genres"].map(genre => genre["genre_name"]).join(", ");
        rowHTML += `<th>${genres}</th>`;

        let starLinks = movie["stars"].map(star => {
            return `<a href='single-star.html?id=${star["star_id"]}'>${star["star_name"]}</a>`;
        }).join(", ");
        rowHTML += `<th>${starLinks}</th>`;

        // Create hyperlinks for each star name with their IDs
        // const stars = movie["stars"].split(", ");
        // const star_ids = movie["star_ids"].split(", ");
        // let starLinks = stars.map((name, index) => {
        //     const encodedStarId = encodeURIComponent(star_ids[index]);
        //     return `<a href='single-star.html?id=${encodedStarId}'>${name}</a>`;
        // }).join(", ");
        // rowHTML += `<th>${starLinks}</th>`;

        // Rating
        rowHTML += `<th>${movie["rating"]}</th>`;
        rowHTML += "</tr>";

        // Append the row HTML to the table body
        MovieTableBodyElement.append(rowHTML);
    });

    //
    // for (let i = 0; i < Math.min(20, resultData.length); i++) {
    //     let rowHTML = "";
    //     rowHTML += "<tr>";
    //     rowHTML += "<th><a href='single-movie.html?id=" + resultData[i]['movie_id'] + "'>" + resultData[i]["title"] + "</a></th>";
    //     rowHTML += "<th>" + resultData[i]["year"] + "</th>";
    //     rowHTML += "<th>" + resultData[i]["director"] + "</th>";
    //     rowHTML += "<th>" + resultData[i]["genres"] + "</th>";
    //
    //     // Split the stars string and create individual hyperlinks
    //     const stars = resultData[i]["stars"].split(", ");
    //     const star_ids = resultData[i]["star_ids"].split(", ");
    //     // Create individual hyperlinks for each star name
    //     let starLinks = stars.map((name, index) => {
    //         const encodedStarId = encodeURIComponent(star_ids[index]); // Encode star ID
    //         return `<a href='single-star.html?id=${encodedStarId}'>${name}</a>`;
    //     }).join(", ");
    //     rowHTML += "<th>" + starLinks + "</th>";
    //
    //     rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
    //     rowHTML += "</tr>";
    //
    //     MovieTableBodyElement.append(rowHTML);
    // }
}

function loadMovies(sortOption = "title", sortOrder = "ASC", page = 1, pageSize = 10) {
    // Makes the HTTP GET request with sort parameters and registers the handleStarResult callback
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: `api/movie_list?page=${currentPage}&size=${pageSize}&sortOption=${sortOption}&sortOrder=${sortOrder}`,
        success: (resultData) => {
            handleStarResult(resultData);
            updatePageInfo();
        }
    });
}
function updatePageInfo() {
    // Updates the page info display
    jQuery("#pageInfo").text(`Page ${currentPage}`);
}

jQuery(document).ready(() => {
    loadMovies();

    // Event listeners for sorting options
    jQuery("#sort-title-asc").click(() => {
        sortOption = "title";
        sortOrder = "ASC";
        currentPage = 1; // Reset to first page
        loadMovies();
    });

    jQuery("#sort-title-desc").click(() => {
        sortOption = "title";
        sortOrder = "DESC";
        currentPage = 1;
        loadMovies();
    });

    jQuery("#sort-rating-asc").click(() => {
        sortOption = "rating";
        sortOrder = "ASC";
        currentPage = 1;
        loadMovies();
    });

    jQuery("#sort-rating-desc").click(() => {
        sortOption = "rating";
        sortOrder = "DESC";
        currentPage = 1;
        loadMovies();
    });

    // Event listener for movies per page dropdown
    jQuery("#pageSizeSelect").change(() => {
        pageSize = parseInt(jQuery("#pageSizeSelect").val());
        currentPage = 1;
        loadMovies();
    });

    // Pagination controls
    jQuery("#prevPage").click(() => {
        if (currentPage > 1) {
            currentPage--;
            loadMovies();
        }
    });

    jQuery("#nextPage").click(() => {
        currentPage++;
        loadMovies();
    });
});
/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
// jQuery.ajax({
//     dataType: "json", // Setting return data type
//     method: "GET", // Setting request method
//     url: "api/movie_list", // Setting request url, which is mapped by MovieListServlet in MovieListServlet.java
//     success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
// });