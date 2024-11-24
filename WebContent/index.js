/**
 * Process the JSON data returned from the Servlet and fill genres and titles into HTML pages.
 */
function handleMainResult(resultData) {
    console.log("handleMainResult: populating genre names and titles from resultData");

    // Parse and fill genres
    let genreListElement = jQuery("#genre-list");
    let genres = resultData["genres"]; // Gets a genres array from JSON data
    genres.forEach((genre) => {
        let genreLink = `<a href="movie-list.html?browse_genre=${encodeURIComponent(genre)}">${genre}</a> `;
        genreListElement.append(genreLink); // Add a genre hyperlink dynamically
    });

    // Parse and fill titles (0-9, A-Z, *)
    let titleListElement = jQuery("#title-list");
    let titles = resultData["titles"]; // Gets an array of titles from the JSON data
    titles.forEach((title) => {
        let titleLink = `<a href="movie-list.html?browse_title=${encodeURIComponent(title)}">${title}</a> `;
        titleListElement.append(titleLink); // Add a title hyperlink dynamically
    });
}

/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // Check if the query results exist in sessionStorage
    let cachedResult = sessionStorage.getItem(query);
    if (cachedResult) {
        console.log("Using cached result for query:", query);
        console.log(JSON.parse(cachedResult))
        doneCallback({ suggestions: JSON.parse(cachedResult) });
        return;
    }

    // If not cached, send AJAX request to the backend
    console.log("sending AJAX request to backend Java Servlet for query:", query)
    jQuery.ajax({
        "method": "GET",
        // escape the query string to avoid errors caused by special characters
        "url": "api/movie-suggestion?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    var jsonData = JSON.parse(data);
    console.log(jsonData)

    // Cache the result in sessionStorage
    sessionStorage.setItem(query, JSON.stringify(jsonData));

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // Jump to the specific result page based on the selected suggestion
    console.log("Jumping to single movie page " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"])
    let url = "single-movie.html?id="+suggestion["data"]["movieId"];
    console.log(url);
    window.location.replace(url);
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // minimum characters = 3
    minChars:3,
    orientation: top
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch (query) {
    // search by genres or first character
    console.log("Performing normal search for query: " + query);
    // Gets the value entered by the user
    const title = document.querySelector('input[name="title"]').value.trim();
    const year = document.querySelector('input[name="year"]').value.trim();
    const director = document.querySelector('input[name="director"]').value.trim();
    const star = document.querySelector('input[name="star"]').value.trim();

    const url = `movie-list.html?title=${encodeURIComponent(title)}&year=${encodeURIComponent(year)}&director=${encodeURIComponent(director)}&star=${encodeURIComponent(star)}`;
    window.location.replace(url);
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

// Bind the Search button click event to handleNormalSearch
jQuery("#search-form").submit(function(event) {
    event.preventDefault();
    console.log("Search button clicked");
    handleNormalSearch($('#autocomplete').val());
});


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres-titles",
    success: (resultData) => handleMainResult(resultData)
});