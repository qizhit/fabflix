/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

let currentPage = 1;  // Initialize the current page number to 1

function handleStarResult(resultData) {
    console.log("resultdata", resultData);
    console.log("handleStarResult: populating movie table from resultData");
    let MovieTableBodyElement = jQuery("#movie_table_body");
    MovieTableBodyElement.empty();  // Empty old data each time you populate new data

    // Iterate through the movie data and fill the table
    resultData.movies.forEach(movie => {
        let rowHTML = "<tr>";
        rowHTML += `<th><a href='single-movie.html?id=${movie.movie_id}'>${movie.title}</a></th>`;
        rowHTML += `<th>${movie.year}</th>`;
        rowHTML += `<th>${movie.director}</th>`;
        rowHTML += `<th>${movie.genres}</th>`;

        // Split the stars string and create individual hyperlinks
        const stars = movie.stars.split(", ");
        const star_ids = movie.star_ids.split(", ");
        let starLinks = stars.map((name, index) =>
            `<a href='single-star.html?id=${encodeURIComponent(star_ids[index])}'>${name}</a>`
        ).join(", ");
        rowHTML += `<th>${starLinks}</th>`;

        rowHTML += `<th>${movie.rating || 'N/A'}</th>`;
        rowHTML += "</tr>";

        MovieTableBodyElement.append(rowHTML);
    });

    updatePaginationControls(resultData.totalPages);
}

/**
 * Updates the pagination controls (Previous, Next, and Page Info).
 */
function updatePaginationControls(totalPages) {
    document.getElementById("pageInfo").innerText = `${currentPage} / ${totalPages}`;

    document.getElementById("prevPage").disabled = currentPage === 1;
    document.getElementById("nextPage").disabled = currentPage >= totalPages;
}


/**
 * Extracts `genre` or `title` parameters from the URL and constructs the API request URL.
 */
function getQueryParameter() {
    const urlParams = new URLSearchParams(window.location.search); // Extract query params from URL
    const genre = urlParams.get("genre");
    const browseTitle = urlParams.get("browse_title");
    const searchTitle = urlParams.get("title");
    const year = urlParams.get("year");
    const director = urlParams.get("director");
    const star = urlParams.get("star");

    let apiUrl = "api/movie_list?";

    // browsing
    if (genre) {
        apiUrl += `genre=${encodeURIComponent(genre)}&`; // Add genre to API URL
    }
    if (browseTitle) {
        apiUrl += `browse_title=${encodeURIComponent(browseTitle)}&`; // Add browseTitle to API URL
    }
    if (searchTitle) {
        apiUrl += `title=${encodeURIComponent(searchTitle)}&`;
    }
    if (year) {
        apiUrl += `year=${encodeURIComponent(year)}&`;
    }
    if (director) {
        apiUrl += `director=${encodeURIComponent(director)}&`;
    }
    if (star) {
        apiUrl += `star=${encodeURIComponent(star)}&`;
    }

    // Add sort parameter
    const sortSelect = document.getElementById("sortSelect").value;
    let [sortBy, sortOrder1, sortOrder2] = sortSelect.split("-");
    console.log(sortBy, sortOrder1, sortOrder2)
    apiUrl += `sortBy=${sortBy}&sortOrder1=${sortOrder1}&sortOrder2=${sortOrder2}&`;

    // Add paging parameters
    const pageSize = document.getElementById("pageSizeSelect").value;
    apiUrl += `page=${currentPage}&pageSize=${pageSize}`;

    return apiUrl;
}


// Listen for changes to the "Movies per page" option and reload the data.
document.getElementById("pageSizeSelect").addEventListener("change", () => {
    currentPage = 1;  // Reset to page 1 on sort change
    reloadTable();
});

// Listen for changes in sorting options and reload the data.
document.getElementById("sortSelect").addEventListener("change", () => {
    currentPage = 1;  // Reset to page 1 on page size change
    reloadTable();
});

// Event listeners for pagination buttons
document.getElementById("prevPage").addEventListener("click", () => {
    if (currentPage > 1) {
        currentPage--;
        reloadTable();
    }
});

document.getElementById("nextPage").addEventListener("click", () => {
    currentPage++;
    reloadTable();
});

/**
 * Get the data from the API and refresh the table.
 */
function reloadTable() {
    const apiUrl = getQueryParameter();
    console.log("Requesting data from: " + apiUrl);

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiUrl,
        success: (resultData) => handleStarResult(resultData),
    });
}

// Load table data at initialization
reloadTable();