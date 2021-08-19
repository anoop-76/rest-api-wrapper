package com.sfdc.force.rest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import com.google.gson.JsonSyntaxException;

public interface ForceRestAPI {

    
	/**
	 * Create a new object, using the apiName given. The second argument specifies values
	 * for the given object. 
	 * 
	 * @param apiName The name of the object to create.
	 * @param objectValues An object who's properties represent the initial values for the object.
	 * @return A CreateResult object giving the id of the new object. If the object was not created, 
	 * CreateResult.id will be null.
	 * @throws IOException
	 * 
	 */
	public abstract CreateResult create(String apiName, Object objectValues)
			throws IOException;

	/**
	 * 
	 * Get a specific object by ID. 
	 * 
	 * @param <T> The type of the object returned.
	 * @param apiName The name of the object to retrieve.
	 * @param objectId The id of the object to retrieve.
	 * @param t This type should match the T parameter, and is required due to Java's handling of generics. Use
	 * the following template to create this object:
	 * 
	 *   Type t = new TypeToken<T>(){}.getType();
	 * 
	 * @return The object, or null if the object was not found.
	 * @throws IOException
	 */
	public abstract <T> T get(String apiName, String objectId, Type t)
			throws IOException;

    /**
     * Executes a SOQL query.
     * 
     * @param <T> The type of records returned.
     * @param query The text of the query.
     * @param t Should be the same type as T, and is required due to Java's handling of generics. Use the following
     * template to create this object:
     * 
     * Type t = new TypeToken<T>(){}.getType();
     * 
     * @return The result of the query. Never null. If the number of records exceeds capacity, the 'nextRecordsUrl'
     * field will be non-null.
     * 
     * @throws JsonSyntaxException
     * @throws IOException
     */
    public abstract <T extends QueryResultMetaData> QueryResult<T> query( String query, Type t ) throws JsonSyntaxException,
        IOException;
	
    /**
     * Retrieves remaining records from a query. The QueryResult passed in must have a non 'nextRecordsUrl' field.
     * 
     * @param <T> The type of records returned.
     * @param r A previously returned QueryResult with a non-null 'nextRecordsUrl' field.
     * @param t Should be the same type as T, and is required due to Java's handling of generics. Use the following
     * template to create this object:
     * 
     * Type t = new TypeToken<T>(){}.getType();
     * 
     * @return The result of the query. Never null. If the number of records exceeds capacity, the 'nextRecordsUrl'
     * field will be non-null.
     * 
     * @throws JsonSyntaxException
     * @throws IOException
     */
	public abstract <T extends QueryResultMetaData> QueryResult<T> query( QueryResult<T> r, Type t ) throws JsonSyntaxException,
        IOException;

    /**
     * Represents the result of a create object request. If id is null, the request failed.
     */
	public class CreateResult {
        private String id;

        public void setId( String id )
        {
            this.id = id;
        }

        public String getId()
        {
            return id;
        }
	}

    /**
     * An interface for representing extra meta-data included with all records returned from a query request.
     * 
     * See QueryResultMDBase for a simple implementation of this interface.
     */
	public interface QueryResultMetaData
	{
		public QueryResultAttribute getAttributes();
		public void setAttributes(QueryResultAttribute attr);
	}
	
    /**
     * Gives metadata added to each record returned by a query request.
     */
	public class QueryResultAttribute {
        private String type;
        private String url;

        public void setType( String type )
        {
            this.type = type;
        }

        public String getType()
        {
            return type;
        }

        public void setUrl( String url )
        {
            this.url = url;
        }

        public String getUrl()
        {
            return url;
        }
	}
	
    /**
     * Holds a list of records returned by a query request.
     * 
     * @param <T> The type of each record returned.
     */
	public class QueryResult<T extends QueryResultMetaData> {
		private boolean done;
		private int totalSize;
        private Collection<T> records = Arrays.asList();
        private String nextRecordsUrl;

        /**
         * @return Indicates if the query completed or not.
         */
        public boolean isDone()
        {
            return done;
        }

        /**
         * @return Number of records returned (in this request only; does not count records to be returned if a
         * nextRecordsUrl is present).
         */
        public int getTotalSize()
        {
            return totalSize;
        }

        /**
         * @return The list of records returned by the query. Will return a zero-length array if no records were
         * returned.
         */
        public Collection<T> getRecords()
        {
            return records;
        }

        /**
         * @return If non-null, then the query produced more records than could be returned. The value gives a URL from
         * which to request additional records.
         */
        public String getNextRecordsUrl()
        {
            return nextRecordsUrl;
        }
	}

}