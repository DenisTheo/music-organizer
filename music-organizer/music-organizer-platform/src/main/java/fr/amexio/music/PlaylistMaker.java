package fr.amexio.music;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.springframework.beans.factory.annotation.Autowired;


public class PlaylistMaker
{
	@Autowired
    private ContentService contentService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private MimetypeService mimetypeService;
    
    public void createPlaylist(List<Music> musics, NodeRef targetFolder, String playlistName)
    {
    	Map<QName, Serializable> props = Map.of(ContentModel.PROP_NAME, playlistName+".xspf");

        // Create the new file in the specified folder
        NodeRef playlistFile = nodeService.createNode(targetFolder, ContentModel.ASSOC_CONTAINS,
            QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), playlistName),
            ContentModel.TYPE_CONTENT, props).getChildRef();

            // Write the content to the file
        ContentWriter writer = contentService.getWriter(playlistFile, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetypeService.getMimetype("xspf"));
        writer.setEncoding("UTF-8");
        writer.putContent(generateXSPFContent(musics));
	}
    
    public void createPlaylist(NodeRef targetFolder, List<Music> musics)
	{
    	createPlaylist(musics, targetFolder, generateFileName());
	}
    
    /**
     * Generates the content of an XSPF Playlist file using Music type objects
     * 
     * @param musics List of Musics to put in the playlist
     * @return the content of an XSPF Playlist file
     */
    public String generateXSPFContent(List<Music> musics)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">\n");
        sb.append("    <trackList>\n");
        
        if (musics != null && musics.size() > 0)
	        for (Music music : musics)
	        {
	            sb.append("        <track>\n");
	            sb.append("            <location>").append(music.getLink()).append("</location>\n");
	            if (music.hasTitle())
	                sb.append("            <title>").append(music.getTitle()).append("</title>\n");
	            sb.append("        </track>\n");
	        }
        
        sb.append("    </trackList>\n");
        sb.append("</playlist>\n");
        
        return sb.toString();
    }
    
    /**
     * Generates a filename based on the current date and time
     * 
     * @return current datetime with format "yyyy-MM-dd HH-mm-ss"
     */
    public String generateFileName()
    {
    	java.util.Date now = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return sdf.format(now);
    }
}
