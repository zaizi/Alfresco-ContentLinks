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

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.QueryParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.zaizi.alfresco.redlink.model.SensefyModel;
import org.zaizi.alfresco.redlink.util.EntityTypeExtractor;

/**
 * DocumentEntities Webscript for Getting Entity information
 * 
 * @author aayala
 * 
 */
public class DocumentEntities extends DeclarativeWebScript
{
    private static final String AT_SIGN = "@";
    private static final String ABSTRACT_PARAM = "abstract";
    private static final String THUMBNAIL_PARAM = "thumbnail";
    private static final String LABEL_PARAM = "label";
    private static final String ENTITY_PARAM = "entity";
    private static final String NODEREF2_PARAM = "nodeRef";
    private static final String NAME_PARAM = "name";
    private static final String RELATED_PARAM = "related";

    // Logger
    private static final Log logger = LogFactory.getLog(DocumentEntities.class);

    private static final String DEFAULT_LANG = "en";

    // Search parameters
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_NODEREF = "noderef";

    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;

    private EntityTypeExtractor entityTypeExtractor;

    @SuppressWarnings("unchecked")
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
            String noderefString = req.getParameter(PARAM_NODEREF);
            NodeRef nodeRef = new NodeRef(noderefString);
            String lang = req.getParameter(PARAM_LANG);
            if (lang == null)
            {
                lang = DEFAULT_LANG;
            }

            if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, SensefyModel.ASPECT_ENHANCED))
            {
                List<NodeRef> categories = (List<NodeRef>) nodeService.getProperty(nodeRef,
                        ContentModel.PROP_CATEGORIES);

                Map<String, List<JSONObject>> mapTypeEntities = new HashMap<String, List<JSONObject>>();

                if (categories != null && !categories.isEmpty())
                {
                    for (NodeRef cat : categories)
                    {
                        if (isEntity(cat))
                        {
                            JSONObject json = new JSONObject();
                            json.put(ENTITY_PARAM, nodeService.getProperty(cat, SensefyModel.PROP_URI));
                            json.put(LABEL_PARAM, nodeService.getProperty(cat, SensefyModel.PROP_LABEL));
                            json.put(
                                    THUMBNAIL_PARAM,
                                    nodeService.getProperty(cat, SensefyModel.PROP_THUMBNAIL) != null ? nodeService.getProperty(
                                            cat, SensefyModel.PROP_THUMBNAIL) : "");
                            json.put(ABSTRACT_PARAM,
                                    nodeService.getProperty(cat, SensefyModel.PROP_ABSTRACT));

                            // Related documents
                            String query = "NOT ID:\"" + QueryParser.escape(nodeRef.toString()) + "\" AND " + AT_SIGN
                                    + QueryParser.escape(ContentModel.PROP_CATEGORIES.toPrefixString(namespaceService))
                                    + ":\"" + QueryParser.escape(cat.toString()) + "\"";

                            SearchParameters sp = new SearchParameters();
                            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
                            sp.setQuery(query);
                            ResultSet rs = null;
                            List<NodeRef> relatedDocs = new ArrayList<NodeRef>();
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
                            JSONArray relatedDocsArray = new JSONArray();
                            for (NodeRef node : relatedDocs)
                            {
                                JSONObject jsonRelated = new JSONObject();
                                jsonRelated.put(NODEREF2_PARAM, node);
                                String name = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
                                jsonRelated.put(NAME_PARAM, name);
                                relatedDocsArray.put(jsonRelated);
                            }

                            json.put(RELATED_PARAM, relatedDocsArray);

                            String typeEntity = entityTypeExtractor.getType(new LinkedHashSet<String>(
                                    (List<String>) nodeService.getProperty(cat, SensefyModel.PROP_TYPES)));

                            if (typeEntity != null)
                            {
                                List<JSONObject> prevValues = mapTypeEntities.get(typeEntity);
                                if (prevValues == null)
                                {
                                    prevValues = new ArrayList<JSONObject>();
                                }
                                prevValues.add(json);
                                mapTypeEntities.put(typeEntity, prevValues);
                            }
                        }
                    }

                    model.put("data", mapTypeEntities);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when trying to query stanbolClient for entities: " + e.getMessage(), e);
            status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
        }
        return model;

    }

    /**
     * Check the existance of the aspect Uriable for a category
     * 
     * @param cat
     * @return
     */
    private boolean isEntity(NodeRef cat)
    {
        return nodeService.hasAspect(cat, SensefyModel.ASPECT_URIABLE);
    }

    /**
     * Set alfresco services
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.namespaceService = serviceRegistry.getNamespaceService();
    }

    /**
     * Set entity type extractor
     * 
     * @param entityTypeExtractor
     */
    public void setEntityTypeExtractor(EntityTypeExtractor entityTypeExtractor)
    {
        this.entityTypeExtractor = entityTypeExtractor;
    }
}
