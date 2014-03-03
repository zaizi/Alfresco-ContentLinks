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
package org.zaizi.alfresco.redlink.service.redlink;

import io.redlink.sdk.Credentials;
import io.redlink.sdk.RedLink.Analysis;
import io.redlink.sdk.RedLinkFactory;
import io.redlink.sdk.analysis.AnalysisRequest;
import io.redlink.sdk.analysis.AnalysisRequest.OutputFormat;
import io.redlink.sdk.impl.DefaultCredentials;
import io.redlink.sdk.impl.analysis.model.Enhancements;
import io.redlink.sdk.impl.analysis.model.EntityAnnotation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ext.RuntimeDelegate;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.json.simple.JSONObject;
import org.openrdf.model.vocabulary.RDFS;
import org.zaizi.alfresco.redlink.domain.EnhancementDefinition;
import org.zaizi.alfresco.redlink.domain.EnhancementType;
import org.zaizi.alfresco.redlink.model.SensefyModel;
import org.zaizi.alfresco.redlink.service.EnhancerService;
import org.zaizi.alfresco.redlink.service.SemanticAbstractService;
import org.zaizi.alfresco.redlink.util.EntityTypeExtractor;

/**
 * Implements {@link EnhancerService}
 * 
 * @author aayala
 * @author Antonio David Perez Morales <aperez@zaizi.com>
 */
public class RedLinkEnhancerServiceImpl extends SemanticAbstractService implements EnhancerService {
    private static final String DEFAULT_LANG = "en";

    private static final String DEPICTION = "http://xmlns.com/foaf/0.1/depiction";

    /**
     * Sanitize category names
     * 
     * @param input
     * @return
     */
    private static String sanitize(String input) {
        return input.replaceAll("[^\\p{Alpha}\\p{Digit} ]+", "").trim();
    }

    private final Log logger = LogFactory.getLog(RedLinkEnhancerServiceImpl.class);
    private ContentService contentService;
    private NodeService nodeService;
    private CategoryService categoryService;
    private TransactionService transactionService;
    private BehaviourFilter behaviourFilter;

    private Double confidenceThreshold;

    private EntityTypeExtractor entityTypeExtractor;
    private String redlinkAPIKey;
    private String redlinkAPIVersion;

    private String redlinkAnalysisName;

    private Analysis redlinkEnhancer;

