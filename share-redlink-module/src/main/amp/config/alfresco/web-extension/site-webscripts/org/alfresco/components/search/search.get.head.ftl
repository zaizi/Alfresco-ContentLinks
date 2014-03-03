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
<#include "../component.head.inc">
<!-- Support libraries -->
<@script type="text/javascript" src="${page.url.context}/res/js/jquery-1.9.1.js"></@script>
<#--<@script type="text/javascript" src="${page.url.context}/res/js/underscore-min.js"></@script>-->
<@script type="text/javascript" src="${page.url.context}/res/js/ajaxSolr/core/Core.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/ajaxSolr/core/AbstractManager.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/ajaxSolr/core/AbstractWidget.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/ajaxSolr/managers/Manager.jquery.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/ajaxSolr/core/Parameter.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/ajaxSolr/core/ParameterStore.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/ajaxSolr/widgets/ResultWidget.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/dsearch-theme.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/base64.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/jquery-ui-1.10.1.custom.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/js/jquery.ui.datepicker-es.js"></@script>
<#--<@script type="text/javascript" src="${page.url.context}/res/js/jquery-ui-1.9.0.custom.js"></@script>-->
<#--<@script type="text/javascript" src="${page.url.context}/res/js/jquery-ui-1.9.2.js"></@script>-->

<#--<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/css/redmond/jquery-ui-1.9.0.custom.css" />-->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/css/jquery-ui-1.10.1.custom.css" />

<@script type="text/javascript" src="${page.url.context}/res/components/search/search-custom.js"></@script>
<!-- Search -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/search/search.css" />
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/search/search-custom.css" />