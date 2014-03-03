<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:alfresco/site-webscripts/org/alfresco/callutils.js">
/*
 * This file is part of Share RedLink Module.
 *
 * Share RedLink Module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Share RedLink Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Share RedLink Module.  If not, see <http://www.gnu.org/licenses/>.
 */
function main()
{
   var nodeRef = AlfrescoUtil.param('nodeRef');
   var url= "/sensefy/entities?noderef="+nodeRef+"&lang=en";
   var json= doGetCall(url);
   model.json=json;
}

main();