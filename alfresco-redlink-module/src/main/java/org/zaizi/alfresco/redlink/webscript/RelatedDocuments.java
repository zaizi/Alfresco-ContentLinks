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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.QueryParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Autocomplete Webscript for Concepts Search
 * 
 * @author aayala
 * 
 */
public class RelatedDocuments extends DeclarativeWebScript
{
    // Logger
    private static final Log logger = LogFactory.getLog(RelatedDocuments.class);

    private static final String PARAM_NODEREF = "noderef";
    private static final String PARAM_LIMIT = "limit";
    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;

    private static final int DEFAULT_LIMIT = 3;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, Object> model = new HashMap<String, Object>(1);

        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieving and checking the parameters...");
        }

        JSONObject results = new JSONObject();

        try
        {
            String nodeString = req.getParameter(PARAM_NODEREF);
            int limit = getLimitFromRequest(req);
            if (NodeRef.isNodeRef(nodeString))
            {
                NodeRef nodeRef = new NodeRef(nodeString);

                if (nodeService.exists(nodeRef))
                {
                    List<NodeRef> relatedDocs = new ArrayList<NodeRef>();

                    List<NodeRef> prevCategories = (List<NodeRef>) nodeService.getProperty(nodeRef,
                            ContentModel.PROP_CATEGORIES);

                    if (prevCategories != null && !prevCategories.isEmpty())
                    {
                        Set<NodeRef> categories = new LinkedHashSet<NodeRef>(prevCategories);

                        if (!categories.isEmpty())
                        {
                            String query = "NOT ID:\"" + nodeRef.toString() + "\" AND (";

                            boolean first = true;
                            for (NodeRef cat : categories)
                            {
                                if (!first)
                                {
                                    query += " OR ";
                                }
                                else
                                {
                                    first = false;
                                }
                                query += "@"
                                        + QueryParser.escape(ContentModel.PROP_CATEGORIES.toPrefixString(namespaceService))
                                        + ":\"" + QueryParser.escape(cat.toString()) + "\"";
                            }

                            query += " )";

                            SearchParameters sp = new SearchParameters();
                            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                            sp.setQuery(query);
                            sp.setLimit(limit);
                            sp.setLimitBy(LimitBy.FINAL_SIZE);
                            ResultSet rs = null;
                            try
                            {
                                relatedDocs = searchService.query(sp).getNodeRefs();
                            }
                            finally
                            {
                                if (rs != null)
                                {
                                    rs.close();
                                }
                            }
                        }

                        JSONArray arrayResults = new JSONArray();
                        for (NodeRef node : relatedDocs)
                        {
                            if (nodeService.exists(node))
                            {
                                JSONObject nodejson = new JSONObject();
                                nodejson.put("nodeRef", node.toString());

                                Set<String> categoriesNode = new LinkedHashSet<String>(
                                        (List<String>) nodeService.getProperty(node, ContentModel.PROP_CATEGORIES));
                                categoriesNode.retainAll(categories);
                                if (categoriesNode.size() != 0)
                                {
                                    nodejson.put("score", categoriesNode.size());
                                    nodejson.put("name", (String) nodeService.getProperty(node, ContentModel.PROP_NAME));
                                    arrayResults.put(nodejson);
                                }
                            }
                        }
                        results.put("results", arrayResults);
                    }
                }
                else
                {
                    status.setCode(404);
                    results.put("status", status.getCode());
                    results.put("response", "NodeRef not exists or doesn't have proper aspect: " + nodeRef.toString());
                }

            }
            else
            {
                status.setCode(400);
                results.put("response", "NodeRef not valid: " + nodeString);
            }

        }
        catch (Exception e)
        {
            logger.error("Error when querying for related documents: " + e.getMessage(), e);
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
            try
            {
                results.put("status", status.getCode());
                results.put("response", e.getMessage());
            }
            catch (JSONException e1)
            {
                logger.error("Error building json response", e);
            }
        }
        model.put("results", results);
        return model;

    }

    /**
     * Get limit value from request
     * 
     * @param req
     */
    private int getLimitFromRequest(WebScriptRequest req)
    {
        int limit = DEFAULT_LIMIT;
        String limitString = req.getParameter(PARAM_LIMIT);
        if (limitString != null)
        {
            try
            {
                limit = Integer.parseInt(limitString);
            }
            catch (NumberFormatException e)
            {
                // ignore
            }
            if (limit <= 0)
            {
                limit = DEFAULT_LIMIT;
            }
        }
        return limit;
    }
}
