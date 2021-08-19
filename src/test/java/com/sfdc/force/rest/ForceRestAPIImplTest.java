package com.sfdc.force.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;
import com.sfdc.force.rest.ForceRestAPI.CreateResult;
import com.sfdc.force.rest.ForceRestAPI.QueryResult;

public class ForceRestAPIImplTest
{

    ForceRestAPIImpl api;
    static String quoteId;

    @Before
    public void setUp() throws Exception
    {
        api = new ForceRestAPIImpl( "https://crm.aps3.blitz.salesforce.com",
                                    "justin.bailey@salesforce.com",
                                    "R2xz5nNK4bl3",
                                    "6513628620583637421",
                                    "3MVG9WQsPp5nH_EpM_KnrLdttExwSfn.21hXy27.yzuIAOCrZjd9XbhTJ7GRJd9c70soOZFsaXL5rs82ReQky" );
    }

    @Test
    public void testCreate() throws IOException
    {
        CreateResult result = api.create( "sfquote__Quote__c", new QuoteCreator( "01230000000AIzd", "006D000000nwq3U" ) );

        quoteId = result.getId();

        assertNotNull( quoteId );
    }

    @Test
    public void testGet() throws IOException
    {
        Quote q = api.get( "sfquote__Quote__c", quoteId, new TypeToken<Quote>()
        {
        }.getType() );

        assertNotNull( q );
        assertNotNull( q.sfquote__Opportunity__c );
    }

    @Test
    public void testQuery() throws IOException
    {
        QueryResult<QuoteLine> quoteLines = api.query( "SELECT name, id, sfquote__Quote__r.name from sfquote__Quote_Line__c WHERE sfquote__Quote__c = '"
                                                               + quoteId + "'",
                                                       new TypeToken<QueryResult<QuoteLine>>()
                                                       {
                                                       }.getType() );

        assertEquals( quoteLines.getTotalSize(), 0 );

        if( quoteLines.getNextRecordsUrl() != null )
        {
            quoteLines = api.query( quoteLines, new TypeToken<QueryResult<QuoteLine>>()
            {
            }.getType() );

            assertEquals( quoteLines.getTotalSize(), 0 );
        }
    }

    private class QuoteCreator
    {
        public QuoteCreator( String rId, String oppId )
        {
            RecordTypeId = rId;
            sfquote__Opportunity__c = oppId;
        }

        public String RecordTypeId = "01230000000AIzd";
        public String sfquote__Opportunity__c = "006D000000nwq3U";
    }

    private class Quote extends ForceObject
    {

        public Quote( String id, String name, String oppId )
        {
            super( id, name );
            sfquote__Opportunity__c = oppId;
        }

        public String sfquote__Opportunity__c;
    }

    public class ForceObject extends QueryResultMDBase
    {
        private String Id;
        private String Name;

        public ForceObject( String id, String name )
        {
            Id = id;
            Name = name;
        }

        public String getId()
        {
            return Id;
        }

        public String getName()
        {
            return Name;
        }
    }

    public class QuoteLine extends ForceObject
    {
        public QuoteLine( String id, String name, Quote q )
        {
            super( id, name );
            sfquote__Quote__r = q;
        }

        private Quote sfquote__Quote__r;

        public Quote getSfquote__Quote__r()
        {
            return sfquote__Quote__r;
        }

        public void setSfquote__Quote__r( Quote sfquote__Quote__r )
        {
            this.sfquote__Quote__r = sfquote__Quote__r;
        }
    }
}
