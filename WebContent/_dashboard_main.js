/**
 * Get the data from the API and refresh the table.
 */
function reloadTable() {
    const apiUrl = getQueryParameter();  // Ensure this function returns the API URL
    console.log("Requesting data from: " + apiUrl);

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiUrl,
        success: (resultData) => handleMovieResult(resultData),
        error: (jqXHR, textStatus, errorThrown) => {
            console.error("Error fetching data:", textStatus, errorThrown);
            jQuery("#metadata-container").html("<p class='text-danger'>Error loading metadata. Please try again.</p>");
        }
    });
}

/**
 * Process the API response data and display it in HTML.
 */
function handleMovieResult(resultData) {
    const metadataContainer = jQuery("#metadata-container");
    metadataContainer.empty();  // Clear any existing content

    // Iterate over each table in the result data
    resultData.forEach((table) => {
        // Create a title for the table
        const tableTitle = `<h2 class="text-primary">Table: ${table.tableName}</h2>`;

        // Create a Bootstrap-styled table for columns metadata
        let tableHTML = `
            <table class="table table-bordered table-striped mb-4">
                <thead>
                    <tr>
                        <th>Column Name</th>
                        <th>Data Type</th>
                    </tr>
                </thead>
                <tbody>
        `;

        // Populate the table with each column's name and data type
        table.columns.forEach((column) => {
            tableHTML += `
                <tr>
                    <td>${column.columnName}</td>
                    <td>${column.dataType}</td>
                </tr>
            `;
        });

        tableHTML += `</tbody></table>`;

        // Append the complete table to the metadata container
        metadataContainer.append(tableTitle + tableHTML);
    });
}

/**
 * Gets the API URL based on query parameters or default URL.
 * Modify this function as needed to set the correct API endpoint.
 */
function getQueryParameter() {
    // Define the API URL here
    const apiUrl = "http://localhost:8080/cs122b_project_war/api/_dashboard_main";  // Update this URL to your actual API endpoint
    return apiUrl;
}

// Call reloadTable on page load
jQuery(document).ready(() => {
    reloadTable();
});
