// Goes to "Data Dictionary/Scripts"

logger.log(`document: ${document.displayPath}/${document.name}`);

// Adds ax:ip aspect if it's not there
if (!document.hasAspect("ax:ip"))
{
    document.addAspect("ax:ip");
    logger.log("Aspect ax:ip added to the node");
}

// list of key words to be skipped and not considered as IP Names
var skip = ["other", "OST", "OP", "ED", "Manga PV", "Files", "Anime", "Movies", "Movie", "Game", "Film", "Series", "Music", "Company Home", "Mainstream", "Playlist"];

// Fetches the current list of IPs on the node
var listOfIPs = document.properties["ax:ipName"] || [];
logger.log(`current IPs: ${listOfIPs}.`);

var ipSource = document;

while(ipSource.parent != null && ipSource.parent.name != "Company Home")
{
	ipSource = ipSource.parent;
	
	var newIP = ipSource.name;
	logger.log(`opening parent folder: ${ipSource.displayPath}/${newIP}`);
	
	// Checks if the IP is already in the list
	if(listOfIPs.every(ip => ip.toLowerCase() != newIP.toLowerCase()))
	{// If the folder is named, for instance, "Disc 9 - Future Redeemed", newIP becomes just "Future Redeemed"
		if(/^disc \d+ -/i.test(newIP))
				newIP = newIP.split(" - ")[1].trim();
				
		if(skip.some(word => word.toLowerCase() == newIP.toLowerCase()) || /Disc \d+/i.test(newIP))
			logger.log(`Value "${newIP}" is skipped as it's not an IP.`);
		else
		{// Adds the IP to the list
			listOfIPs.push(newIP);
			logger.log(`Trying to add IP ${newIP} to the node.`);
		}
	}
 	else
 		logger.log(`IP ${newIP} already found in node's IPs`);
}

// applies the list to the actual node.
document.properties["ax:ipName"] = listOfIPs;

// Commits the changes to the node
try
{
	document.save();
	
	if (listOfIPs.every(ip => document.properties["ax:ipName"].includes(ip)))
		logger.log(`Successfully added IPs to ${document.name}'s node`);
	else
		logger.log(`IPs apparently were not saved to ${document.name}'s node`);
} catch (e)
{
    logger.log(`Error trying to save changes to the node: ${e.message}`);
}

logger.log(`new list of IPs: ${listOfIPs}.`);
