// Goes to "Data Dictionary/Scripts"

var contentReader = document.content;
var metadata = contentReader ? contentReader.metadata : null;

if (metadata)
{
    // Extracts available metadata
    var title = metadata["title"] || document.name.replace(/\.[^\.]+$/, ""); // Title or File Name (minus extension)
    var album = metadata["album"];
    var genre = metadata["genre"];
    var year = metadata["year"] ? parseInt(metadata["year"], 10) : null;
    var discNumber = metadata["disc_number"] ? parseInt(metadata["disc_number"], 10) : null;
    var trackNumber = metadata["track_number"] ? parseInt(metadata["track_number"], 10) : null;

    // Metadata Injection
    document.properties["ax:songTitle"] = title;
    
    if (album)
    	document.properties["ax:album"] = album;
    
    if (genre)
    	document.properties["ax:genre"] = genre;
    
    if (year)
    	document.properties["ax:year"] = year;
    
    if (discNumber)
    	document.properties["ax:discNumber"] = discNumber;
    
    if (trackNumber)
    	document.properties["ax:trackNumber"] = trackNumber;

    // Validates file update
    document.save();
} else
{
    logger.log("No metadata available for file " + document.name);
}
