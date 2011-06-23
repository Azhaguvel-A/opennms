/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for OnmsNode entity
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component
@PerRequest
@Scope("prototype")
@Path("nodes")
@Transactional
public class NodeRestService extends OnmsRestService {
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private EventProxy m_eventProxy;
    
    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;

    /**
     * <p>getNodes</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNodeList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsNodeList getNodes() {
        OnmsNodeList coll = new OnmsNodeList(m_nodeDao.findMatching(getQueryFilters(m_uriInfo.getQueryParameters())));

        //For getting totalCount
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);
        addFiltersToCriteria(m_uriInfo.getQueryParameters(), criteria, OnmsNode.class);
        criteria.createAlias("snmpInterfaces", "snmpInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("ipInterfaces", "ipInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.setProjection(
                Projections.distinct(
                        Projections.projectionList().add(
                                Projections.alias( Projections.property("id"), "id" )
                        )
                )
        );
        OnmsCriteria rootCriteria = new OnmsCriteria(OnmsNode.class);
        rootCriteria.add(Subqueries.propertyIn("id", criteria.getDetachedCriteria()));
        coll.setTotalCount(m_nodeDao.countMatching(rootCriteria));

        return coll;
    }

    /**
     * <p>getNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{nodeCriteria}")
    public OnmsNode getNode(@PathParam("nodeCriteria") String nodeCriteria) {
        return m_nodeDao.get(nodeCriteria);
    }

    /**
     * <p>addNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addNode(OnmsNode node) {
        log().debug("addNode: Adding node " + node);
        m_nodeDao.save(node);
        try {
            sendEvent(EventConstants.NODE_ADDED_EVENT_UEI, node.getId());
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok(node).build();
    }
    
    /**
     * <p>updateNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{nodeCriteria}")
    public Response updateNode(@PathParam("nodeCriteria") String nodeCriteria, MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "updateNode: Can't find node " + nodeCriteria);
        }
        log().debug("updateNode: updating node " + node);
        BeanWrapper wrapper = new BeanWrapperImpl(node);
        for(String key : params.keySet()) {
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                @SuppressWarnings("unchecked")
				Object value = wrapper.convertIfNecessary(stringValue, wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
            }
        }
        log().debug("updateNode: node " + node + " updated");
        m_nodeDao.saveOrUpdate(node);
        return Response.ok(node).build();
    }
    
    /**
     * <p>deleteNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{nodeCriteria}")
    public Response deleteNode(@PathParam("nodeCriteria") String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throwException(Status.BAD_REQUEST, "deleteNode: Can't find node " + nodeCriteria);
        }
        log().debug("deleteNode: deleting node " + nodeCriteria);
        m_nodeDao.delete(node);
        try {
            sendEvent(EventConstants.NODE_DELETED_EVENT_UEI, node.getId());
        } catch (EventProxyException ex) {
            throwException(Status.BAD_REQUEST, ex.getMessage());
        }
        return Response.ok().build();
    }

    /**
     * <p>getIpInterfaceResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsIpInterfaceResource} object.
     */
    @Path("{nodeCriteria}/ipinterfaces")
    public OnmsIpInterfaceResource getIpInterfaceResource() {
        return m_context.getResource(OnmsIpInterfaceResource.class);
    }

    /**
     * <p>getSnmpInterfaceResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsSnmpInterfaceResource} object.
     */
    @Path("{nodeCriteria}/snmpinterfaces")
    public OnmsSnmpInterfaceResource getSnmpInterfaceResource() {
        return m_context.getResource(OnmsSnmpInterfaceResource.class);
    }

    /**
     * <p>getCategoryResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsCategoryResource} object.
     */
    @Path("{nodeCriteria}/categories")
    public OnmsCategoryResource getCategoryResource() {
        return m_context.getResource(OnmsCategoryResource.class);
    }

    /**
     * <p>getAssetRecordResource</p>
     *
     * @return a {@link org.opennms.web.rest.AssetRecordResource} object.
     */
    @Path("{nodeCriteria}/assetRecord")
    public AssetRecordResource getAssetRecordResource() {
        return m_context.getResource(AssetRecordResource.class);
    }
    
    private OnmsCriteria getQueryFilters(MultivaluedMap<String,String> params) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class);

        setLimitOffset(params, criteria, DEFAULT_LIMIT, false);
        addOrdering(params, criteria, false);
        // Set default ordering
        addOrdering(
            new MultivaluedMapImpl(
                new String[][] { 
                    new String[] { "orderBy", "label" }, 
                    new String[] { "order", "asc" } 
                }
            ), criteria, false
        );
        addFiltersToCriteria(params, criteria, OnmsNode.class);

        criteria.createAlias("snmpInterfaces", "snmpInterface", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("ipInterfaces", "ipInterface", CriteriaSpecification.LEFT_JOIN);

        return getDistinctIdCriteria(OnmsNode.class, criteria);
    }
    
    private void sendEvent(String uei, int nodeId) throws EventProxyException {
        EventBuilder bldr = new EventBuilder(uei, getClass().getName());
        bldr.setNodeid(nodeId);
        m_eventProxy.send(bldr.getEvent());
    }
    
}
