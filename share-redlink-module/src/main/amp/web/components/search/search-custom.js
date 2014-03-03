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
 * Search component.
 *
 * @namespace Alfresco
 * @class Alfresco.Search
 */
(function () {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML;

    /**
     * Search constructor.
     *
     * @param {String} htmlId The HTML id of the parent element
     * @return {Alfresco.Search} The new Search instance
     * @constructor
     */
    Alfresco.Search = function (htmlId) {
        Alfresco.Search.superclass.constructor.call(this, "Alfresco.Search", htmlId, ["button", "container", "datasource", "datatable", "paginator", "json"]);

        // Decoupled event listeners
        YAHOO.Bubbling.on("onSearch", this.onSearch, this);

        return this;
    };

    YAHOO.extend(Alfresco.Search, Alfresco.component.Base, {
        /**
         * Object container for initialization options
         *
         * @property options
         * @type object
         */
        options: {
            /**
             * Current siteId
             *
             * @property siteId
             * @type string
             */
            siteId: "",

            /**
             * Current site title
             *
             * @property siteTitle
             * @type string
             */
            siteTitle: "",

            /**
             * Maximum number of results displayed.
             *
             * @property maxSearchResults
             * @type int
             * @default 250
             */
            maxSearchResults: 250,

            /**
             * Results page size.
             *
             * @property pageSize
             * @type int
             * @default 50
             */
            pageSize: 50,

            /**
             * Search term to use for the initial search
             * @property initialSearchTerm
             * @type string
             * @default ""
             */
            initialSearchTerm: "",

            /**
             * Search tag to use for the initial search
             * @property initialSearchTag
             * @type string
             * @default ""
             */
            initialSearchTag: "",

            /**
             * States whether all sites should be searched.
             *
             * @property initialSearchAllSites
             * @type boolean
             */
            initialSearchAllSites: true,

            /**
             * States whether repository should be searched.
             * This is in preference to current or all sites.
             *
             * @property initialSearchRepository
             * @type boolean
             */
            initialSearchRepository: false,

            /**
             * Sort property to use for the initial search.
             * Empty default value will use score relevance default.
             * @property initialSort
             * @type string
             * @default ""
             */
            initialSort: "",

            /**
             * Advanced Search query - forms data json format based search.
             * @property searchQuery
             * @type string
             * @default ""
             */
            searchQuery: "",

            /**
             * Number of characters required for a search.
             *
             * @property minSearchTermLength
             * @type int
             * @default 1
             */
            minSearchTermLength: 1,

            /* DISAMBIGUATION */
            searchEntity: "",
            searchFilters: {},
            searchSparql: "",
            entityhubUrl: "http://dev.iks-project.eu:8081/entityhub/site/dbpedia/find"
            /*-*/
        },

        /**
         * Search term used for the last search.
         */
        searchTerm: "",

        /**
         * Search tag used for the last search.
         */
        searchTag: "",

        /**
         * Whether the search was over all sites or just the current one
         */
        searchAllSites: true,

        /**
         * Whether the search is over the entire repository - in preference to site or all sites
         */
        searchRepository: false,

        /**
         * Search sort used for the last search.
         */
        searchSort: "",

        /**
         * Number of search results.
         */
        resultsCount: 0,

        /**
         * Current visible page index - counts from 1
         */
        currentPage: 1,

        /**
         * True if there are more results than the ones listed in the table.
         */
        hasMoreResults: false,

        /* DISAMBIGUATION */
        searchEntity: "",
        searchFilters: {},
        searchSparql: "",
        /*-*/

        /**
         * Fired by YUI when parent element is available for scripting.
         * Component initialisation, including instantiation of YUI widgets and event listener binding.
         *
         * @method onReady
         */
        onReady: function Search_onReady() {
            var me = this;

            // DataSource definition
            var uriSearchResults = Alfresco.constants.PROXY_URI_RELATIVE + "slingshot/search?";
            this.widgets.dataSource = new YAHOO.util.DataSource(uriSearchResults, {
                responseType: YAHOO.util.DataSource.TYPE_JSON,
                connXhrMode: "queueRequests",
                responseSchema: {
                    resultsList: "items"
                }
            });

            /* SENSEFY */
            this.widgets.dataSource.doBeforeParseData = function doBeforeParseData(oRequest, oFullResponse, oCallback) {
                me.processFacets(oFullResponse);
                me.processSparqlData(oFullResponse);
                return oFullResponse;
            };
            /*-*/

            // YUI Paginator definition
            var handlePagination = function Search_handlePagination(state, me) {
                me.currentPage = state.page;
                me.widgets.paginator.setState(state);
            };
            this.widgets.paginator = new YAHOO.widget.Paginator({
                containers: [this.id + "-paginator-top", this.id + "-paginator-bottom"],
                rowsPerPage: this.options.pageSize,
                initialPage: 1,
                template: this.msg("pagination.template"),
                pageReportTemplate: this.msg("pagination.template.page-report"),
                previousPageLinkLabel: this.msg("pagination.previousPageLinkLabel"),
                nextPageLinkLabel: this.msg("pagination.nextPageLinkLabel")
            });
            this.widgets.paginator.subscribe("changeRequest", handlePagination, this);

            // setup of the datatable.
            this._setupDataTable();

            // set initial value and register the "enter" event on the search text field
            var queryInput = Dom.get(this.id + "-search-text");
            queryInput.value = this.options.initialSearchTerm;

            this.widgets.enterListener = new YAHOO.util.KeyListener(queryInput, {
                keys: YAHOO.util.KeyListener.KEY.ENTER
            }, {
                fn: me._searchEnterHandler,
                scope: this,
                correctScope: true
            }, "keydown").enable();

            // trigger the initial search
            YAHOO.Bubbling.fire("onSearch", {
                searchTerm: this.options.initialSearchTerm,
                searchTag: this.options.initialSearchTag,
                searchSort: this.options.initialSort,
                searchAllSites: this.options.initialSearchAllSites,
                searchRepository: this.options.initialSearchRepository,
                /* DISAMBIGUATION */
                searchEntity: this.options.searchEntity,
                searchFilters: this.options.searchFilters,
                searchSparql: this.options.searchSparql
                /*-*/
            });

            // toggle site scope links
            var toggleLink = Dom.get(this.id + "-site-link");
            Event.addListener(toggleLink, "click", this.onSiteSearch, this, true);
            toggleLink = Dom.get(this.id + "-all-sites-link");
            Event.addListener(toggleLink, "click", this.onAllSiteSearch, this, true);
            toggleLink = Dom.get(this.id + "-repo-link");
            Event.addListener(toggleLink, "click", this.onRepositorySearch, this, true);

            // search YUI button
            this.widgets.searchButton = Alfresco.util.createYUIButton(this, "search-button", this.onSearchClick);

            // menu button for sort options
            this.widgets.sortButton = new YAHOO.widget.Button(this.id + "-sort-menubutton", {
                type: "menu",
                menu: this.id + "-sort-menu",
                menualignment: ["tr", "br"],
                lazyloadmenu: false
            });
            // set initially selected sort button label
            var menuItems = this.widgets.sortButton.getMenu().getItems();
            for (var m in menuItems) {
                if (menuItems[m].value === this.options.initialSort) {
                    this.widgets.sortButton.set("label", this.msg("label.sortby", menuItems[m].cfg.getProperty("text")));
                    break;
                }
            }
            // event handler for sort menu
            this.widgets.sortButton.getMenu().subscribe("click", function (p_sType, p_aArgs) {
                var menuItem = p_aArgs[1];
                if (menuItem) {
                    me.refreshSearch({
                        searchSort: menuItem.value
                    });
                }
            });

            // Hook action events
            var fnActionHandler = function Search_fnActionHandler(layer, args) {
                var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
                if (owner !== null) {
                    if (typeof me[owner.className] == "function") {
                        args[1].stop = true;
                        var tagId = owner.id.substring(me.id.length + 1);
                        me[owner.className].call(me, tagId);
                    }
                }
                return true;
            };
            YAHOO.Bubbling.addDefaultAction("search-tag", fnActionHandler);

            // Finally show the component body here to prevent UI artifacts on YUI button decoration
            Dom.setStyle(this.id + "-body", "visibility", "visible");
        },

        _setupDataTable: function Search_setupDataTable() {
            /**
             * DataTable Cell Renderers
             *
             * Each cell has a custom renderer defined as a custom function. See YUI documentation for details.
             * These MUST be inline in order to have access to the Alfresco.Search class (via the "me" variable).
             */
            var me = this;

            /**
             * Thumbnail custom datacell formatter
             *
             * @method renderCellThumbnail
             * @param elCell {object}
             * @param oRecord {object}
             * @param oColumn {object}
             * @param oData {object|string}
             */
            renderCellThumbnail = function Search_renderCellThumbnail(elCell, oRecord, oColumn, oData) {
                oColumn.width = 100;
                oColumn.height = 100;
                Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
                Dom.setStyle(elCell, "height", oColumn.height + "px");
                Dom.addClass(elCell, "thumbnail-cell");

                var url = me._getBrowseUrlForRecord(oRecord);
                var imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/generic-result.png';

                // use the preview image for a document type
                var dataType = oRecord.getData("type");
                switch (dataType) {
                case "document":
                    imageUrl = Alfresco.constants.PROXY_URI_RELATIVE + "api/node/" + oRecord.getData("nodeRef").replace(":/", "");
                    imageUrl += "/content/thumbnails/doclib?c=queue&ph=true";
                    break;

                case "folder":
                    imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/folder.png';
                    break;

                case "blogpost":
                    imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/blog-post.png';
                    break;

                case "forumpost":
                    imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/topic-post.png';
                    break;

                case "calendarevent":
                    imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/calendar-event.png';
                    break;

                case "wikipage":
                    imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/wiki-page.png';
                    break;

                case "link":
                    imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/link.png';
                    break;

                case "datalist":
                    imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/datalist.png';
                    break;

                case "datalistitem":
                    imageUrl = Alfresco.constants.URL_RESCONTEXT + 'components/search/images/datalistitem.png';
                    break;
                }

                // Render the cell
                var name = oRecord.getData("displayName");
                var htmlName = $html(name);
                var html = '<span><a href="' + url + '"><img src="' + imageUrl + '" alt="' + htmlName + '" title="' + htmlName + '" /></a></span>';
                if (dataType === "document") {
                    var viewUrl = Alfresco.constants.PROXY_URI_RELATIVE + "api/node/content/" + oRecord.getData("nodeRef").replace(":/", "") + "/" + oRecord.getData("name");
                    html = '<div class="action-overlay">' +
                        '<a href="' + encodeURI(viewUrl) + '" target="_blank"><img title="' + $html(me.msg("label.viewinbrowser")) +
                        '" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/search/images/view-in-browser-16.png" width="16" height="16"/></a>' +
                        '<a href="' + encodeURI(viewUrl + "?a=true") + '" style="padding-left:4px" target="_blank"><img title="' + $html(me.msg("label.download")) +
                        '" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/search/images/download-16.png" width="16" height="16"/></a>' +
                        '</div>' + html;
                }
                elCell.innerHTML = html;
            };

            /**
             * Description/detail custom cell formatter
             *
             * @method renderCellDescription
             * @param elCell {object}
             * @param oRecord {object}
             * @param oColumn {object}
             * @param oData {object|string}
             */
            renderCellDescription = function Search_renderCellDescription(elCell, oRecord, oColumn, oData) {
                // apply styles
                Dom.setStyle(elCell.parentNode, "line-height", "1.5em");

                // site and repository items render with different information available
                var site = oRecord.getData("site");
                var url = me._getBrowseUrlForRecord(oRecord);

                // displayname and link to details page
                var displayName = oRecord.getData("displayName");
                var desc = '<h3 class="itemname"><a href="' + url + '" class="theme-color-1">' + $html(displayName) + '</a>';
                // add title (if any) to displayname area
                var title = oRecord.getData("title");
                if (title && title !== displayName) {
                    desc += '<span class="title">(' + $html(title) + ')</span>';
                }
                desc += '</h3>';

                // description (if any)
                var txt = oRecord.getData("description");
                if (txt) {
                    desc += '<div class="details meta">' + $html(txt) + '</div>';
                }

                // detailed information, includes site etc. type specific
                desc += '<div class="details">';
                var type = oRecord.getData("type");
                switch (type) {
                case "document":
                case "folder":
                case "blogpost":
                case "forumpost":
                case "calendarevent":
                case "wikipage":
                case "datalist":
                case "datalistitem":
                case "link":
                    desc += me.msg("label." + type);
                    break;

                default:
                    desc += me.msg("label.unknown");
                    break;
                }

                // link to the site and other meta-data details
                if (site) {
                    desc += ' ' + me.msg("message.insite");
                    desc += ' <a href="' + Alfresco.constants.URL_PAGECONTEXT + 'site/' + $html(site.shortName) + '/dashboard">' + $html(site.title) + '</a>';
                }
                if (oRecord.getData("size") !== -1) {
                    desc += ' ' + me.msg("message.ofsize");
                    desc += ' <span class="meta">' + Alfresco.util.formatFileSize(oRecord.getData("size")) + '</span>';
                }
                if (oRecord.getData("modifiedBy")) {
                    desc += ' ' + me.msg("message.modifiedby");
                    desc += ' <a href="' + Alfresco.constants.URL_PAGECONTEXT + 'user/' + encodeURI(oRecord.getData("modifiedByUser")) + '/profile">' + $html(oRecord.getData("modifiedBy")) + '</a>';
                }
                desc += ' ' + me.msg("message.modifiedon") + ' <span class="meta">' + Alfresco.util.formatDate(oRecord.getData("modifiedOn")) + '</span>';
                desc += '</div>';

                // folder path (if any)
                if (type === "document" || type === "folder") {
                    var path = oRecord.getData("path");
                    if (site) {
                        if (path === null || path === undefined) {
                            path = "";
                        }
                        desc += '<div class="details">' + me.msg("message.infolderpath") +
                            ': <a href="' + me._getBrowseUrlForFolderPath(path, site) + '">' + $html('/' + path) + '</a></div>';
                    } else {
                        if (path) {
                            desc += '<div class="details">' + me.msg("message.infolderpath") +
                                ': <a href="' + me._getBrowseUrlForFolderPath(path) + '">' + $html(path) + '</a></div>';
                        }
                    }
                }

                // tags (if any)
                var tags = oRecord.getData("tags");
                if (tags.length !== 0) {
                    var i, j;
                    desc += '<div class="details"><span class="tags">' + me.msg("label.tags") + ': ';
                    for (i = 0, j = tags.length; i < j; i++) {
                        desc += '<span id="' + me.id + '-' + $html(tags[i]) + '" class="searchByTag"><a class="search-tag" href="#">' + $html(tags[i]) + '</a> </span>';
                    }
                    desc += '</span></div>';
                }

                /* SENSEFY */
                // related documents
                var rid = 'related-' + oRecord.getCount();
                desc += '<div class="details" id="' + rid + '">';
                desc += '</div>';

                elCell.innerHTML = desc;
                elCell.rid = rid;
                elCell.url = url;

                Alfresco.util.Ajax.jsonGet({
                    method: "GET",
                    url: Alfresco.constants.PROXY_URI + "sensefy/related?noderef=" + oRecord.getData("nodeRef") + "&limit=5",
                    successCallback: {
                        fn: function (o) {
                            var docs = o.json.results;
                            if (docs !== undefined) {
                                var desc = '<span class="related">' + me.msg("label.related") + ': ';
                                var div = Dom.get(this.rid);
                                for (i = 0, j = docs.length; i < j; i++) {
                                    desc += '<a class="related-doc" href="document-details?nodeRef=' + docs[i].nodeRef + '">' + $html(docs[i].name) + ' (' + docs[i].score + ')</a>';
                                    if (i < j - 1) {
                                        desc += ', ';
                                    }
                                }
                                desc += '</span>';
                                div.innerHTML = desc;
                            }
                        },
                        scope: elCell
                    }
                });
                /* */
            };

            // DataTable column defintions
            var columnDefinitions = [{
                key: "image",
                label: me.msg("message.preview"),
                sortable: false,
                formatter: renderCellThumbnail,
                width: 100
            }, {
                key: "summary",
                label: me.msg("label.description"),
                sortable: false,
                formatter: renderCellDescription
            }];

            // DataTable definition
            this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-results", columnDefinitions, this.widgets.dataSource, {
                renderLoopSize: Alfresco.util.RENDERLOOPSIZE,
                initialLoad: false,
                paginator: this.widgets.paginator,
                MSG_LOADING: ""
            });

            // show initial message
            this._setDefaultDataTableErrors(this.widgets.dataTable);
            if (this.options.initialSearchTerm.length === 0 && this.options.initialSearchTag.length === 0) {
                this.widgets.dataTable.set("MSG_EMPTY", "");
            }

            // Override abstract function within DataTable to set custom error message
            this.widgets.dataTable.doBeforeLoadData = function Search_doBeforeLoadData(sRequest, oResponse, oPayload) {
                if (oResponse.error) {
                    try {
                        var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                        me.widgets.dataTable.set("MSG_ERROR", response.message);
                    } catch (e) {
                        me._setDefaultDataTableErrors(me.widgets.dataTable);
                    }
                } else if (oResponse.results) {
                    // clear the empty error message
                    me.widgets.dataTable.set("MSG_EMPTY", "");

                    // update the results count, update hasMoreResults.
                    me.hasMoreResults = (oResponse.results.length > me.options.maxSearchResults);
                    if (me.hasMoreResults) {
                        oResponse.results = oResponse.results.slice(0, me.options.maxSearchResults);
                        me.resultsCount = me.options.maxSearchResults;
                    } else {
                        me.resultsCount = oResponse.results.length;
                    }

                    if (me.resultsCount > me.options.pageSize) {
                        Dom.removeClass(me.id + "-paginator-top", "hidden");
                        Dom.removeClass(me.id + "-search-bar-bottom", "hidden");
                    }
                }
                // Must return true to have the "Loading..." message replaced by the error message
                return true;
            };

            // Rendering complete event handler
            me.widgets.dataTable.subscribe("renderEvent", function () {
                // Update the paginator
                me.widgets.paginator.setState({
                    page: me.currentPage,
                    totalRecords: me.resultsCount
                });
                me.widgets.paginator.render();
            });
        },

        /**
         * Constructs the completed browse url for a record.
         * @param record {string} the record
         */
        _getBrowseUrlForRecord: function Search__getBrowseUrlForRecord(record) {
            var url = null;

            var name = record.getData("name"),
                type = record.getData("type"),
                site = record.getData("site"),
                path = record.getData("path");

            switch (type) {
            case "document":
                {
                    url = "document-details?nodeRef=" + record.getData("nodeRef");
                    break;
                }

            case "folder":
                {
                    if (path !== null) {
                        if (site) {
                            url = "documentlibrary?path=" + encodeURIComponent(this._buildSpaceNamePath(path.split("/"), name));
                        } else {
                            url = "repository?path=" + encodeURIComponent(this._buildSpaceNamePath(path.split("/").slice(2), name));
                        }
                    }
                    break;
                }

            case "blogpost":
                {
                    url = "blog-postview?postId=" + name;
                    break;
                }

            case "forumpost":
                {
                    url = "discussions-topicview?topicId=" + name;
                    break;
                }

            case "calendarevent":
                {
                    url = record.getData("container") + "?date=" + Alfresco.util.formatDate(record.getData("modifiedOn"), "yyyy-mm-dd");
                    break;
                }

            case "wikipage":
                {
                    url = "wiki-page?title=" + name;
                    break;
                }

            case "link":
                {
                    url = "links-view?linkId=" + name;
                    break;
                }

            case "datalist":
            case "datalistitem":
                {
                    url = "data-lists?list=" + name;
                    break;
                }
            }

            if (url !== null) {
                // browse urls always go to a page. We assume that the url contains the page name and all
                // parameters. Add the absolute path and the optional site param
                if (site) {
                    url = Alfresco.constants.URL_PAGECONTEXT + "site/" + site.shortName + "/" + url;
                } else {
                    url = Alfresco.constants.URL_PAGECONTEXT + url;
                }
            }

            return (url !== null ? url : '#');
        },

        /**
         * Constructs the folder url for a record.
         * @param path {string} folder path
         *        For a site relative item this can be empty (root of doclib) or any path - without a leading slash
         *        For a repository item, this can never be empty - but will contain leading slash and Company Home root
         */
        _getBrowseUrlForFolderPath: function Search__getBrowseUrlForFolderPath(path, site) {
            var url = null;
            if (site) {
                url = Alfresco.constants.URL_PAGECONTEXT + "site/" + site.shortName + "/documentlibrary?path=" + encodeURIComponent('/' + path);
            } else {
                url = Alfresco.constants.URL_PAGECONTEXT + "repository?path=" + encodeURIComponent('/' + path.split('/').slice(2).join('/'));
            }
            return url;
        },

        _buildSpaceNamePath: function Search__buildSpaceNamePath(pathParts, name) {
            return (pathParts.length !== 0 ? ("/" + pathParts.join("/")) : "") + "/" + name;
        },

        /**
         * DEFAULT ACTION EVENT HANDLERS
         * Handlers for standard events fired from YUI widgets, e.g. "click"
         */

        /**
         * Perform a search for a given tag
         * The tag is simply handled as search term
         */
        searchByTag: function Search_searchTag(param) {
            this.refreshSearch({
                searchTag: param,
                searchTerm: "",
                searchQuery: "",
                searchFilters: {}
            });
        },

        /**
         * Refresh the search page by full URL refresh
         *
         * @method refreshSearch
         * @param args {object} search args
         */
        refreshSearch: function Search_refreshSearch(args) {
            var searchTerm = this.searchTerm;
            if (args.searchTerm !== undefined) {
                searchTerm = args.searchTerm;
            }
            var searchTag = this.searchTag;
            if (args.searchTag !== undefined) {
                searchTag = args.searchTag;
            }
            var searchAllSites = this.searchAllSites;
            if (args.searchAllSites !== undefined) {
                searchAllSites = args.searchAllSites;
            }
            var searchRepository = this.searchRepository;
            if (args.searchRepository !== undefined) {
                searchRepository = args.searchRepository;
            }
            var searchSort = this.searchSort;
            if (args.searchSort !== undefined) {
                searchSort = args.searchSort;
            }
            var searchQuery = this.options.searchQuery;
            if (args.searchQuery !== undefined) {
                searchQuery = args.searchQuery;
            }
            /* DISAMBIGUATION */
            var searchEntity = this.options.searchEntity;
            if (args.searchEntity !== undefined) {
                searchEntity = args.searchEntity;
            }
            var searchFilters = this.options.searchFilters;
            if (args.searchFilters !== undefined) {
                searchFilters = args.searchFilters;
            }
            var searchSparql = this.options.searchSparql;
            if (args.searchSparql !== undefined) {
                searchSparql = args.searchSparql;
            }
            /*-*/

            // redirect back to the search page - with appropriate site context
            var url = Alfresco.constants.URL_PAGECONTEXT;
            if (this.options.siteId.length !== 0) {
                url += "site/" + this.options.siteId + "/";
            }

            // add search data webscript arguments
            url += "search?t=" + encodeURIComponent(searchTerm);
            if (searchSort.length !== 0) {
                url += "&s=" + searchSort;
            }
            if (searchQuery.length !== 0) {
                // if we have a query (already encoded), then apply it
                // most other options such as tag, terms are trumped
                url += "&q=" + searchQuery;
            } else if (searchTag.length !== 0) {
                url += "&tag=" + encodeURIComponent(searchTag);
            }
            /* DISAMBIGUATION */
            if (searchEntity.length !== 0 && searchTerm.length == 0) {
                url = url.slice(0, url.indexOf('?')) + "?entity=" + encodeURIComponent(searchEntity);
            } else {
                url = url.slice(0, url.indexOf('?')) + "?t=" + encodeURIComponent(searchTerm);
            }

            var filters = [];
            for (var facet in searchFilters) {
                if (searchFilters.hasOwnProperty(facet) && searchFilters[facet].length > 0) {
                    for (var i = 0, j = 1; i < searchFilters[facet].length; i++, j++) {
                        filters.push(facet + ':' + searchFilters[facet][i].replace(":", "\\:"));
                    }
                }
            }
            url += (filters.length > 0) ? "&filters=" + encodeURIComponent(filters.join("|")) : "";
            /*-*/
            url += "&a=" + searchAllSites + "&r=" + searchRepository;
            window.location = url;
        },

        /**
         * BUBBLING LIBRARY EVENT HANDLERS FOR PAGE EVENTS
         * Disconnected event handlers for inter-component event notification
         */

        /**
         * Execute Search event handler
         *
         * @method onSearch
         * @param layer {object} Event fired
         * @param args {array} Event parameters (depends on event type)
         */
        onSearch: function Search_onSearch(layer, args) {
            var obj = args[1];
            if (obj !== null) {
                var searchTerm = this.searchTerm;
                if (obj.searchTerm !== undefined) {
                    searchTerm = obj.searchTerm;
                }
                var searchTag = this.searchTag;
                if (obj.searchTag !== undefined) {
                    searchTag = obj.searchTag;
                }
                var searchAllSites = this.searchAllSites;
                if (obj.searchAllSites !== undefined) {
                    searchAllSites = obj.searchAllSites;
                }
                var searchRepository = this.searchRepository;
                if (obj.searchRepository !== undefined) {
                    searchRepository = obj.searchRepository;
                }
                var searchSort = this.searchSort;
                if (obj.searchSort !== undefined) {
                    searchSort = obj.searchSort;
                }
                /* DISAMBIGUATION */
                var searchEntity = this.searchEntity;
                if (obj.searchEntity !== undefined) {
                    searchEntity = obj.searchEntity;
                }
                var searchFilters = this.searchFilters;
                if (obj.searchFilters !== undefined) {
                    searchFilters = obj.searchFilters;
                }
                var searchSparql = this.searchSparql;
                if (obj.searchSparql !== undefined) {
                    searchSparql = obj.searchSparql;
                }
                /*-*/
                this._performSearch({
                    searchTerm: searchTerm,
                    searchTag: searchTag,
                    searchAllSites: searchAllSites,
                    searchRepository: searchRepository,
                    searchSort: searchSort,
                    /* DISAMBIGUATION */
                    searchEntity: searchEntity,
                    searchFilters: searchFilters,
                    searchSparql: searchSparql
                    /*-*/
                });
            }
        },

        /**
         * Event handler that gets fired when user clicks the Search button.
         *
         * @method onSearchClick
         * @param e {object} DomEvent
         * @param obj {object} Object passed back from addListener method
         */
        onSearchClick: function Search_onSearchClick(e, obj) {
            this.refreshSearch({
                searchTag: "",
                searchTerm: YAHOO.lang.trim(Dom.get(this.id + "-search-text").value),
                searchQuery: "",
                searchFilters: {}
            });
        },

        /**
         * Click event for Current Site search link
         *
         * @method onSiteSearch
         */
        onSiteSearch: function Search_onSiteSearch(e, args) {
            this.refreshSearch({
                searchAllSites: false,
                searchRepository: false
            });
        },

        /**
         * Click event for All Sites search link
         *
         * @method onAllSiteSearch
         */
        onAllSiteSearch: function Search_onAllSiteSearch(e, args) {
            this.refreshSearch({
                searchAllSites: true,
                searchRepository: false
            });
        },

        /**
         * Click event for Repository search link
         *
         * @method onRepositorySearch
         */
        onRepositorySearch: function Search_onRepositorySearch(e, args) {
            this.refreshSearch({
                searchRepository: true
            });
        },

        /* DISAMBIGUATION */
        processFacets: function Search_processFacets(response) {
            this.facets = response.facets;

            // clean old facets
            var facetedDiv = Dom.get(this.id + "-facets");
            facetedDiv.innerHTML = "";

            // iterate over facets
            for (var facet in this.facets) {
                if (this.facets.hasOwnProperty(facet)) {
                    // read facet values
                    var facetValues = [];
                    for (var facetValue in this.facets[facet]) {
                        if (this.facets[facet].hasOwnProperty(facetValue)) {
                            facetValues.push(facetValue);
                        }
                    }

                    // ignore empty facets
                    if (facetValues.length == 0) continue;

                    var facetLabel = facet;
                    if (facet.split("||").length > 1) {
                        facetLabel = facet.split("||")[1];
                    }
                    var h3 = document.createElement("h3");
                    h3.innerHTML = this.msg("label." + facetLabel + ".title");
                    h3.title = this.msg("label." + facetLabel + ".description");

                    var parentDiv = document.createElement("div");
                    parentDiv.appendChild(h3);

                    var listItems = document.createElement("ul");

                    var entry = this.facets[facet];
                    var li = null;
                    var spanFilter = null;

                    //check if all values are noderefs
                    var booleanNodeRefs = true;
                    for (var i in facetValues) {
                        if (facetValue.indexOf("workspace://") == -1) {
                            booleanNodeRefs = false;
                            break;
                        }
                    }

                    var result;

                    if (booleanNodeRefs) {
                        //look for names if values are noderefs
                        Alfresco.util.Ajax.jsonGet({
                            method: "GET",
                            url: Alfresco.constants.PROXY_URI + "sensefy/categorynames?nodeRefs=" + facetValues.join(','),
                            listItems: listItems,
                            facet: facet,
                            facetValues: facetValues,
                            successCallback: {
                                fn: function (obj) {
                                    result = obj.json;
                                    this.printFacets(true, result, obj.config.listItems, obj.config.facet, obj.config.facetValues);
                                },
                                scope: this
                            }
                        });

                    } else {
                        this.printFacets(false, null, listItems, facet, facetValues);
                    }

                    parentDiv.appendChild(listItems);
                    facetedDiv.appendChild(parentDiv);
                }
            }
        },

        printFacets: function Search_printFacets(b, result, listItems, facet, facetValues) {

            // print selected facets
            if (this.searchFilters[facet] !== undefined) {
                for (var i in this.searchFilters[facet]) {

                    var facetValue = this.searchFilters[facet][i];

                    // create facet span
                    spanFilter = document.createElement("span");
                    spanFilter.setAttribute('rel', facet + '__' + facetValue);
                    spanFilter.setAttribute('style', 'color: #F55');
                    Dom.addClass(spanFilter, this.id + "-" + facet + "-" + i);


                    if (b) {
                        spanFilter.innerHTML = result[facetValue];
                    } else {
                        spanFilter.innerHTML = facetValue;
                    }

                    // event fired when clicking an unselected facet
                    YAHOO.util.Event.addListener(spanFilter, "click", this.onUnselectedFacet, this);

                    // create facet li entry
                    li = document.createElement("li");
                    li.appendChild(spanFilter);

                    // add facet li entry to the list
                    listItems.appendChild(li);
                }
            }

            // print unselected facets
            for (var i in facetValues) {
                var facetValue = facetValues[i];

                if (this.searchFilters[facet] !== undefined && this.searchFilters[facet].indexOf(facetValue) > -1) {
                    continue;
                }

                // add facet label
                var facetValue = facetValues[i];

                // create facet span
                spanFilter = document.createElement("span");
                spanFilter.setAttribute('rel', facet + '__' + facetValue);
                Dom.addClass(spanFilter, this.id + "-" + facet + "-" + i);

                if (b) {
                    spanFilter.innerHTML = result[facetValue] + " [" + this.facets[facet][facetValue] + "]";

                } else {
                    spanFilter.innerHTML = facetValue + " [" + this.facets[facet][facetValue] + "]";
                }

                // event fired when clicking an unselected facet
                YAHOO.util.Event.addListener(spanFilter, "click", this.onSelectedFacet, this);

                // create facet li entry
                li = document.createElement("li");
                li.appendChild(spanFilter);

                // add facet li entry to the list
                listItems.appendChild(li);
            }

        },

        processSparqlData: function Search_processSparqlData(response) {
            this.sparqlData = response.sparqlData;

            // clean old facets
            var sparqlDataDiv = Dom.get(this.id + "-sparqlData");
            sparqlDataDiv.innerHTML = "";
            if (this.sparqlData === undefined || this.sparqlData.length == 0) {
                return;
            }
            var fields = Object.keys(this.sparqlData[0]);

            var myColumnDefs = [];

            for (var i = 0; i < fields.length; i++) {
                myColumnDefs.push({
                    key: fields[i],
                    sortable: true,
                    resizeable: true
                });
            }

            var myDataSource = new YAHOO.util.DataSource(this.sparqlData);
            myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
            myDataSource.responseSchema = {
                fields: fields
            };

            var myDataTable = new YAHOO.widget.DataTable(this.id + "-sparqlData",
                myColumnDefs, myDataSource, {
                    caption: "SPARQL Data Results"
                });

        },

        onSelectedFacet: function Search_onSelectedFacet(e, obj) {
            var srcEl = e.srcelement ? e.srcelement : e.target;

            var facetSelection = srcEl.getAttribute('rel');

            var i = facetSelection.indexOf("__");
            var facet = facetSelection.slice(0, i);
            var facetValue = facetSelection.slice(i + 2, facetSelection.length);

            var filters = obj.searchFilters;
            if (filters[facet] == undefined) {
                filters[facet] = [];
            }
            filters[facet].push(facetValue);

            obj._performSearch({
                searchTerm: obj.searchTerm,
                searchTag: obj.searchTag,
                searchAllSites: obj.searchAllSites,
                searchRepository: obj.searchRepository,
                searchSort: obj.searchSort,
                searchEntity: obj.searchEntity,
                searchFilters: filters,
                searchSparql: obj.searchSparql
            });
        },

        onUnselectedFacet: function Search_onUnselectedFacet(e, obj) {
            var srcEl = e.srcelement ? e.srcelement : e.target;

            var facetSelection = srcEl.getAttribute('rel');

            var i = facetSelection.indexOf("__");
            var facet = facetSelection.slice(0, i);
            var facetValue = facetSelection.slice(i + 2, facetSelection.length);

            var i = obj.searchFilters[facet].indexOf(facetValue);
            var filters = [];

            if (i == 0) {
                obj.searchFilters[facet].shift();
            } else if (i > 0) {
                obj.searchFilters[facet].splice(i, 1);
            }

            filters = obj.searchFilters;

            obj._performSearch({
                searchTerm: obj.searchTerm,
                searchTag: obj.searchTag,
                searchAllSites: obj.searchAllSites,
                searchRepository: obj.searchRepository,
                searchSort: obj.searchSort,
                searchEntity: obj.searchEntity,
                searchFilters: filters,
                searchSparql: obj.searchSparql
            });
        },

        onSelectedRangeFacet: function Search_onSelectedFacet(e, obj) {
            var from = $("#" + e.target.id).siblings()[0];
            var to = $("#" + e.target.id).siblings()[2];
            var facet = $(e.target).data("facet");
            var fromValue = from.value;
            var toValue = to.value;

            if ($(e.target).data("type") == "tdate") {
                fromDate = $(from).datepicker('getDate', '+1d');
                toDate = $(to).datepicker('getDate', '+1d');
                toDate.setDate(toDate.getDate() + 1);
                fromValue = fromDate.toISOString();
                toValue = toDate.toISOString();
            }

            var facetValue = "[" + fromValue + " TO " + toValue + "]";

            var filters = obj.searchFilters;
            if (filters[facet] == undefined) {
                filters[facet] = [];
            }
            filters[facet].push(facetValue);

            obj._performSearch({
                searchTerm: obj.searchTerm,
                searchTag: obj.searchTag,
                searchAllSites: obj.searchAllSites,
                searchRepository: obj.searchRepository,
                searchSort: obj.searchSort,
                searchEntity: obj.searchEntity,
                searchFilters: filters,
                searchSparql: obj.searchSparql
            });
        },

        onDisambiguatedSearch: function ADVSearch_onDisambiguatedSearch(entity) {
            this._performSearch({
                searchTerm: YAHOO.lang.trim(Dom.get(this.id + "-search-text").value),
                searchTag: this.searchTag,
                searchAllSites: this.searchAllSites,
                searchRepository: this.searchRepository,
                searchSort: this.searchSort,
                searchEntity: entity.id,
                searchFilters: {},
                searchSparql: this.searchSparql
            });
        },
        /*-*/

        /**
         * Search text box ENTER key event handler
         *
         * @method _searchEnterHandler
         */
        _searchEnterHandler: function Search__searchEnterHandler(e, args) {
            this.refreshSearch({
                searchTag: "",
                searchTerm: YAHOO.lang.trim(Dom.get(this.id + "-search-text").value),
                searchQuery: ""
            });
        },

        /**
         * Updates search results list by calling data webscript with current site and query term
         *
         * @method _performSearch
         * @param args {object} search args
         */
        _performSearch: function Search__performSearch(args) {
            var searchTerm = YAHOO.lang.trim(args.searchTerm),
                searchTag = YAHOO.lang.trim(args.searchTag),
                searchAllSites = args.searchAllSites,
                searchRepository = args.searchRepository,
                searchSort = args.searchSort,
                /* SENSEFY */
                searchEntity = args.searchEntity,
                searchFilters = args.searchFilters,
                searchSparql = args.searchSparql;
            /*-*/

            if (this.options.searchQuery.length === 0 && searchTag.length === 0 && searchTerm.replace(/\*/g, "").length < this.options.minSearchTermLength &&
                /* SENSEFY */
                this.options.searchEntity.length === 0
                /*-*/
            ) {
                Alfresco.util.PopupManager.displayMessage({
                    text: this.msg("message.minimum-length", this.options.minSearchTermLength)
                });
                return;
            }

            // empty results table
            this.widgets.dataTable.deleteRows(0, this.widgets.dataTable.getRecordSet().getLength());

            // update the ui to show that a search is on-going
            this.widgets.dataTable.set("MSG_EMPTY", "");
            this.widgets.dataTable.render();

            // Success handler

            function successHandler(sRequest, oResponse, oPayload) {
                // update current state on success
                this.searchTerm = searchTerm;
                this.searchTag = searchTag;
                this.searchAllSites = searchAllSites;
                this.searchRepository = searchRepository;
                this.searchSort = searchSort;
                /* SENSEFY */
                this.searchEntity = searchEntity;
                this.searchFilters = searchFilters;
                this.searchSparql = searchSparql;
                /*-*/

                this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, sRequest, oResponse, oPayload);

                // update the results info text
                this._updateResultsInfo();

                // set focus to search input textbox
                Dom.get(this.id + "-search-text").focus();
            }

            // Failure handler

            function failureHandler(sRequest, oResponse) {
                switch (oResponse.status) {
                case 401:
                    // Session has likely timed-out, so refresh to display login page
                    window.location.reload();
                    break;
                case 408:
                    // Timeout waiting on Alfresco server - probably due to heavy load
                    Dom.get(this.id + '-search-info').innerHTML = this.msg("message.timeout");
                    break;
                default:
                    // General server error code
                    if (oResponse.responseText) {
                        var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                        Dom.get(this.id + '-search-info').innerHTML = response.message;
                    } else {
                        Dom.get(this.id + '-search-info').innerHTML = oResponse.statusText;
                    }
                    break;
                }
            }

            this.widgets.dataSource.sendRequest(this._buildSearchParams(searchRepository, searchAllSites, searchTerm, searchTag, searchSort, /* SENSEFY */ searchEntity, searchFilters, searchSparql /*-*/ ), {
                success: successHandler,
                failure: failureHandler,
                scope: this
            });
        },

        /**
         * Updates the results info text.
         *
         * @method _updateResultsInfo
         */
        _updateResultsInfo: function Search__updateResultsInfo() {
            // update the search results field
            var text;
            var resultsCount = '<b>' + this.resultsCount + '</b>';
            if (this.hasMoreResults) {
                text = this.msg("search.info.resultinfomore", resultsCount);
            } else {
                text = this.msg("search.info.resultinfo", resultsCount);
            }

            // apply the context
            if (this.searchRepository || this.options.searchQuery.length !== 0) {
                text += ' ' + this.msg("search.info.foundinrepository");
            } else if (this.searchAllSites) {
                text += ' ' + this.msg("search.info.foundinallsite");
            } else {
                text += ' ' + this.msg("search.info.foundinsite", $html(this.options.siteTitle));
            }

            // set the text
            Dom.get(this.id + '-search-info').innerHTML = text;
        },

        /**
         * Build URI parameter string for search JSON data webscript
         *
         * @method _buildSearchParams
         */
        _buildSearchParams: function Search__buildSearchParams(searchRepository, searchAllSites, searchTerm, searchTag, searchSort, /* SENSEFY */ searchEntity, searchFilters, searchSparql /*-*/ ) {
            var site = searchAllSites ? "" : this.options.siteId;
            var params = YAHOO.lang.substitute("site={site}&term={term}&tag={tag}&maxResults={maxResults}&sort={sort}&query={query}&repo={repo}&entity={entity}&sparql={sparql}", {
                site: encodeURIComponent(site),
                repo: searchRepository.toString(),
                term: encodeURIComponent(searchTerm),
                tag: encodeURIComponent(searchTag),
                sort: encodeURIComponent(searchSort),
                query: encodeURIComponent(this.options.searchQuery),
                maxResults: this.options.maxSearchResults + 1, // to calculate whether more results were available
                /* SENSEFY */
                entity: encodeURIComponent(decodeURIComponent(escape(searchEntity))),
                sparql: encodeURIComponent(searchSparql)
                /*-*/
            });

            /* SENSEFY */
            var filters = [];
            for (var facet in searchFilters) {
                if (searchFilters.hasOwnProperty(facet) && searchFilters[facet].length > 0) {
                    for (var i = 0, j = 1; i < searchFilters[facet].length; i++, j++) {
                        var f = facet;
                        if (facet.split("||").length > 1) {
                            f = facet.split("||")[0];
                        }
                        filters.push(f + ':"' + searchFilters[facet][i] + '"');
                    }
                }
            }
            params += (filters.length > 0) ? "&filters=" + encodeURIComponent(filters.join("|")) : "";

            //TODO get from configuration
            params += "&facets=" + encodeURIComponent("cm:categories");
            /*-*/

            return params;
        },

        /**
         * Resets the YUI DataTable errors to our custom messages
         * NOTE: Scope could be YAHOO.widget.DataTable, so can't use "this"
         *
         * @method _setDefaultDataTableErrors
         * @param dataTable {object} Instance of the DataTable
         */
        _setDefaultDataTableErrors: function Search__setDefaultDataTableErrors(dataTable) {
            var msg = Alfresco.util.message;
            dataTable.set("MSG_EMPTY", msg("message.empty", "Alfresco.Search"));
            dataTable.set("MSG_ERROR", msg("message.error", "Alfresco.Search"));
        }
    });
})();