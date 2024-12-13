package fr.amexio.music;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * WebScript used to generate an XSPF playlist based on a search query.
 * This WS fetches music files from the repository, creates the corresponding playlist,
 * and returns a model with links to the files in the alfresco repository.
 */
public class XSPFPlaylist extends DeclarativeWebScript
{
	//@Autowired
	private SearchService searchService;

	//@Autowired
	private ContentService contentService;

	//@Autowired
	private NodeService nodeService;

    //@Autowired
    private QuickShareService quickShareService;
    
    //@Autowired
    private SysAdminParams sysAdminParams;

	//@Autowired
	private PlaylistMaker playlistMaker;

	private static final Logger log = LogManager.getLogger();

	private static final int MAX_SEARCH_RESULTS = 16384; // Max Playlist Size
	private static final String SHARE_URL_BASE = "/proxy/alfresco-noauth/api/internal/shared/node/"; // needs the domain, this, the sharedId, then "/content"
	
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
	{
		log.debug("Started making a playlist");
		
		String jsonBody;
		try
		{
			jsonBody = req.getContent().getContent();
		} catch (IOException e)
		{
			status.setCode(Status.STATUS_BAD_REQUEST);
			status.setMessage("JsonBody could not be retreived: " + e);
			return null;
		}
		ObjectMapper objectMapper = new ObjectMapper();
		
		// Retrieve the mandatory query parameter
		Map<String, Object> requestBody;
		try
		{
			requestBody = objectMapper.readValue(jsonBody, Map.class);
		} catch (JsonMappingException e)
		{
			status.setCode(Status.STATUS_BAD_REQUEST);
			status.setMessage("JsonBody Mapping error: " + e);
			return null;
		} catch (JsonProcessingException e)
		{
			status.setCode(Status.STATUS_BAD_REQUEST);
			status.setMessage("Processing error: " + e);
			return null;
		}
		String query = (String) requestBody.get("query");
				
		if (query == null || query.isEmpty())
		{
			status.setCode(Status.STATUS_BAD_REQUEST);
			status.setMessage("Query parameter is required.");
			return null;
		}
		
		List<Music> musics = new ArrayList<>();

		// Fetch the music files based on the query
		for (NodeRef node : fetchMusic(query))
		{
			try
			{
				//link
				String link = getLink(node);
				if(link != null && !link.equals(""))
				{
					Music music = new Music(link);
					
					//title
					String title = (String) nodeService.getProperty(node, QName.createQName("{http://www.amexio.fr/model/music/1.0}songTitle"));
					if(title != null && !title.trim().equals(""))
					{
						music.setTitle(title);
						log.debug("Music Tite: " + title);
					}
					
					musics.add(music);
					log.debug("Music object created");
				} else
				{
					log.warn("Music object could not be created: Link is null or empty for NodeRef " + node.getId());
				}
			}catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		String playlistName = (String) requestBody.get("name"); // optional playlist file name
		
		String targetFolderID = (String) requestBody.get("target"); //optional IF of the folder node to generate the playlist file in
		NodeRef targetFolder = null;
		if (targetFolderID != null && !targetFolderID.trim().isEmpty())
		{
			targetFolderID = targetFolderID.trim();

	        // Getting NodeRef if it exists
	        targetFolder = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, targetFolderID);
	        if (!nodeService.exists(targetFolder))
	            targetFolder = null;
	    }

		// Generates the playlist in the repo
		NodeRef playlistFile = playlistMaker.createPlaylist(musics, targetFolder, playlistName);
		log.debug("playlist generated at node " + playlistFile.getId());

		return new HashMap<>();
	}

	/**
	 * Fetches music nodes from the repository based on the given query.
	 * 
	 * @param query The FTS query to filter the music files.
	 * @return A list of NodeRefs representing the music files.
	 */
	private List<NodeRef> fetchMusic(String query)
	{
		SearchParameters sp = new SearchParameters();
		sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
		sp.setQuery(query);
		sp.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
		sp.setMaxItems(MAX_SEARCH_RESULTS);

		ResultSet resultSet = searchService.query(sp);
		List<NodeRef> nodes = resultSet.getNodeRefs();
		resultSet.close();

		return nodes;
	}

	/**
	 * Retrieves the shared link for a given node. If the node does not have a sharedId, it'll generate one using the QuickShare action.
	 * 
	 * @param node The NodeRef for the music file.
	 * @return The shared link for the node.
	 */
	public String getLink(NodeRef node) throws IOException
	{		
		QName sharedAspect = QName.createQName("{http://www.alfresco.org/model/qshare/1.0}shared");
		
		// Check if the shared aspect is already applied to the node
		if (!nodeService.hasAspect(node, sharedAspect))
		{
			log.debug("Sharing node " + node.getId() + " via QuickShare");
			quickShareService.shareContent(node);
		}

		// Fetch the sharedId property, which is used to generate the shared URL
		String sharedId = (String) nodeService.getProperty(node, QName.createQName("{http://www.alfresco.org/model/qshare/1.0}sharedId"));

		if (sharedId == null || sharedId.trim().equals(""))
		{
			log.error("Failed to retrieve sharedId for node: " + node.getId());
			return null;
		}
		
		String url = sysAdminParams.getShareProtocol() + "://" + sysAdminParams.getShareHost() + ':' + sysAdminParams.getSharePort()
		+ '/' + sysAdminParams.getShareContext() + SHARE_URL_BASE + sharedId + "/content";
		
		log.debug("Shared File URL: " + url);
		
		// Construct the shared URL using the sharedId
		return url;
	}
	
	// Setters & Getters for dependencies and injection
	
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}
	public SearchService getSearchService()
	{
		return searchService;
	}

	public void setContentService(ContentService contentService)
	{
		this.contentService = contentService;
	}

	public ContentService getContentService()
	{
		return contentService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public NodeService getNodeService()
	{
		return nodeService;
	}

	public void setPlaylistMaker(PlaylistMaker playlistMaker)
	{
		this.playlistMaker = playlistMaker;
	}

	public PlaylistMaker getPlaylistMaker()
	{
		return playlistMaker;
	}

    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareService = quickShareService;
    }

    public QuickShareService getQuickShareService()
    {
        return quickShareService;
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public SysAdminParams getSysAdminParams()
    {
        return sysAdminParams;
    }
}
