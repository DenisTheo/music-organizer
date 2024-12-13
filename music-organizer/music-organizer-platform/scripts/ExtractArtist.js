// Goes to "Data Dictionary/Scripts"

// Function to load and parse the CSV file
function loadListFromFile(filePath)
{
    var file = companyhome.childByNamePath(filePath);
    if (!file || !file.exists())
    {
        logger.log("File not Found: " + filePath);
        return [];
    }
    var content = file.content;
    return content.split("\n").map(function (line)
    {
        return line.trim().split(";");
    });
}

// Function to normalize strings for comparison
function normalizeString(value)
{
	// Keeps alphanumeric of all languages, and some symbols, but removes everything else, including spaces
    return value.toLowerCase().replace(/[^a-z0-9+@\p{L}\p{N}\p{P}\p{S}]/gu, "");
}

// Function to find all matches in the options list
function findAllMatches(value, optionsList)
{
    value = normalizeString(value);
    var matches = [];

    optionsList.forEach(function (options)
    {
        for (var i=0; i<options.length; i++)
            if (normalizeString(options[i]) === value)
            {// if a row of info has an entry matching the value,
                matches.push(options[0]); // Adds the first entry of the matching line to a list of matches
                break; // Stops further matching on this line only
            }
    });

    return matches;
}

// Function to add values to a multivalued property
function addValuesToProperty(document, aspect, propertyName, values)
{
    if (!document.hasAspect(aspect))
        document.addAspect(aspect);

    var currentValues = document.properties[propertyName] || [];
    values.forEach(function (value)
    {
        if (currentValues.indexOf(value) === -1)
        {
            currentValues.push(value);
        }
    });

    document.properties[propertyName] = currentValues;
}

// Function to process artists and composers
function processArtistsAndComposers(document, metadataValue, optionsList, aspect, propertyName, unmatchedValues)
{
    if (metadataValue && metadataValue.trim() !== "")
    {
        var individualValues = metadataValue.split(",").map(function (item)
        {
            return item.trim();
        });

        individualValues.forEach(function (individualValue)
        {
            var matches = findAllMatches(individualValue, optionsList);

            if (matches.length > 0)
                addValuesToProperty(document, aspect, propertyName, matches);
            else
                unmatchedValues.push(individualValue);
        });
    }
}

// Load known composers and singers
var dataFolder = "Data Dictionary/Music/"; // Edit to relocate data files
var knownComposers = loadListFromFile(dataFolder + "composers.csv");
var knownSingers = loadListFromFile(dataFolder + "vocal_artists.csv");

// Read metadata from the file
var contentReader = document.content;
var metadata = contentReader ? contentReader.metadata : null;

if (metadata)
{
    var unmatchedComposers = [];
    var unmatchedSingers = [];

    // Process composers
    processArtistsAndComposers(
        document,
        metadata["composer"],
        knownComposers,
        "ax:composer",
        "ax:composerName",
        unmatchedComposers
    );

    // Process vocal artists
    processArtistsAndComposers(
        document,
        metadata["artist"],
        knownSingers,
        "ax:singer",
        "ax:singerName",
        unmatchedSingers
    );

    // Log unmatched values
    if (unmatchedComposers.length > 0)
        logger.log("Unmatched composers for file " + document.name + ": " + unmatchedComposers.join(", "));

    if (unmatchedSingers.length > 0)
        logger.log("Unmatched singers for file " + document.name + ": " + unmatchedSingers.join(", "));

    // Save the document
    document.save();
} else
{
    logger.log("No metadata found for file " + document.name);
}
