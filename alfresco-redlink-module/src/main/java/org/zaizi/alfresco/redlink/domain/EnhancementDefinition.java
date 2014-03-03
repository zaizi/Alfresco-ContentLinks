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

public class EnhancementDefinition {

    private List<EnhancementType> types;
    
    public EnhancementDefinition() {
        this(new ArrayList<EnhancementType>());
    }
    
    public EnhancementDefinition(List<EnhancementType> types) {
        this.types = types;
    }
    
    public void add(EnhancementType type) {
        if(!types.contains(type)) {
            types.add(type);
        }
    }
    
    public void remove(EnhancementType type) {
        if(types.contains(type)) {
            types.remove(type);
        }
    }
    
    public Boolean has(EnhancementType type) {
        return types.contains(type);
    }
    
    @Override
    public String toString() {
        return types.toString();
    }
}