    private ResteasyProviderFactory resteasyProviderFactory;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.zaizi.alfresco.sensefy.service.EnhancerService#enhance(org.alfresco.service.cmr.repository.NodeRef,
     * org.zaizi.alfresco.sensefy.domain.EnhancementDefinition)
     */
    @Override
    public void enhance(final NodeRef nodeRef, final EnhancementDefinition enhancementDefinition) {
        // Storing temporarily the last user who modified the file so that after doing some things with
        // system user, the final modifier user be the same
        final String userModifier = 
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<String>() {

            @Override
            public String doWork() throws Exception {
                return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
            }});

        // Alfresco implementation
        final RuntimeDelegate rd = RuntimeDelegate.getInstance();

        try {
            
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {

                @Override
                public Void doWork() throws Exception {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Enhancing content " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
                                     + " " + enhancementDefinition.toString());
                    }
                    
                    // Redlink api implementation
                    ResteasyProviderFactory.setInstance(resteasyProviderFactory);

                    Enhancements enhancements = getEnhancements(nodeRef);
                    // enhance language
                    if (enhancementDefinition.has(EnhancementType.LANGUAGE_DETECTION)) {
                        enhanceLanguage(nodeRef, enhancements);
                    }

                    // enhance entities
                    if (enhancementDefinition.has(EnhancementType.ENTITIES_EXTRACTION)) {
                        enhanceAnnotations(nodeRef, enhancements);
                    }

                    nodeService.setProperty(nodeRef, SensefyModel.PROP_ENHANCED_PROCESSED, true);
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("Finished the enhancement for "
                                     + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
                    }
                    
                    return null;
                    
                }
            }, userModifier);
           
            


        } catch (ContentIOException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        } finally {
            // Set alfresco implementaion again
            RuntimeDelegate.setInstance(rd);
        }
    }

    public EntityTypeExtractor getEntityTypeExtractor() {
        return entityTypeExtractor;
    }

    public String getRedlinkAnalysisName() {
        return redlinkAnalysisName;
    }

    public String getRedlinkAPIKey() {
        return redlinkAPIKey;
    }

    public String getRedlinkAPIVersion() {
        return redlinkAPIVersion;
    }

    /**
     * Initialization method
     */
    public void init() {
        contentService = getServiceRegistry().getContentService();
        nodeService = getServiceRegistry().getNodeService();
        categoryService = getServiceRegistry().getCategoryService();
        transactionService = getServiceRegistry().getTransactionService();

        Credentials credentials = new DefaultCredentials(redlinkAPIKey, redlinkAPIVersion);
        redlinkEnhancer = RedLinkFactory.getInstance().createAnalysisClient(credentials);

        resteasyProviderFactory = new ResteasyProviderFactory();
    }

    /**
     * Set behaviourFilter
     * 
     * @param behaviourFilter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
        this.behaviourFilter = behaviourFilter;
    }

    public void setConfidenceThreshold(Double confidence) {
        confidenceThreshold = confidence;
    }

    public void setEntityTypeExtractor(EntityTypeExtractor entityTypeExtractor) {
        this.entityTypeExtractor = entityTypeExtractor;
    }

    public void setRedlinkAnalysisName(String redlinkAnalysisName) {
        this.redlinkAnalysisName = redlinkAnalysisName;
    }

    public void setRedlinkAPIKey(String redlinkAPIKey) {
        this.redlinkAPIKey = redlinkAPIKey;
    }

    public void setRedlinkAPIVersion(String redlinkAPIVersion) {
        this.redlinkAPIVersion = redlinkAPIVersion;
    }

    /**
     * Enhance a nodeRef with Entity Annotations as categories
     * 
     * @param nodeRef
     * @throws ContentIOException
     * @throws IOException
     */
    private void enhanceAnnotations(final NodeRef nodeRef, Enhancements enhancements) throws ContentIOException,
                                                                                     IOException,
                                                                                     InterruptedException {
        List<EntityAnnotation> entityAnnotations = new ArrayList<EntityAnnotation>(
                enhancements.getEntityAnnotationsByConfidenceValue(confidenceThreshold));

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + entityAnnotations.size() + " entity annotations for nodeRef: " + nodeRef
                         + " with a conficende over: " + confidenceThreshold);
        }
        final Set<NodeRef> cats = new LinkedHashSet<NodeRef>();

        for (EntityAnnotation entity : entityAnnotations) {
            // sanitize entity label for category name compatibility
            final String entityLabel = sanitize(entity.getEntityLabel());

            Set<String> entityTypes = (Set<String>) entity.getEntityTypes();
            final String type = entityTypeExtractor.getType(entityTypes);
            final NodeRef cat;
            if (type != null) {
                cat = transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<NodeRef>() {
                        @Override
                        public NodeRef execute() throws Throwable {
                            return AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
                                @Override
                                public NodeRef doWork() {
                                    final NodeRef cat = getCategory(type, entityLabel);
                                    return cat;
                                }
                            });
                        }
                    }, false, true);

                if (nodeService.exists(cat)) {
                    cats.add(cat);

                    final Map<QName,Serializable> props = nodeService.getProperties(cat);

                    String comment = entity.getEntityReference().getValue(RDFS.COMMENT.toString(),
                        DEFAULT_LANG);

                    String abstractString = StringUtils.EMPTY;
                    if (comment != null && !comment.isEmpty()) {
                        abstractString = JSONObject.escape(comment);
                    }

                    String thumbnail = StringUtils.EMPTY;

                    thumbnail = entity.getEntityReference().getFirstPropertyValue(DEPICTION);

                    props.put(SensefyModel.PROP_URI, entity.getEntityReference().getUri());
                    props.put(SensefyModel.PROP_LABEL, entity.getEntityLabel());
                    props.put(SensefyModel.PROP_ABSTRACT, abstractString);
                    props.put(SensefyModel.PROP_THUMBNAIL, thumbnail);
                    props.put(SensefyModel.PROP_TYPES, (Serializable) entityTypes);

                    // Update category properties using system user and retrying if concurrent errors
                    transactionService.getRetryingTransactionHelper().doInTransaction(
                        new RetryingTransactionCallback<Void>() {
                            @Override
                            public Void execute() throws Throwable {
                                AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {
                                    @Override
                                    public Void doWork() {
                                        if (nodeService.exists(cat)) {
                                            nodeService.setProperties(cat, props);
                                        }
                                        return null;
                                    }
                                });
                                return null;
                            }
                        }, false, true);
                }
            }
        }

        if (cats != null && !cats.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Setting " + cats.size() + " categories to node: " + nodeRef);
            }

            behaviourFilter.disableBehaviour(nodeRef);
            nodeService.setProperty(nodeRef, ContentModel.PROP_CATEGORIES, (Serializable) cats);
            behaviourFilter.enableBehaviour(nodeRef);
        }
    }

    /**
     * Enhance a nodeRef with content languages
     * 
     * @param nodeRef
     * @throws ContentIOException
     * @throws IOException
     */
    private void enhanceLanguage(NodeRef nodeRef, Enhancements enhancements) throws ContentIOException,
                                                                            IOException {
        if (enhancements != null) {
            if (enhancements.getLanguages().size() > 0) {
                nodeService.setProperty(nodeRef, SensefyModel.PROP_ENHANCED_LANGUAGES,
                    (Serializable) enhancements.getLanguages());
            }
        }
    }

    /**
     * Get Category inside a parent category Name
     * 
     * @param parentName
     * @param categoryName
     * @return
     */
    private NodeRef getCategory(final String parentName, final String categoryName) {
        return AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
            @Override
            public NodeRef doWork() throws Exception {
                NodeRef result = null;

                Collection<ChildAssociationRef> catChilds = categoryService.getRootCategories(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, ContentModel.ASPECT_GEN_CLASSIFIABLE,
                    parentName, true);

                NodeRef parent = null;
                if (catChilds.isEmpty()) {
                    try {
                        parent = categoryService.createRootCategory(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                            ContentModel.ASPECT_GEN_CLASSIFIABLE, parentName);
                    } catch (DuplicateChildNodeNameException e) {
                        Collection<ChildAssociationRef> categoryChilds = categoryService.getRootCategories(
                            StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, ContentModel.ASPECT_GEN_CLASSIFIABLE,
                            parentName, false);
                        parent = categoryChilds.iterator().next().getChildRef();
                    }
                } else {
                    parent = catChilds.iterator().next().getChildRef();
                }

                ChildAssociationRef resultChild = categoryService.getCategory(parent,
                    ContentModel.ASPECT_GEN_CLASSIFIABLE, categoryName);

                if (resultChild == null) {
                    try {
                        result = categoryService.createCategory(parent, categoryName);
                    } catch (DuplicateChildNodeNameException e) {
                        resultChild = categoryService.getCategory(parent,
                            ContentModel.ASPECT_GEN_CLASSIFIABLE, categoryName);
                        result = resultChild.getChildRef();
                    }
                } else {
                    result = resultChild.getChildRef();
                }

                return result;
            }
        });

    }

    /**
     * @param nodeRef
     * @return
     * @throws IOException
     */
    private Enhancements getEnhancements(final NodeRef nodeRef) throws IOException {
        // read content
        ContentReader reader = getTextContentReader(nodeRef);

        String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        String title = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
        String description = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);

        String metadataInfo = name + " " + title + " " + description;
        InputStream metadataStream = null;
        try {
            metadataStream = new ByteArrayInputStream(metadataInfo.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("Error when building inputstream from metadata to be enhanced: " + e.getMessage(), e);
        }

        // Get enhancements
        InputStream inputStream = null;
        if (reader != null) {
            inputStream = new SequenceInputStream(metadataStream, reader.getContentInputStream());
            // reader.getcon
        } else {
            inputStream = metadataStream;
        }

        AnalysisRequest request = AnalysisRequest.builder().setAnalysis(redlinkAnalysisName)
                .setContent(inputStream).setOutputFormat(OutputFormat.TURTLE).build();
        Enhancements enhancements = redlinkEnhancer.enhance(request);
        return enhancements;
    }

    /**
     * Get a content reader from a nodeRef. If content mimetype isn't text/plain, convert it
     * 
     * @param nodeRef
     *            Node reference
     * @return Content reader
     */
    @SuppressWarnings("deprecation")
    private ContentReader getTextContentReader(NodeRef nodeRef) {
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

        if (reader == null || MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(reader.getMimetype())) {
            return reader;
        } else {
            ContentWriter writer = contentService.getWriter(null, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding(reader.getEncoding());

            // try and transform the content
            if (contentService.isTransformable(reader, writer)) {
                contentService.transform(reader, writer);

                ContentReader resultReader = writer.getReader();
                return resultReader;
            }
            return null;
        }
    }

    /*
     * public List<NodeRef> search(String query) { ResultSet rs = null; try { List<NodeRef> nodes = new
     * ArrayList<NodeRef>();
     * 
     * // search parameters SearchParameters sp = new SearchParameters();
     * sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE); sp.setLanguage(SearchService.LANGUAGE_LUCENE);
     * 
     * sp.setQuery(query);
     * 
     * if (logger.isDebugEnabled()) { logger.debug("Search Parameters: " + sp); } rs =
     * searchService.query(sp); nodes = rs.getNodeRefs();
     * 
     * return nodes; } catch (Exception e) { e.printStackTrace(); return null; } finally { if (rs != null) {
     * rs.close(); } }
     * 
     * }
     */

}
