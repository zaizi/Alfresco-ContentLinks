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
{
<#if data?? && data?is_hash>
	<#list data?keys as dataKey>
		"${dataKey}" : [
			<#if data[dataKey]?? && data[dataKey]?is_sequence>
				<@printEntityList entityList=data[dataKey]/>
			</#if>
		]<#if dataKey_has_next>,</#if> 
	</#list>
</#if>
}

<#macro printEntityList entityList><#list entityList as e>
{	
  "uri": "${e.entity!""}",
  "label": "${e.label!""}",
  "thumbnail": "${e.thumbnail!""}",
  "abstract": "${e.abstract!""}",
  "related": ${e.related![]}
}<#if e_has_next>,</#if></#list>
</#macro>
