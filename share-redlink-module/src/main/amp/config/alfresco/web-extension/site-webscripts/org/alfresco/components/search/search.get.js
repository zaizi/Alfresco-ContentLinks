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
/**
 * Search component GET method
 */

function main()
{
   // fetch the request params required by the search component template
   var siteId = (page.url.templateArgs["site"] != null) ? page.url.templateArgs["site"] : "";
   var siteTitle = null;
   if (siteId.length != 0)
   {
      // Call the repository for the site profile
      var json = remote.call("/api/sites/" + siteId);
      if (json.status == 200)
      {
         // Create javascript objects from the repo response
         var obj = eval('(' + json + ')');
         if (obj)
         {
            siteTitle = (obj.title.length != 0) ? obj.title : obj.shortName;
         }
      }
   }
   
   // get the search sorting fields from the config
   var sortables = config.scoped["Search"]["sorting"].childrenMap["sort"];
   var sortFields = [];
   for (var i = 0, sort, label; i < sortables.size(); i++)
   {
      sort = sortables.get(i);
      
      // resolve label text
      label = sort.attributes["label"];
      if (label == null)
      {
         label = sort.attributes["labelId"];
         if (label != null)
         {
            label = msg.get(label);
         }
      }
      
      // create the model object to represent the sort field definition
      sortFields.push(
      {
         type: sort.value,
         label: label ? label : sort.value
      });
   }
   
   // Prepare the model
   var repoconfig = config.scoped['Search']['search'].getChildValue('repository-search');
   model.siteId = siteId;
   model.siteTitle = (siteTitle != null ? siteTitle : "");
   model.sortFields = sortFields;
   model.searchTerm = (page.url.args["t"] != null) ? page.url.args["t"] : "";
   model.searchTag = (page.url.args["tag"] != null) ? page.url.args["tag"] : "";
   model.searchSort = (page.url.args["s"] != null) ? page.url.args["s"] : "";
   // config override can force repository search on/off
   model.searchRepo = ((page.url.args["r"] == "true") || repoconfig == "always") && repoconfig != "none";
   model.searchAllSites = (page.url.args["a"] == "true" || siteId.length == 0);
   
   // Advanced search forms based json query
   model.searchQuery = (page.url.args["q"] != null) ? page.url.args["q"] : "";
   model.searchEntity = (page.url.args["entity"] != null ? page.url.args["entity"] : "");
   model.searchSparql = (page.url.args["sparql"] != null ? page.url.args["sparql"] : "");
}

main();