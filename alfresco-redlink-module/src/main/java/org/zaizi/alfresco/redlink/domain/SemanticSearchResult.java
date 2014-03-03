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
package org.zaizi.alfresco.redlink.domain;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * 
 * 
 * @author rharo
 * 
 */
public class SemanticSearchResult
{
    /**
     * List of NodeRel Search Results
     */
    private List<NodeRef> itemResults;

    private List<AlfrescoFacet> facetResults;

    public SemanticSearchResult()
    {
        this.itemResults = new ArrayList<NodeRef>();
        this.facetResults = new ArrayList<AlfrescoFacet>();
    }

    public void addItemResult(NodeRef nodeRef)
    {
        itemResults.add(nodeRef);
    }

    public void addAllItemResult(List<NodeRef> nodeRefs)
    {
        itemResults.addAll(nodeRefs);
    }

    public void addFacetResult(AlfrescoFacet facet)
    {
        facetResults.add(facet);
    }

    public List<NodeRef> getItemResults()
    {
        return itemResults;
    }

    public List<AlfrescoFacet> getFacetResults()
    {
        return facetResults;
    }

    public void setItemResults(List<NodeRef> itemResults)
    {
        this.itemResults = itemResults;
    }

    public void setFacetResults(List<AlfrescoFacet> facetResults)
    {
        this.facetResults = facetResults;
    }
}
