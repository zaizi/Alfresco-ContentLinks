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
package org.zaizi.alfresco.redlink.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zaizi.alfresco.redlink.domain.EnhancementDefinition;
import org.zaizi.alfresco.redlink.domain.EnhancementType;
import org.zaizi.alfresco.redlink.model.SensefyModel;
import org.zaizi.alfresco.redlink.service.EnhancerService;

public class EnhanceContentActionExecuter extends ActionExecuterAbstractBase
{

    private Log logger = LogFactory.getLog(EnhanceContentActionExecuter.class);

    // parameters and labels
    public static final String NAME = "enhance-content";
    public static final String PARAM_LANGUAGE_DETECTION = "language-detection";
    public static final String PARAM_ENTITY_EXTRACTION = "entity-extraction";

    // alfresco services
    private NodeService nodeService;

    // enhancement services
    private EnhancerService enhancerService;

    public void init()
    {
        super.init();
        logger.info("Initializing EnhanceContentActionExecuter");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     * org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef nodeRef)
    {
        /*if (nodeService.exists(nodeRef))
        {
            // enhancement properties
            Boolean languageDetection = (Boolean) action.getParameterValue(PARAM_LANGUAGE_DETECTION);
            Boolean entityExtraction = (Boolean) action.getParameterValue(PARAM_ENTITY_EXTRACTION);

            // build the enhancement definition
            EnhancementDefinition enhancementDefinition = new EnhancementDefinition();
            if (languageDetection)
            {
                enhancementDefinition.add(EnhancementType.LANGUAGE_DETECTION);
            }
            if (entityExtraction)
            {
                enhancementDefinition.add(EnhancementType.ENTITIES_EXTRACTION);
            }

            // enhance content
            enhancerService.enhance(nodeRef, enhancementDefinition);
        }*/
        
        if(nodeService.exists(nodeRef)) {
            // enhancement properties
            /*Boolean languageDetection = (Boolean) action.getParameterValue(PARAM_LANGUAGE_DETECTION);
            Boolean entityExtraction = (Boolean) action.getParameterValue(PARAM_ENTITY_EXTRACTION);*/
            
            if(!nodeService.hasAspect(nodeRef, SensefyModel.ASPECT_ENHANCED)) {
                Map<QName,Serializable> aspectProperties = new HashMap<QName,Serializable>();
                aspectProperties.put(SensefyModel.PROP_ENHANCED_PROCESSED, false);
                nodeService.addAspect(nodeRef, SensefyModel.ASPECT_ENHANCED, aspectProperties);
                
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> params)
    {
        params.add(new ParameterDefinitionImpl(PARAM_LANGUAGE_DETECTION, DataTypeDefinition.BOOLEAN, false,
                getParamDisplayLabel(PARAM_LANGUAGE_DETECTION)));
        params.add(new ParameterDefinitionImpl(PARAM_ENTITY_EXTRACTION, DataTypeDefinition.BOOLEAN, false,
                getParamDisplayLabel(PARAM_ENTITY_EXTRACTION)));
    }

    /**
     * Inject the Alfresco service registry
     * 
     * @param serviceRegistry Alfresco service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.nodeService = serviceRegistry.getNodeService();
    }

    /**
     * Inject the enhancer service
     * 
     * @param enhancerService Enhancer service
     */
    public void setEnhancerService(EnhancerService enhancerService)
    {
        this.enhancerService = enhancerService;
    }
}
