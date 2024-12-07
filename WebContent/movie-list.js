/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

let currentPage = 1;  // Initialize the current page number to 1
let apiUrlInfo = "";

function handleMovieResult(resultData) {
    console.log("resultdata", resultData);
    console.log("handleMovieResult: populating movie table from resultData");
    let MovieTableBodyElement = jQuery("#movie_table_body");
    MovieTableBodyElement.empty();  // Empty old data each time you populate new data

    // Iterate through the movie data and fill the table
    resultData.movies.forEach(movie => {
        let rowHTML = "<tr>";
        rowHTML += `<th><a href='single-movie.html?id=${movie.movieId}'>${movie.title || 'N/A'}</a></th>`;
        rowHTML += `<th>${movie.year || 'N/A'}</th>`;
        rowHTML += `<th>${movie.director || 'N/A'}</th>`;

        // Split the genres string and create individual hyperlinks
        let genreLinks = "N/A";
        if (movie.genres !== "N/A") {
            let genreNames = movie.genres.split(", ");
            genreLinks = genreNames.map(genre => {
                return `<a href='movie-list.html?browse_genre=${encodeURIComponent(genre.trim())}'>${genre}</a>`;
            }).join(", ");
        }
        rowHTML += `<th>${genreLinks}</th>`;

        // Split the stars string and create individual hyperlinks
        let starLinks = "N/A";
        if (movie.stars !== "N/A" &&  movie.star_ids !== "N/A") {
            const starNames = movie.stars.split(", ");
            const starIdArray = movie.star_ids.split(", ");
            starLinks = starNames.map((name, index) => {
                const encodedStarId = encodeURIComponent(starIdArray[index]);
                return `<a href='single-star.html?id=${encodedStarId}'>${name}</a>`;
            }).join(", ");
        }
        rowHTML += "<th>" + starLinks + "</th>";  // Stars

        rowHTML += `<th>${movie.rating || 'N/A'}</th>`;
        // Add to Cart
        rowHTML += `<th class="text-center align-middle">
                    <button class="btn btn-success add-to-cart" data-id="${movie.movieId}" data-title="${movie.title}" data-price="${movie.price}">Add</button></th>`;
        rowHTML += "</tr>";

        MovieTableBodyElement.append(rowHTML);
    });

    if (resultData.isSessionData === true) {
        currentPage = resultData.page;
        document.getElementById("sortSelect").value = `${resultData.sortBy}-${resultData.sortOrder1}-${resultData.sortOrder2}`;
        document.getElementById("pageSizeSelect").value = resultData.pageSize;
    }

    updateSortPaginationControls(resultData.totalPages);

    apiUrlInfo = concatenateUrl(resultData.browseGenre, resultData.browseTitle,
        resultData.searchTitle, resultData.year, resultData.director, resultData.star);
}

// /**
//  * Event handler for "Add to Cart" button
//  */
$(document).on('click', '.add-to-cart', function () {
    const movieId = $(this).data('id');
    const movieTitle = $(this).data('title');
    const moviePrice = $(this).data('price');

    $.ajax({
        url: "api/checkout",
        method: "POST",
        data: {
            action: "add",
            movieId: movieId,
            title: movieTitle,
            price: moviePrice,
            quantity: 1  // By default, an amount is added
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
 * Updates the pagination controls (Previous, Next, and Page Info).
 */
function updateSortPaginationControls(totalPages) {
    document.getElementById("pageInfo").innerText = `${currentPage} / ${totalPages}`;
    document.getElementById("prevPage").disabled = currentPage === 1;
    document.getElementById("nextPage").disabled = currentPage >= totalPages;
}


function concatenateUrl(browseGenre, browseTitle, searchTitle, year, director, star) {
    let apiUrlInfo = "";
    // browsing
    if (browseGenre) {
        apiUrlInfo += `browse_genre=${encodeURIComponent(browseGenre)}&`; // Add genre to API URL
    }
    if (browseTitle) {
        apiUrlInfo += `browse_title=${encodeURIComponent(browseTitle)}&`; // Add browseTitle to API URL
    }
    if (searchTitle) {
        apiUrlInfo += `title=${encodeURIComponent(searchTitle)}&`;
    }
    if (year) {
        apiUrlInfo += `year=${encodeURIComponent(year)}&`;
    }
    if (director) {
        apiUrlInfo += `director=${encodeURIComponent(director)}&`;
    }
    if (star) {
        apiUrlInfo += `star=${encodeURIComponent(star)}&`;
    }

    return apiUrlInfo;
}

/**
 * Extracts `genre` or `title` parameters from the URL and constructs the API request URL.
 */
function getQueryParameter() {
    const urlParams = new URLSearchParams(window.location.search); // Extract query params from URL
    let apiUrl = "api/movie_list?";

    if (urlParams.toString().length === 0) {
        apiUrl += apiUrlInfo;
    } else {
        const browseGenre = urlParams.get("browse_genre");
        const browseTitle = urlParams.get("browse_title");
        const searchTitle = urlParams.get("title");
        const year = urlParams.get("year");
        const director = urlParams.get("director");
        const star = urlParams.get("star");

        apiUrl += concatenateUrl(browseGenre, browseTitle, searchTitle, year, director, star)
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
        success: (resultData) => handleMovieResult(resultData),
    });
}

reloadTable();