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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.jscript.ScriptNode;

/**
 * Custom Search Result
 * 
 * @author aayala
 * 
 */
public class SearchResult
{
    private Set<ScriptNode> nodes;

    private List<AlfrescoFacet> facets;

    public SearchResult()
    {
        this.nodes = new LinkedHashSet<ScriptNode>();
        this.facets = new ArrayList<AlfrescoFacet>();
    }

    public void addNode(ScriptNode node)
    {
        nodes.add(node);
    }

    public void addAllNodes(Set<ScriptNode> nodeRefs)
    {
        nodes.addAll(nodeRefs);
    }

    public void addFacet(AlfrescoFacet facet)
    {
        facets.add(facet);
    }

    public Set<ScriptNode> getNodes()
    {
        return nodes;
    }

    public List<AlfrescoFacet> getFacets()
    {
        return facets;
    }

    public void setNodes(Set<ScriptNode> nodes)
    {
        this.nodes = nodes;
    }

    public void setFacets(List<AlfrescoFacet> facets)
    {
        this.facets = facets;
    }

}
