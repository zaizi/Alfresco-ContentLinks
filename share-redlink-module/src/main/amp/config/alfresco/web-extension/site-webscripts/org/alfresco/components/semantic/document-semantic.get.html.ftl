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
<#-- Parameters and libs -->
<#include "../../include/alfresco-macros.lib.ftl" />
<#assign el=args.htmlid/>

<#macro printEntityList entityList>
	<#list entityList as e><div><a class="entityLink" href="${e.uri}">${e.label}</a><div class="data" title="${e.label}"><img class="thumbnail" src="${e.thumbnail}" /><div class="entityData"><div class="abstract">${e.abstract}</div><div class="related"><#if e.related?size &gt; 0><br/><strong>${msg("related")}:</strong><br/><#list e.related as r><a href="document-details?nodeRef=${r.nodeRef}">${r.name}</a><#if r_has_next><br/></#if></#list></#if></div></div></div></div></#list>
</#macro>
		      
<#if json?? && json?keys?size==0>
	  <#--no entities-->
   <#else>
   <!-- Markup -->
   <div class="document-semantic document-details-panel">
      <h2 id="${el}-heading" class="thin dark">
         ${msg("heading")}
      </h2>
      <div id="${el}-semantic-data" class="panel-body" style="display:block;">
      <#list json?keys as type>
        	<h3 class="dark">${msg(type)}</h3>
	        <@printEntityList entityList=json[type]/>
		    <br/>
      </#list>
	  <#if json?keys?size==0>
	    <h3>${msg("noentities")}</h3>
	  </#if>
      </div>
      <script type="text/javascript">//<![CDATA[
         Alfresco.util.createTwister("${el}-heading", "DocumentSemantic",{"collapsed":"false"});
            $(".entityLink").click(function(e) {
              e.preventDefault();
			  $($(e.target).data("dialog")).dialog('open');
			});
			$(".data").each(function(index,data) {
			$(data).prev().data("dialog",data);
				$(data).dialog({
	                autoOpen: false,
	                minHeight: 0,
	                resizable: false,
	                width: 650,
	                modal: true,
	                buttons: [],
	                open: function(event,ui){$("a").blur();}
            	});
			});
      //]]></script>
   </div>
</#if>