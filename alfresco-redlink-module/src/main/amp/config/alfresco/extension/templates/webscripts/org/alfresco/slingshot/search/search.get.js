<import resource="classpath:/alfresco/extension/templates/webscripts/org/alfresco/slingshot/search/search.lib.js">
/*
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
function main()
{
   var params =
   {
      siteId: (args.site !== null) ? args.site : null,
      containerId: (args.container !== null) ? args.container : null,
      repo: (args.repo !== null) ? (args.repo == "true") : false,
      term: (args.term !== null) ? args.term : null,
      tag: (args.tag !== null) ? args.tag : null,
      query: (args.query !== null) ? args.query : null,
      sort: (args.sort !== null) ? args.sort : null,
      maxResults: (args.maxResults !== null) ? parseInt(args.maxResults, 10) : DEFAULT_MAX_RESULTS,
      /* SENSEFY */
      filters: (args.filters !== null && args.filters.length > 0) ? args.filters.split("|") : [],
      facets: (args.facets !== null && args.facets.length > 0) ? args.facets.split("|") : [],
      /*-*/
   };
   
   model.data = getSearchResults(params);
}

main();