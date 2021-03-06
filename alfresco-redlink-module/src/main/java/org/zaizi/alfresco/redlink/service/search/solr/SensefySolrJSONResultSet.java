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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSetRow;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSetRowIterator;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sensefy Solr JSON Result Set
 * 
 * @author aayala
 * 
 */
@SuppressWarnings("unused")
public class SensefySolrJSONResultSet implements ResultSet
{
    private NodeService nodeService;

    private ArrayList<Pair<Long, Float>> page;

    private ArrayList<NodeRef> refs;

    // private ResultSetMetaData rsmd;

    private Long status;

    private Long queryTime;

    private Long numberFound;

    private Long start;

    // private Float maxScore;

    private SimpleResultSetMetaData resultSetMetaData;

    private HashMap<String, List<Pair<String, Integer>>> fieldFacets = new HashMap<String, List<Pair<String, Integer>>>(
            1);

    private NodeDAO nodeDao;

    /**
     * Detached result set based on that provided
     * 
     * @param resultSet
     */
    @SuppressWarnings("rawtypes")
    public SensefySolrJSONResultSet(JSONObject json, SearchParameters searchParameters, NodeService nodeService,
            NodeDAO nodeDao, LimitBy limitBy, int maxResults)
    {
        // Note all properties are returned as multi-valued from the WildcardField "*" definition in the SOLR schema.xml
        this.nodeService = nodeService;
        this.nodeDao = nodeDao;
        try
        {
            JSONObject responseHeader = json.getJSONObject("header");
            status = responseHeader.getLong("status");
            queryTime = responseHeader.getLong("qtime");

            JSONObject response = json.getJSONObject("response");
            numberFound = response.getLong("numFound");
            start = response.getLong("start");
            // maxScore = Float.valueOf(response.getString("maxScore"));

            JSONArray docs = response.getJSONArray("docs");

            int numDocs = docs.length();

            ArrayList<Long> rawDbids = new ArrayList<Long>(numDocs);
            ArrayList<Float> rawScores = new ArrayList<Float>(numDocs);
            for (int i = 0; i < numDocs; i++)
            {
                JSONObject doc = docs.getJSONObject(i);
                Long dbid = doc.getLong("DBID");
                Float score = Float.valueOf(doc.getString("score"));

                rawDbids.add(dbid);
                rawScores.add(score);
            }

            // bulk load

            nodeDao.cacheNodesById(rawDbids);

            // filter out rubbish

            page = new ArrayList<Pair<Long, Float>>(numDocs);
            refs = new ArrayList<NodeRef>(numDocs);
            for (int i = 0; i < numDocs; i++)
            {
                Long dbid = rawDbids.get(i);
                NodeRef nodeRef = nodeService.getNodeRef(dbid);

                if (nodeRef != null)
                {
                    page.add(new Pair<Long, Float>(dbid, rawScores.get(i)));
                    refs.add(nodeRef);
                }
            }

            if (json.has("facets"))
            {
                JSONObject facets = json.getJSONObject("facets");
                Iterator it = facets.keys();
                while (it.hasNext())
                {
                    String facetName = (String) it.next();
                    JSONObject facetJSON = facets.getJSONObject(facetName);
                    ArrayList<Pair<String, Integer>> facetValues = new ArrayList<Pair<String, Integer>>(
                            facetJSON.length());
                    Iterator it2 = facetJSON.keys();
                    while (it2.hasNext())
                    {
                        String facetEntryValue = (String) it2.next();
                        int facetEntryCount = facetJSON.getInt(facetEntryValue);
                        Pair<String, Integer> pair = new Pair<String, Integer>(facetEntryValue, facetEntryCount);
                        facetValues.add(pair);
                    }
                    fieldFacets.put(facetName, facetValues);
                }
            }

        }
        catch (JSONException e)
        {

        }
        // We'll say we were unlimited if we got a number less than the limit
        this.resultSetMetaData = new SimpleResultSetMetaData(
                maxResults > 0 && numberFound < maxResults ? LimitBy.UNLIMITED : limitBy,
                PermissionEvaluationMode.EAGER, searchParameters);
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#close()
     */
    @Override
    public void close()
    {
        // NO OP
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getBulkFetch()
     */
    @Override
    public boolean getBulkFetch()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getBulkFetchSize()
     */
    @Override
    public int getBulkFetchSize()
    {
        return Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getChildAssocRef(int)
     */
    @Override
    public ChildAssociationRef getChildAssocRef(int n)
    {
        ChildAssociationRef primaryParentAssoc = nodeService.getPrimaryParent(getNodeRef(n));
        if (primaryParentAssoc != null)
        {
            return primaryParentAssoc;
        }
        else
        {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getChildAssocRefs()
     */
    @Override
    public List<ChildAssociationRef> getChildAssocRefs()
    {
        ArrayList<ChildAssociationRef> refs = new ArrayList<ChildAssociationRef>(page.size());
        for (int i = 0; i < page.size(); i++)
        {
            refs.add(getChildAssocRef(i));
        }
        return refs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRef(int)
     */
    @Override
    public NodeRef getNodeRef(int n)
    {
        return refs.get(n);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRefs()
     */
    @Override
    public List<NodeRef> getNodeRefs()
    {
        return Collections.unmodifiableList(refs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getResultSetMetaData()
     */
    @Override
    public ResultSetMetaData getResultSetMetaData()
    {
        return resultSetMetaData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getRow(int)
     */
    @Override
    public ResultSetRow getRow(int i)
    {
        return new SolrJSONResultSetRow(this, i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getScore(int)
     */
    @Override
    public float getScore(int n)
    {
        return page.get(n).getSecond();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getStart()
     */
    @Override
    public int getStart()
    {
        return start.intValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#hasMore()
     */
    @Override
    public boolean hasMore()
    {
        return numberFound.longValue() > (start.longValue() + page.size() + 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#length()
     */
    @Override
    public int length()
    {
        return page.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#setBulkFetch(boolean)
     */
    @Override
    public boolean setBulkFetch(boolean bulkFetch)
    {
        return bulkFetch;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.search.ResultSetSPI#setBulkFetchSize(int)
     */
    @Override
    public int setBulkFetchSize(int bulkFetchSize)
    {
        return bulkFetchSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<ResultSetRow> iterator()
    {
        return new SolrJSONResultSetRowIterator(this);
    }

    /**
     * @return the queryTime
     */
    public Long getQueryTime()
    {
        return queryTime;
    }

    /**
     * @return the numberFound
     */
    public long getNumberFound()
    {
        return numberFound.longValue();
    }

    @Override
    public List<Pair<String, Integer>> getFieldFacet(String field)
    {
        List<Pair<String, Integer>> answer = fieldFacets.get(field);
        if (answer != null)
        {
            return answer;
        }
        else
        {
            return Collections.<Pair<String, Integer>> emptyList();
        }
    }
}
