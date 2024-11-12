// ---- Playlist Name ---------------------------------------------------------

var playlistName = args.name;

if(playlistName == null) // Makes the variable name optional
{
	var now = new Date();
	// Current Date with format "YYYY-MM-DD hh-mm-ss"
	playlistName = `${now.getFullYear()}-${("0"+(now.getMonth()+1)).slice(-2)}-${("0"+now.getDate()).slice(-2)} ${("0"+now.getHours()).slice(-2)}-${("0"+now.getMinutes()).slice(-2)}-${("0"+now.getSeconds()).slice(-2)}`;
}	

// ---- Query -----------------------------------------------------------------

logger.log(`QUERY: \"${args.query}\"`);

var maxSearchResults = 10000;
var paths = [];

var result = search.query({query: args.query,
			store: "workspace://SpacesStore",
			language: "fts-alfresco",
			page: {maxItems: maxSearchResults,
				skipCount: 0}});
	
logger.log(`${result.length} corresponding Music files found.`);

// ---- File Processing -------------------------------------------------------

if(result.length != 0)
{
	// compiling paths
	for (var i=0; i<result.length; i++)
	{
	    var filePath = result[i].nodeRef;
	    logger.log(`path to found file: ${filePath}`);
	    paths.push(filePath);
	}
	
	// playlist file content generation
	model.paths = paths;
	
	var playlistContent = '<?xml version="1.0" encoding="UTF-8"?>\n';
	playlistContent += '<playlist version="1" xmlns="http://xspf.org/ns/0/">\n';
	playlistContent += '    <trackList>\n';
	
	for (var i=0; i<paths.length; i++)
	{
	    playlistContent += '        <track>\n';
	    playlistContent += '            <location>' + paths[i] + '</location>\n';
	    playlistContent += '        </track>\n';
	}
		
	playlistContent += '    </trackList>\n';
	playlistContent += '</playlist>\n';
	
	// manage hosting folder
	var playlistFolder = companyhome.childByNamePath("Music");
	if (playlistFolder.childByNamePath("Playlists") == null)
	    playlistFolder.createFolder("Playlists");
	playlistFolder = playlistFolder.childByNamePath("Playlists");
	
	// create playlist file
	var playlistFile = playlistFolder.createFile(`${playlistName}.xspf`);
	playlistFile.properties.content.content = playlistContent;
	playlistFile.mimetype = "application/xspf+xml";
	playlistFile.save();
	
	logger.log(`Playlist ${playlistName}.xspf created at: ${playlistFile.nodeRef}`);
}
else
	logger.log("No suitable music file found, the playlist was not created.");
	