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
<#include "include/alfresco-template.ftl" />
<@templateHeader>
   <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js"></@script>
   <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/document-details/document-details-panel.css" />
   <@templateHtmlEditorAssets />
</@>

<@templateBody>
   <div id="alf-hd">
      <@region id="header" scope="global"/>
      <@region id="title" scope="template"/>
      <@region id="navigation" scope="template"/>
   </div>
   <div id="bd">
      <@region id="actions-common" scope="template"/>
      <@region id="actions" scope="template"/>
      <@region id="node-header" scope="template"/>
      <div class="yui-gc">
         <div class="yui-u first">
            <#if (config.scoped['DocumentDetails']['document-details'].getChildValue('display-web-preview') == "true")>
               <@region id="web-preview" scope="template"/>
            </#if>
            <@region id="comments" scope="template"/>
         </div>
         <div class="yui-u">
            <@region id="document-actions" scope="template"/>
            <@region id="document-tags" scope="template"/>
            <@region id="document-links" scope="template"/>
            <@region id="document-metadata" scope="template"/>
            <@region id="document-semantic" scope="template"/>
            <@region id="document-related" scope="template"/>
            <@region id="document-sync" scope="template"/>
            <@region id="document-permissions" scope="template"/>
            <@region id="document-workflows" scope="template"/>
            <@region id="document-versions" scope="template"/>
            <@region id="document-publishing" scope="template"/>
            <#if imapServerEnabled>
               <@region id="document-attachments" scope="template"/>
            </#if>
         </div>
      </div>

      <@region id="html-upload" scope="template"/>
      <@region id="flash-upload" scope="template"/>
      <@region id="file-upload" scope="template"/>
   </div>
   <@region id="doclib-custom" scope="template"/>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global"/>
   </div>
</@>
