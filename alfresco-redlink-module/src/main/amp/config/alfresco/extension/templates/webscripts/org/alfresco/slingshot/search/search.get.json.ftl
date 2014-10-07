<#--

    This file is part of Alfresco RedLink Module.

    Alfresco RedLink Module is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Alfresco RedLink Module is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with Alfresco RedLink Module.  If not, see <http://www.gnu.org/licenses/>.

-->
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"items":
	[
		<#list data.items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"type": "${item.type}",
			"name": "${item.name!''}",
			"displayName": "${item.displayName!''}",
			<#if item.title??>
			"title": "${item.title}",
			</#if>
			"description": "${item.description!''}",
			"modifiedOn": "${xmldate(item.modifiedOn)}",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"size": ${item.size?c},
			<#if item.site??>
			"site":
			{
				"shortName": "${item.site.shortName}",
				"title": "${item.site.title}"
			},
			"container": "${item.container}",
			</#if>
			<#if item.path??>
			"path": "${item.path}",
			</#if>
			"tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>]
		}<#if item_has_next>,</#if>
		</#list>
	]
	<#-- SENSEFY -->
	<#if data.facets??>
	,
	"facets" :
	{
		<#list data.facets as facet>
   			"${facet.name}" : {
   			<#if facet.counts??>
   				<#list facet.counts?keys as v_key>
   					"${v_key}" : "${facet.counts[v_key]}"<#if v_key_has_next>,</#if>
   				</#list>
   		    </#if>
   			}<#if facet_has_next>,</#if>
		</#list>
	} 
	</#if>
	
	<#-- * -->
}
</#escape>