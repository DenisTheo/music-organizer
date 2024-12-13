package fr.amexio.music;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.alfresco.service.cmr.repository.MimetypeService;
//import org.springframework.beans.factory.annotation.Autowired;

public class PlaylistMaker
{
	//@Autowired
    private ContentService contentService;

    //@Autowired
    private NodeService nodeService;

    //@Autowired
    private MimetypeService mimetypeService;
    
    //@Autowired
    private SearchService searchService;
    
	private static final Logger log = LogManager.getLogger();
    
	/**
	 * Generates a playlist file (.xspf format) in a target folder based on a list of Musics to implement
	 * 
	 * @param musics	list of Music files to use in the playlist
	 * @param targetFolder	folder in which to generate the playlist file (optional)
	 * @param playlistName	Name of the playlist file to create (optional)
	 * 
	 * @return	the created file's NodeRef
	 */
    public NodeRef createPlaylist(List<Music> musics, NodeRef targetFolder, String playlistName)
    {
    	// Folder
    	if (targetFolder == null)
    		targetFolder = getPlaylistFolder();
    	
    	// File Name
    	if (playlistName == null || playlistName.trim().equals(""))
    		playlistName = generateFileName();
    	
    	playlistName = playlistName.endsWith(".xspf") ? playlistName : playlistName+".xspf";
    	log.debug("playlist file name: " + playlistName);
    	
    	// Fetch the playlist file if it already exists
    	NodeRef playlistFile = nodeService.getChildByName(targetFolder, ContentModel.ASSOC_CONTAINS, playlistName);
    		
        // Create the new file if it doesn't exist already
    	if (playlistFile == null)
    	{
            Map<QName, Serializable> props = Map.of(ContentModel.PROP_NAME, playlistName);
            playlistFile = nodeService.createNode(targetFolder, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), playlistName),
                    ContentModel.TYPE_CONTENT, props).getChildRef();
        }
    	// Make sure the file is versionable
        makeVersionable(playlistFile);

        // Write the content to the file
        ContentWriter writer = contentService.getWriter(playlistFile, ContentModel.PROP_CONTENT, true);

        writer.setMimetype("text/xspf+xml"); // Manual Value
        writer.setEncoding("UTF-8");
        writer.putContent(generateXSPFContent(musics, playlistName));
        
        return playlistFile;
	}
    
    public NodeRef createPlaylist(List<Music> musics, NodeRef targetFolder)
	{
    	return createPlaylist(musics, targetFolder, generateFileName());
	}
    
    public NodeRef createPlaylist(List<Music> musics, String playlistName)
	{
    	return createPlaylist(musics, getPlaylistFolder(), playlistName);
	}
    
    public NodeRef createPlaylist(List<Music> musics)
	{
    	return createPlaylist(musics, getPlaylistFolder(), generateFileName());
	}
    
    /**
     * Ensures a NodeRef has the "versionable" aspect. Adds it if missing.
     * 
     * @param nodeRef	the Node to make versionable
     */
    private void makeVersionable(NodeRef nodeRef)
    {
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            Map<QName, Serializable> versionProps = new HashMap<>();
            versionProps.put(ContentModel.PROP_AUTO_VERSION, true);
            versionProps.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, versionProps);
            log.debug("made node " + nodeRef.getId() + " versionable");
        }
    }
    
    /**
     * Returns the Music/Playlist Folder in the Workspace. Generates a Music/Playlist folder if it doesn't exist already.
     * 
     * @return the Folder's NodeRef
     */
    protected NodeRef getPlaylistFolder()
    {
    	// Find the Company Home Folder
    	String query = "PATH:\"/app:company_home\"";
    	SearchParameters sp = new SearchParameters();
    	sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
    	sp.setQuery(query);
    	sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

    	ResultSet results = searchService.query(sp);
    	if (results.length() < 1)
    	    throw new IllegalStateException("Company Home not found.");
	    NodeRef companyHome = results.getNodeRef(0);
    	
    	// Search for/Create the Music Folder
    	NodeRef musicFolder = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, "Music");
        if (musicFolder == null)
        {
            Map<QName, Serializable> folderProps = new HashMap<>();
            folderProps.put(ContentModel.PROP_NAME, "Music");
            
            musicFolder = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), "Music"),
                    ContentModel.TYPE_FOLDER, folderProps).getChildRef();
        }
        
        // Search for/Create the Playlist Folder
        NodeRef playlistFolder = nodeService.getChildByName(musicFolder, ContentModel.ASSOC_CONTAINS, "Playlist");
        if (playlistFolder == null)
        {
            Map<QName, Serializable> folderProps = new HashMap<>();
            folderProps.put(ContentModel.PROP_NAME, "Playlist");
            
            playlistFolder = nodeService.createNode(musicFolder, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), "Playlist"),
                    ContentModel.TYPE_FOLDER, folderProps).getChildRef();
        }

        return playlistFolder;
    }
    
    /**
     * Generates a filename based on the current date and time
     * 
     * @return current date and time with format "yyyy-MM-dd HH-mm-ss"
     */
    public String generateFileName()
    {
    	java.util.Date now = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return sdf.format(now);
    }
    
    /**
     * Generates the content of an XSPF Playlist file using Music type objects
     * 
     * @param musics List of Musics to put in the playlist
     * @return the content of an XSPF Playlist file
     */
    public String generateXSPFContent(List<Music> musics, String playlistName)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">\n");
        sb.append("    <title>").append(playlistName).append("</title>\n");
        sb.append("    <trackList>\n");
        
        if (musics != null && musics.size() > 0)
	        for (Music music : musics)
	        {
	            sb.append("        <track>\n");
	            sb.append("            <location>").append(music.getLink()).append("</location>\n");
	            
	            if (music.hasTitle()) // optional
	                sb.append("            <title>").append(music.getTitle()).append("</title>\n");
	            
	            if (music.hasSingers() || music.hasComposers()) // optional
	                sb.append("            <creator>").append(music.getArtists()).append("</creator>\n");

	            if(music.hasAlbumName())
	                sb.append("            <album>").append(music.getAlbumName()).append("</album>\n");
	            
	            if(music.hasTrackNumber())
	                sb.append("            <trackNum>").append(music.getTrackNumber()).append("</trackNum>\n");
	            
	            if(music.hasDiscNumber())
	                sb.append("            <meta rel=\"disk-number\">").append(music.getDiscNumber()).append("</meta>\n");
	            
	            if(music.hasGenres())
	                sb.append("            <meta rel=\"genre\">").append(music.getGenresAsString()).append("</meta>\n");
	            
	            if(music.hasYear())
	                sb.append("            <meta rel=\"year\">").append(music.getYear()).append("</meta>\n");
	            
	            sb.append("        </track>\n");
	        }
        
        sb.append("    </trackList>\n");
        sb.append("</playlist>\n");
        
        return sb.toString();
    }
    
    // -------- GETTERS & SETTERS --------
    
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    public MimetypeService getMimetypeService()
    {
        return mimetypeService;
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

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }
}
