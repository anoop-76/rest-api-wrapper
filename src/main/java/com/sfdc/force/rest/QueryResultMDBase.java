package com.sfdc.force.rest;

import com.sfdc.force.rest.ForceRestAPI.QueryResultAttribute;
import com.sfdc.force.rest.ForceRestAPI.QueryResultMetaData;

/**
 * An implementation of the QueryResultMetaData interface that can be used as the base class for records returned from a
 * query.
 */
public class QueryResultMDBase implements QueryResultMetaData {
	private QueryResultAttribute attributes;
	
	public QueryResultAttribute getAttributes() {
		return attributes;
	}
	public void setAttributes(QueryResultAttribute attr) {
		attributes = attr;
	}
	
}