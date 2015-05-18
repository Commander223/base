package org.safehaus.subutai.common.security;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class NullSubutaiLoginContextTest
{
    private NullSubutaiLoginContext context;


    @Test
    public void testGetInstance() throws Exception
    {
        assertNotNull( NullSubutaiLoginContext.getInstance() );
    }
}