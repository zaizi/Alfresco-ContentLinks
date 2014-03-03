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
package org.zaizi.alfresco.redlink.policies;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies.OnContentPropertyUpdatePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zaizi.alfresco.redlink.domain.EnhancementDefinition;
import org.zaizi.alfresco.redlink.domain.EnhancementType;
import org.zaizi.alfresco.redlink.model.SensefyModel;
import org.zaizi.alfresco.redlink.service.EnhancerService;

/**
 * Enhance policy
 * 
 * @author aayala
 * @author Antonio David Perez Morales <aperez@zaizi.com>
 * 
 */
public class EnhancePolicy implements OnAddAspectPolicy, OnUpdatePropertiesPolicy, OnContentPropertyUpdatePolicy
{
    private Log logger = LogFactory.getLog(EnhancePolicy.class);

    private NodeService nodeService;
    private PolicyComponent policyComponent;

    private EnhancerService enhancerService;

    /**
     * Class initialization
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME,
                SensefyModel.ASPECT_ENHANCED, new JavaBehaviour(this, "onContentPropertyUpdate",
                        NotificationFrequency.TRANSACTION_COMMIT));

        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                SensefyModel.ASPECT_ENHANCED, new JavaBehaviour(this, "onUpdateProperties",
                        NotificationFrequency.TRANSACTION_COMMIT));

        /*this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME,
                SensefyModel.ASPECT_ENHANCED, new JavaBehaviour(this, "onAddAspect",
                        NotificationFrequency.TRANSACTION_COMMIT));*/

        if (logger.isDebugEnabled())
        {
            logger.debug("Initialized policy for enhanced aspect");
        }
    }

    /**
     * Inject Alfresco's service registry
     * 
     * @param services Alfresco's service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.nodeService = services.getNodeService();
    }

    /**
     * Inject Alfresco's policy component
     * 
     * @param policyComponent Alfresco's policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Inject Enhancer Service
     * 
     * @param enhancerService
     */
    public void setEnhancerService(EnhancerService enhancerService)
    {
        this.enhancerService = enhancerService;
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (nodeService.exists(nodeRef) && aspectTypeQName.equals(SensefyModel.ASPECT_ENHANCED)
                && nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT))
        {
            if (!(Boolean) nodeService.getProperty(nodeRef, SensefyModel.PROP_ENHANCED_CONTENTHUB))
            {
                EnhancementDefinition def = new EnhancementDefinition();
                def.add(EnhancementType.ENTITIES_EXTRACTION);
                def.add(EnhancementType.LANGUAGE_DETECTION);
                enhancerService.enhance(nodeRef, def);
            }
        }

    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef) && nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT))
        {
            String nameBefore = (String) before.get(ContentModel.PROP_NAME);
            MLText titleBefore = (MLText) before.get(ContentModel.PROP_TITLE);
            MLText descBefore = (MLText) before.get(ContentModel.PROP_DESCRIPTION);
            String nameAfter = (String) after.get(ContentModel.PROP_NAME);
            MLText titleAfter = (MLText) after.get(ContentModel.PROP_TITLE);
            MLText descAfter = (MLText) after.get(ContentModel.PROP_DESCRIPTION);
            if ((nameAfter != null && !nameAfter.equals(nameBefore))
                    || (titleAfter != null && !titleAfter.equals(titleBefore))
                    || (descAfter != null && !descAfter.equals(descBefore)))
            {
                nodeService.setProperty(nodeRef, SensefyModel.PROP_ENHANCED_PROCESSED, false);
            }
        }
    }

    @Override
    public void onContentPropertyUpdate(NodeRef nodeRef,
                                        QName propertyQName,
                                        ContentData beforeValue,
                                        ContentData afterValue) {
        if(nodeService.exists(nodeRef ) && nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
            String beforeContentUrl = beforeValue.getContentUrl();
            String beforeEncoding = beforeValue.getEncoding();
            String beforeInfoUrl = beforeValue.getInfoUrl();
            String beforeMimetype = beforeValue.getMimetype();
            long beforeSize = beforeValue.getSize();

            String afterContentUrl = afterValue.getContentUrl();
            String afterEncoding = afterValue.getEncoding();
            String afterInfoUrl = afterValue.getInfoUrl();
            String afterMimetype= afterValue.getMimetype();
            long afterSize = afterValue.getSize();
            
            if(!beforeContentUrl.equals(afterContentUrl) || !beforeEncoding.equals(afterEncoding) || !beforeInfoUrl.equals(afterInfoUrl) || !beforeMimetype.equals(afterMimetype) || beforeSize != afterSize) {
                nodeService.setProperty(nodeRef, SensefyModel.PROP_ENHANCED_PROCESSED, false);
            }
            
        }
    }

}
