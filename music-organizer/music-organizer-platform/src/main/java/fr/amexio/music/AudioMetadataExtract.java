package fr.amexio.music;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import java.io.InputStream;
import java.util.List;

public class AudioMetadataExtract extends ActionExecuterAbstractBase
{
	private NodeService nodeService;
    private ContentService contentService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        ContentReader reader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
        
        if (reader != null && reader.exists())
        {
            try (InputStream is = reader.getContentInputStream())
            {
                ContentHandler handler = new BodyContentHandler();
                Metadata metadata = new Metadata();
                AutoDetectParser parser = new AutoDetectParser();
                parser.parse(is, handler, metadata);

            	if(nodeService.hasAspect(actionedUponNodeRef,
					QName.createQName("http://www.amexio.fr/model/content/1.0", "audio")))
            	{
	                nodeService.setProperty(actionedUponNodeRef,
	                        QName.createQName("http://www.amexio.fr/model/content/1.0", "songTitle"),
	                        metadata.get("title"));
	                nodeService.setProperty(actionedUponNodeRef,
	                        QName.createQName("http://www.amexio.fr/model/content/1.0", "artist"),
	                        metadata.get("xmpDM:artist"));
	                nodeService.setProperty(actionedUponNodeRef,
	                        QName.createQName("http://www.amexio.fr/model/content/1.0", "album"),
	                        metadata.get("xmpDM:album"));
	                nodeService.setProperty(actionedUponNodeRef,
	                        QName.createQName("http://www.amexio.fr/model/content/1.0", "genre"),
	                        metadata.get("xmpDM:genre"));
	                nodeService.setProperty(actionedUponNodeRef,
	                        QName.createQName("http://www.amexio.fr/model/content/1.0", "year"),
	                        metadata.get("xmpDM:releaseDate"));
            	}
            } catch (Exception e)
            { // Handle exceptions appropriately
                e.printStackTrace();
            }
        }
    }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList)
	{
		// TODO Auto-generated method stub
		
	}
}
