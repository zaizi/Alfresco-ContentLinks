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
package org.zaizi.alfresco.redlink.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Maps;

/**
 * Entity Types extractor & configuration bean
 * 
 * @author aayala
 * 
 */
public class EntityTypeExtractor
{
    private static final String COMMA = ",";
    private List<String> entityTypes;
    private Map<String, String> mapEntityTypes;

    private Properties properties;

    public void init()
    {
        mapEntityTypes = Maps.newLinkedHashMap();
        for (String entityType : entityTypes)
        {
            String value = properties.getProperty("sensefy.redlink.extract.entity.types." + entityType + ".uris");
            String[] values = value.split(COMMA);
            for (String v : values)
            {
                mapEntityTypes.put(v, entityType);
            }
        }
    }

    /**
     * Return the type of an entity providing a list of types (only return types wanted)
     * 
     * @param ent
     * @return
     */
    public String getType(Collection<String> types)
    {
        if (types != null && !types.isEmpty())
        {
            for (String type : types)
            {
                if (mapEntityTypes.containsKey(type))
                {
                    return mapEntityTypes.get(type);
                }
            }
        }
        return null;
    }

    /**
     * Get generic Entity types
     * 
     * @return
     */
    public List<String> getAllEntityTypes()
    {
        return entityTypes;
    }

    /**
     * Set default entity types
     * 
     * @param entityTypes
     */
    public void setEntityTypes(String entityTypes)
    {
        String[] types = entityTypes.split(COMMA);
        this.entityTypes = Arrays.asList(types);
    }

    public Properties getProperties()
    {
        return properties;
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

}
