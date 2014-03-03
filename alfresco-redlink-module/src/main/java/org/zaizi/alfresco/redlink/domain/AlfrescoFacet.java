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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.util.Pair;

/**
 * AlfrescoFacet Info
 * 
 * @author aayala
 * 
 */
public class AlfrescoFacet
{
    private String type;

    private String name;

    private Map<String, Integer> counts = new LinkedHashMap<String, Integer>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, Integer> getFacetFieldsValues()
    {
        return counts;
    }

    public Integer getFieldCount(String name)
    {
        return counts.get(name);
    }

    public Collection<String> getFacetFields()
    {
        return counts.keySet();
    }

    public void addFieldValue(String field, Integer value)
    {
        counts.put(field, value);
    }

    public void addAllFacetValues(Collection<Pair<String, Integer>> values)
    {
        for (Pair<String, Integer> par : values)
        {
            counts.put(par.getFirst(), par.getSecond());
        }

    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map<String, Integer> getCounts()
    {
        return counts;
    }

    public void setCounts(Map<String, Integer> counts)
    {
        this.counts = counts;
    }

}
