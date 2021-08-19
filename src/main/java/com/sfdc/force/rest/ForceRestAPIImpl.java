package com.sfdc.force.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class ForceRestAPIImpl implements ForceRestAPI
{

    private String domain;
    private String access_token;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private HttpClient httpClient = new DefaultHttpClient();

    public ForceRestAPIImpl( String dom, String username, String password, String client_secret, String client_id )
    {
        domain = dom;
        try
        {
            access_token = getAuthResult( username, password, client_secret, client_id ).access_token;
        }
        catch( IOException e )
        {
            e.printStackTrace();
            /*
             * TODO : add better exception handling for authentication failures
             */
        }
    }

    /*
     * (non-Javadoc)
     * @see foo.ForceRestAPI#post(java.lang.String, java.lang.Object)
     */
    public CreateResult create( String apiName, Object objectValues ) throws IOException
    {
        HttpPost post = null;
        HttpResponse resp;
        CreateResult result = null;

        try
        {
            post = new HttpPost( domain + "/services/data/v20.0/sobjects/" + apiName );
            post.setHeader( "Authorization", "Bearer " + access_token );
            post.setHeader( "Content-Type", "application/json" );
            post.setHeader( "Accept", "application/json" );
            post.setEntity( createJSONEntity( objectValues ) );

            resp = httpClient.execute( post );

            if( resp.getStatusLine().getStatusCode() < 200 || resp.getStatusLine().getStatusCode() >= 300 )
                return new CreateResult();

            result = gson.fromJson( EntityUtils.toString( resp.getEntity() ), new CreateResult().getClass() );
        }
        finally
        {
            if( post != null )
                post.releaseConnection();
        }

        return result;
    }

    private StringEntity createJSONEntity( Object objectValues ) throws UnsupportedEncodingException
    {
        StringEntity entity = new StringEntity( gson.toJson( objectValues ) );
        // entity.setContentEncoding( "application/json" );

        return entity;
    }

    private RestOAuth getAuthResult( String username, String password, String client_secret, String client_id )
        throws IOException
    {
        HttpPost post = new HttpPost( domain + "/services/oauth2/token" );
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add( new BasicNameValuePair( "username", username ) );
        nvps.add( new BasicNameValuePair( "password", password ) );
        nvps.add( new BasicNameValuePair( "grant_type", "password" ) );
        nvps.add( new BasicNameValuePair( "client_id", client_id ) );
        nvps.add( new BasicNameValuePair( "client_secret", client_secret ) );
        post.setEntity( new UrlEncodedFormEntity( nvps ) );
        HttpResponse response = httpClient.execute( post );

        RestOAuth res = gson.fromJson( EntityUtils.toString( response.getEntity() ), RestOAuth.class );
        return res;
    }

    /**
     * @author justin.bailey
     * 
     * Helper class for OAuth authentication against the Force.com API.
     */
    private class RestOAuth
    {
        public String id;
        public String issued_at;
        public String instance_url;
        public String signature;
        public String access_token;
    }

    /*
     * (non-Javadoc)
     * @see foo.ForceRestAPI#get(java.lang.String, java.lang.String, java.lang.reflect.Type)
     */
    public <T> T get( String apiName, String objectId, Type t ) throws IOException
    {
        HttpGet get = new HttpGet( domain + "/services/data/v20.0/sobjects/" + apiName + "/" + objectId );
        get.setHeader( "Authorization", "Bearer " + access_token );
        return gson.fromJson( EntityUtils.toString( httpClient.execute( get ).getEntity() ), t );
    }

    /*
     * (non-Javadoc)
     * @see foo.ForceRestAPI#buildQueryRequest(foo.QuoteConfiguratorTester.AuthResult , java.lang.String)
     */
    public <T extends QueryResultMetaData> QueryResult<T> query( String query, Type t ) throws JsonSyntaxException,
        IOException
    {
        HttpGet get = new HttpGet( domain + "/services/data/v20.0/query/?q=" + URLEncoder.encode( query, "UTF-8" ) );
        get.setHeader( "Authorization", "Bearer " + access_token );

        return gson.fromJson( EntityUtils.toString( httpClient.execute( get ).getEntity() ), t );
    }

    public <T extends QueryResultMetaData> QueryResult<T> query( QueryResult<T> r, Type t ) throws IOException
    {
        HttpGet get = new HttpGet( domain + r.getNextRecordsUrl() );
        get.setHeader( "Authorization", "Bearer " + access_token );

        return gson.fromJson( EntityUtils.toString( httpClient.execute( get ).getEntity() ), t );
    }

}