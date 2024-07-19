package fr.amexio.music;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlaylistMaker extends DeclarativeWebScript
{
	private NodeService nodeService;
	private ContentService contentService;
	private MimetypeService mimetypeService;
	private String conditions;
	
	public void init()
	{
		
	}
		
	public boolean fileCheck(ChildAssociationRef test)
	{
		NodeRef parentRef = test.getParentRef();
		//conditions
		
		if (nodeService.exists(parentRef) && nodeService.hasAspect(parentRef, QName.createQName("http://www.amexio.fr/model/content/1.0", "ax:composer")))
		{
			//TODO
			
			return true;
		}
		
		return false;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService)
	{
		this.contentService = contentService;
	}

	public void setMimetypeService(MimetypeService mimetypeService)
	{
		this.mimetypeService = mimetypeService;
	}

	@Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status) //WebScriptResult res instead of Status status ?
	{
		Map<String, Object> model = new HashMap<>();

		String pathsParam = req.getParameter("paths");
		String[] paths = {};

		if (pathsParam != null)
		{
			paths = pathsParam.split(",");
		}

		if (paths.length == 0)
		{
			status.setCode(400, "No path provided.");
			//res.setStatus(400);
			//res.getWriter().write("No path provided.");
			return null;
		}

		model.put("paths", paths);

		return model;
	}
}
