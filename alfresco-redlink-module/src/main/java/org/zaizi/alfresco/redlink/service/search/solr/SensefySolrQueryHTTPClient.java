/**
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
package org.zaizi.alfresco.redlink.service.search.solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author aayala
 */
public class SensefySolrQueryHTTPClient
{
    static Log s_logger = LogFactory.getLog(SensefySolrQueryHTTPClient.class);

    private NodeService nodeService;

    private PermissionService permissionService;

    private NodeDAO nodeDAO;

    private HttpClient httpClient;

    private RepositoryState repositoryState;

    private int maxTotalConnections = 40;
    private int maxHostConnections = 40;
    private int socketTimeout = 0;

    private String host;
    private int port;
    private String sensefySearchEndpoint;

    private int maximumResultsFromUnlimitedQuery = Integer.MAX_VALUE;

    public SensefySolrQueryHTTPClient()
    {
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "NodeService", nodeService);
        PropertyCheck.mandatory(this, "PermissionService", permissionService);
        PropertyCheck.mandatory(this, "RepositoryState", repositoryState);

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        httpClient = new HttpClient(connectionManager);
        HttpClientParams params = httpClient.getParams();
        params.setBooleanParameter(HttpConnectionParams.TCP_NODELAY, true);
        params.setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, true);
        params.setSoTimeout(socketTimeout);
        HttpConnectionManagerParams connectionManagerParams = httpClient.getHttpConnectionManager().getParams();
        connectionManagerParams.setMaxTotalConnections(maxTotalConnections);
        connectionManagerParams.setDefaultMaxConnectionsPerHost(maxHostConnections);

        httpClient.getHostConfiguration().setHost(host, port);
    }

    /**
     * @param repositoryState the repositoryState to set
     */
    public void setRepositoryState(RepositoryState repositoryState)
    {
        this.repositoryState = repositoryState;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeDao the nodeDao to set
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param maximumResultsFromUnlimitedQuery the maximum number of results to request from an otherwise unlimited
     *            query
     */
    public void setMaximumResultsFromUnlimitedQuery(int maximumResultsFromUnlimitedQuery)
    {
        this.maximumResultsFromUnlimitedQuery = maximumResultsFromUnlimitedQuery;
    }

    public ResultSet executeQuery(SearchParameters searchParameters)// , String language)
    {
        if (repositoryState.isBootstrapping())
        {
            throw new AlfrescoRuntimeException("SOLR queries can not be executed while the repository is bootstrapping");
        }

        try
        {
            if (searchParameters.getStores().size() == 0)
            {
                throw new AlfrescoRuntimeException("No store for query");
            }

            URLCodec encoder = new URLCodec();
            StringBuilder url = new StringBuilder();
            url.append(sensefySearchEndpoint);

            // Send the query
            url.append("?query=");
            url.append(encoder.encode(searchParameters.getQuery(), "UTF-8"));
            // url.append("?wt=").append(encoder.encode("json", "UTF-8"));
            url.append("&fields=").append(encoder.encode("DBID,score", "UTF-8"));

            // Emulate old limiting behaviour and metadata
            final LimitBy limitBy;
            int maxResults = -1;
            if (searchParameters.getMaxItems() >= 0)
            {
                maxResults = searchParameters.getMaxItems();
                limitBy = LimitBy.FINAL_SIZE;
            }
            else if (searchParameters.getLimitBy() == LimitBy.FINAL_SIZE && searchParameters.getLimit() >= 0)
            {
                maxResults = searchParameters.getLimit();
                limitBy = LimitBy.FINAL_SIZE;
            }
            else
            {
                maxResults = searchParameters.getMaxPermissionChecks();
                if (maxResults < 0)
                {
                    maxResults = maximumResultsFromUnlimitedQuery;
                }
                limitBy = LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS;
            }
            url.append("&rows=").append(String.valueOf(maxResults));

            // url.append("&df=").append(encoder.encode(searchParameters.getDefaultFieldName(), "UTF-8"));
            url.append("&start=").append(encoder.encode("" + searchParameters.getSkipCount(), "UTF-8"));

            // Locale locale = I18NUtil.getLocale();
            // if (searchParameters.getLocales().size() > 0)
            // {
            // locale = searchParameters.getLocales().get(0);
            // }
            // url.append("&locale=");
            // url.append(encoder.encode(locale.toString(), "UTF-8"));

            StringBuffer sortBuffer = new StringBuffer();
            for (SortDefinition sortDefinition : searchParameters.getSortDefinitions())
            {
                if (sortBuffer.length() == 0)
                {
                    sortBuffer.append("&sort=");
                }
                else
                {
                    sortBuffer.append(encoder.encode(", ", "UTF-8"));
                }
                sortBuffer.append(encoder.encode(sortDefinition.getField(), "UTF-8")).append(
                        encoder.encode(" ", "UTF-8"));
                if (sortDefinition.isAscending())
                {
                    sortBuffer.append(encoder.encode("asc", "UTF-8"));
                }
                else
                {
                    sortBuffer.append(encoder.encode("desc", "UTF-8"));
                }

            }
            url.append(sortBuffer);

            if (searchParameters.getFieldFacets().size() > 0)
            {
                for (FieldFacet facet : searchParameters.getFieldFacets())
                {
                    url.append("&facet=").append(encoder.encode(facet.getField(), "UTF-8"));
                }
            }
            // end of field factes

            // add current username doing the request for permissions
            url.append("&userName=").append(encoder.encode(AuthenticationUtil.getRunAsUser(), "UTF-8"));

            GetMethod get = new GetMethod(url.toString());

            try
            {
                httpClient.executeMethod(get);

                if (get.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY
                        || get.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)
                {
                    Header locationHeader = get.getResponseHeader("location");
                    if (locationHeader != null)
                    {
                        String redirectLocation = locationHeader.getValue();
                        get.setURI(new URI(redirectLocation, true));
                        httpClient.executeMethod(get);
                    }
                }

                if (get.getStatusCode() != HttpServletResponse.SC_OK)
                {
                    throw new LuceneQueryParserException("Request failed " + get.getStatusCode() + " " + url.toString());
                }

                Reader reader = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
                // TODO - replace with streaming-based solution e.g. SimpleJSON ContentHandler
                JSONObject json = new JSONObject(new JSONTokener(reader));
                SensefySolrJSONResultSet results = new SensefySolrJSONResultSet(json, searchParameters, nodeService,
                        nodeDAO, limitBy, maxResults);
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Sent :" + url);
                    s_logger.debug("Got: " + results.getNumberFound() + " in " + results.getQueryTime() + " ms");
                }

                return results;
            }
            finally
            {
                get.releaseConnection();
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new LuceneQueryParserException("", e);
        }
        catch (HttpException e)
        {
            throw new LuceneQueryParserException("", e);
        }
        catch (IOException e)
        {
            throw new LuceneQueryParserException("", e);
        }
        catch (JSONException e)
        {
            throw new LuceneQueryParserException("", e);
        }
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getSensefySearchEndpoint()
    {
        return sensefySearchEndpoint;
    }

    public void setSensefySearchEndpoint(String sensefySearchEndpoint)
    {
        this.sensefySearchEndpoint = sensefySearchEndpoint;
    }

}
