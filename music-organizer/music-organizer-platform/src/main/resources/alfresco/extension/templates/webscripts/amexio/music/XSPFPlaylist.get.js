var maxSearchResults = 10000;
var paths = [];

logger.log('QUERY: \"' + args.query + '\"');

var result = search.query({query: args.query,
			store: "workspace://SpacesStore",
			language: "fts-alfresco",
			page: {maxItems: maxSearchResults,
				skipCount: 0}});

logger.log(result.length + " Music files found.");

for (var i = 0; i < result.length; i++)
{
    var n = result[i];
    //n.properties["ax:path"] = "C:\\Users\\tdenis\\Music\\Mainstream\\Ronnie James Dio\\(1983) Holy Diver 320\\" + n.name;
	//n.save();
    logger.log(n.displayPath + "/" + n.name + " - " + n.nodeRef + " - " + n.properties["ax:path"]); 
    paths.push(n.properties["ax:path"]);
}

logger.log("Document - NodeRef - Path");
logger.log(paths);

/*var playlistContent = '<?xml version="1.0" encoding="UTF-8"?>\n';
playlistContent += '<playlist version="1" xmlns="http://xspf.org/ns/0/">\n';
playlistContent += '  <trackList>\n';

for (var i = 0; i < paths.length; i++) {
    playlistContent += '    <track>\n';
    playlistContent += '      <location>' + paths[i] + '</location>\n';
    playlistContent += '    </track>\n';
}

playlistContent += '  </trackList>\n';
playlistContent += '</playlist>\n';*/

model.paths = paths;

/*var playlistFolder = companyhome.childByNamePath("Music/Playlists");
if (playlistFolder == null)
    playlistFolder = companyhome.createFolder("Music/Playlists");

var playlistFile = playlistFolder.createFile("playlist.xspf");
playlistFile.properties.content.write(playlistContent);
playlistFile.mimetype = "application/xspf+xml";
playlistFile.save();

logger.log("Playlist created at: " + playlistFile.nodeRef)

*/
