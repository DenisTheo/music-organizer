package fr.amexio.music;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class XSPFPlaylist extends DeclarativeWebScript
{
	@Autowired
    private SearchService searchService;
	
	@Autowired
	private ContentService contentService;

    @Autowired
    private NodeService nodeService;
    
    @Autowired //? à vérifier si c'est nécessaire
    private PlaylistMaker playlistMaker;
    
    private static final Log logger = LogFactory.getLog(XSPFPlaylist.class);
    
    private List<Music> musics;

    private static final int MAX_SEARCH_RESULTS = 10000;
    
    private static final String SHARE_URL_BASE = "http://localhost:8180/share/proxy/alfresco-noauth/api/internal/shared/node/";
    // TODO: find a better, responsive implementation of this URL to adapt on any alfresco server

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        // Gets Query's Value from URL
        String query = req.getParameter("query");
        String playlistName = req.getParameter("name"); // optional

        List<Music> musics = new ArrayList<>();
        List<String> musicLinks = new ArrayList<>();
        
        //NodeRef curNode = new NodeRef("workspace://SpacesStore/[add node id here]"); //scrapped (probably)

        // Get all relevant nodes
        for (NodeRef node : fetchMusic(query))
        {
        	String link = getLink(node);
        	Music music = new Music(link);
        	music.setTitle((String) nodeService.getProperty(node, QName.createQName("{http://www.amexio.fr/model/music/1.0}title")));
            
            musics.add(music);
            musicLinks.add(link);
        }

        // Playlist Generation
        Map<String, Object> model = new HashMap<>();
        
        model.put("links", musicLinks);
        
        // TODO: Replace playlist content display with actual playlist generation

        return model;
    }
    
    private Music makeMusic()
    {
    	String title = null;
    	
    	String link = ""; //TODO: Get Shared Link
    	
    	// TODO
    	
    	return new Music(link, title);
    }
    
    /**
     * Returns all nodes corresponding to the query's filters
     * 
     * @param query	A String containing a FTS query to filter what music files to select or not
     * @returnall relevant music files, as a list of noderefs
     */
    private List<NodeRef> fetchMusic(String query)
    {
    	// Config
    	SearchParameters sp = new SearchParameters();
    	sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery(query);
        sp.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        sp.setMaxItems(MAX_SEARCH_RESULTS);

        // Request
        ResultSet resultSet = searchService.query(sp);
        
        // Number of Files found
        System.out.println(resultSet.length() + " Music files found."); // TODO: Replace with logger stuff

        List<NodeRef> nodes = resultSet.getNodeRefs();
        
        // End Request/Connection
        resultSet.close();
        
    	return nodes;
    }
    
    public String getLink(NodeRef node)
    {
        // Adds the aspect if it doesn’t exist
    	QName sharedAspect = QName.createQName("{http://www.alfresco.org/model/qshare/1.0}shared");
        if (!nodeService.hasAspect(node, sharedAspect))
            nodeService.addAspect(node, sharedAspect, null);
        
        // TODO: check default SharedBy value (admin?)
    	
    	// fetch the shared file ID
    	String shareId = (String) nodeService.getProperty(node, QName.createQName("{http://www.alfresco.org/model/qshare/1.0}sharedId"));
    	if (shareId == null)
    	{
            logger.error("Failed to retrieve sharedId for node: " + node.getId());
            return null;
    	}
            
        return (SHARE_URL_BASE + shareId + "/content");
    }
    
    //Setters & Getters
    
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
}
