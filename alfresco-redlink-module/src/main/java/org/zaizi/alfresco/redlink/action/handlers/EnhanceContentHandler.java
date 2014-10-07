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
package org.zaizi.alfresco.redlink.action.handlers;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.web.bean.actions.handlers.BaseActionHandler;
import org.alfresco.web.bean.wizard.IWizardBean;
import org.zaizi.alfresco.redlink.action.EnhanceContentActionExecuter;

public class EnhanceContentHandler extends BaseActionHandler
{
    /**
     * 
     */
    private static final long serialVersionUID = -894371706468972787L;
    public static final String PROP_LANGUAGE = "language";
    public static final String PROP_ENTITIES = "entities";

    @Override
    public String getJSPPath()
    {
        return getJSPPath(EnhanceContentActionExecuter.NAME);
    }

    @Override
    public void prepareForSave(Map<String, Serializable> actionProps, Map<String, Serializable> repoParams)
    {
        Boolean language = (Boolean) actionProps.get(PROP_LANGUAGE);
        Boolean entities = (Boolean) actionProps.get(PROP_ENTITIES);

        repoParams.put(EnhanceContentActionExecuter.PARAM_LANGUAGE_DETECTION, language);
        repoParams.put(EnhanceContentActionExecuter.PARAM_ENTITY_EXTRACTION, entities);
    }

    @Override
    public void prepareForEdit(Map<String, Serializable> actionProps, Map<String, Serializable> repoParams)
    {
        actionProps.put(PROP_LANGUAGE, repoParams.get(EnhanceContentActionExecuter.PARAM_LANGUAGE_DETECTION));
        actionProps.put(PROP_ENTITIES, repoParams.get(EnhanceContentActionExecuter.PARAM_ENTITY_EXTRACTION));
    }

    @Override
    public String generateSummary(FacesContext context, IWizardBean wizard, Map<String, Serializable> actionProps)
    {
        Boolean language = (Boolean) actionProps.get(PROP_LANGUAGE);
        Boolean entities = (Boolean) actionProps.get(PROP_ENTITIES);

        // return MessageFormat.format(Application.getMessage(context, "enhance_content"),
        // new Object[] {language?"yes":"no", entities?"yes":"no"});
        return "Language detection: " + (language ? "yes" : "no") + ", Entities extraction: "
                + (entities ? "yes" : "no");
    }
}
