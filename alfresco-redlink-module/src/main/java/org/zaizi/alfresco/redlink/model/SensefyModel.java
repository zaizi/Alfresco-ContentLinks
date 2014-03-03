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
package org.zaizi.alfresco.redlink.model;

import org.alfresco.service.namespace.QName;

/**
 * Knowledge Management Model
 * 
 * @author efoncubierta
 * 
 */
public interface SensefyModel
{
    public static final String NAMESPACE_URI = "http://www.zaizi.org/model/sensefy/1.0";
    public static final String NAMESPACE_PREFIX = "sensefy";

    public static final QName ASPECT_ENHANCED = QName.createQName(NAMESPACE_URI, "enhanced");
    public static final QName PROP_ENHANCED_PROCESSED = QName.createQName(NAMESPACE_URI, "enhanced_processed");
    
    public static final QName ASPECT_FACETED = QName.createQName(NAMESPACE_URI, "faceted");
    public static final QName PROP_ENHANCED_LANGUAGES = QName.createQName(NAMESPACE_URI, "enhanced_languages");
    public static final QName PROP_ENHANCED_CONTENTHUB = QName.createQName(NAMESPACE_URI, "enhanced_contenthub");
    public static final QName PROP_ENTITY_URIS = QName.createQName(NAMESPACE_URI, "entity_uris");

    public static final QName PROP_ORGANIZATIONS = QName.createQName(NAMESPACE_URI, "organizations");
    public static final QName PROP_PEOPLE = QName.createQName(NAMESPACE_URI, "people");
    public static final QName PROP_PLACES = QName.createQName(NAMESPACE_URI, "places");

    public static final QName ASPECT_URIABLE = QName.createQName(NAMESPACE_URI, "uriable");
    public static final QName PROP_URI = QName.createQName(NAMESPACE_URI, "uri");
    public static final QName PROP_LABEL = QName.createQName(NAMESPACE_URI, "label");
    public static final QName PROP_THUMBNAIL = QName.createQName(NAMESPACE_URI, "thumbnail");
    public static final QName PROP_ABSTRACT = QName.createQName(NAMESPACE_URI, "abstract");
    public static final QName PROP_TYPES = QName.createQName(NAMESPACE_URI, "types");
}
