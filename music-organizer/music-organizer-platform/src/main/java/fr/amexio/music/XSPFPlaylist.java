package fr.amexio.music;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class XSPFPlaylist extends DeclarativeWebScript
{
	@Autowired
    private SearchService searchService;

    @Autowired
    private NodeService nodeService;

    private static final int MAX_SEARCH_RESULTS = 10000;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // Gets Query's Value from URL
        String query = req.getParameter("query");
        List<String> paths = new ArrayList<>();

        // Get all relevant nodes
        for (NodeRef nodeRef : fetchMusic(query))
        {
            String displayPath = nodeService.getPath(nodeRef).toString();
            String name = (String) nodeService.getProperty(nodeRef, QName.createQName("{http://www.amexio.fr/model/music/1.0}name")); // TODO: export the namespace to an external file
            String axPath = (String) nodeService.getProperty(nodeRef, QName.createQName("{http://www.amexio.fr/model/music/1.0}path"));

            System.out.println(displayPath + "/" + name + " - " + nodeRef.toString() + " - " + axPath);

            paths.add(axPath);
        }

        // Playlist Generation
        Map<String, Object> model = new HashMap<>();
        model.put("paths", paths);
        
        // TODO: Replace playlist content display with actual playlist generation
        
        //String xspfContent = generateXSPFContent(paths);
        //writeXSPFToFile(xspfContent, "playlist.xspf");
        	// need to check where it'll actually be executed/generated

        return model;
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
    	SearchParameters searchParameters = new SearchParameters();
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.setQuery(query);
        searchParameters.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        searchParameters.setMaxItems(MAX_SEARCH_RESULTS);

        // Request
        ResultSet resultSet = searchService.query(searchParameters);
        
        // Number of Files found
        //System.out.println(resultSet.length() + " Music files found."); // TODO: Replace with logger stuff

        List<NodeRef> nodes = resultSet.getNodeRefs();
        
        // End Request/Connection
        resultSet.close();
        
    	return nodes;
    }
    
    /**
     * Writes the content to a file
     * 
     * @param content The XSPF content to write to the file
     * @param filename The name of the file to write the content to
     */
    @SuppressWarnings("unused") // WIP
	private void writeXSPFToFile(String content, String filename)
    {
        try (FileWriter writer = new FileWriter(filename))
        {
            writer.write(content);
        }
        catch (IOException e)
        {
            System.err.println("Error writing XSPF file: " + e.getMessage());
        }
    }
    
    // Temporary, Should be using the model instead
    private String generateXSPFContent(List<String> paths)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">\n");
        sb.append("    <trackList>\n");

        for (String path : paths)
        {
            sb.append("        <track>\n");
            sb.append("            <location>").append(path).append("</location>\n");
            sb.append("        </track>\n");
        }

        sb.append("    </trackList>\n");
        sb.append("</playlist>\n");

        return sb.toString();
    }
}
