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
package org.zaizi.alfresco.redlink.service;

import org.alfresco.service.ServiceRegistry;

/**
 * Abstract utility class for semantic services
 * 
 * @author efoncubierta
 *
 */
public abstract class SemanticAbstractService
{
	private ServiceRegistry services;
	
	/**
	 * Inject Alfresco's service registry
	 * 
	 * @param services Alfresco's service registry
	 */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Get Alfresco's service registry
     * 
     * @return Alfresco's service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
    	return this.services;
    }
}
