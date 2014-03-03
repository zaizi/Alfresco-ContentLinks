/**
 * This file is part of Alfresco RedLink Module.
 *
 * Alfresco RedLink Module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco RedLink Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Alfresco RedLink Module.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zaizi.alfresco.redlink.webscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript for retrieving category names from a list of nodeRefs
 * 
 * @author aayala
 * 
 */
public class CategoryNames extends DeclarativeWebScript
{
    private static final String NODEREFS_PARAM = "nodeRefs";
    private static final String CHAR_SEPARATOR = ",";

    // Logger
    private static final Log logger = LogFactory.getLog(CategoryNames.class);

    private NodeService nodeService;

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, Object> model = new HashMap<String, Object>(1);

        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieving and checking the parameters...");
        }

        try
        {
            String noderefsString = req.getParameter(NODEREFS_PARAM);
            String[] nodeRefsArray = noderefsString.split(CHAR_SEPARATOR);
            List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
            for (String nodeString : nodeRefsArray)
            {
                if (NodeRef.isNodeRef(nodeString))
                {
                    nodeRefs.add(new NodeRef(nodeString));
                }
                else
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("NodeRef param provided is not valid: " + nodeString);
                    }
                }
            }

            JSONObject json = new JSONObject();

            for (NodeRef node : nodeRefs)
            {
                if (nodeService.exists(node))
                {
                    json.put(node.toString(), (String) nodeService.getProperty(node, ContentModel.PROP_NAME));
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Obtained name: " + json.getString(node.toString()) + " for nodeRef: "
                                + node.toString());
                    }
                }
            }

            model.put("data", json);
        }
        catch (Exception e)
        {
            logger.error("Error when getting category names: " + e.getMessage(), e);
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
        }
        return model;

    }

    /**
     * Set alfresco services
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.nodeService = serviceRegistry.getNodeService();
    }

}
