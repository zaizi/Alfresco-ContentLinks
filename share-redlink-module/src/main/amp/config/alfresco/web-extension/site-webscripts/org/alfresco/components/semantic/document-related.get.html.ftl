<#--

    This file is part of Share RedLink Module.

    Share RedLink Module is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Share RedLink Module is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with Share RedLink Module.  If not, see <http://www.gnu.org/licenses/>.

-->
<!-- Parameters and libs -->
<#include "../../include/alfresco-macros.lib.ftl" />
<#assign el=args.htmlid/>
<#if json?? && json.results?? && json.results?is_sequence && json.results?size&gt;0>
	<!-- Markup -->
   <div class="document-related document-details-panel">
      <h2 id="${el}-heading" class="thin dark">
         ${msg("heading")}
      </h2>
      <div id="${el}-related-data" class="panel-body" style="display:block;">
			<#list json.results as elem>
				<div><a href="document-details?nodeRef=${elem.nodeRef}">${elem.name} (${elem.score})</a></div>
			</#list>
      </div>
      <script type="text/javascript">//<![CDATA[
         Alfresco.util.createTwister("${el}-heading", "DocumentRelated",{"collapsed":"true"});
      //]]></script>
   </div>      		
<#else>
	<#--no entities-->
</#if>