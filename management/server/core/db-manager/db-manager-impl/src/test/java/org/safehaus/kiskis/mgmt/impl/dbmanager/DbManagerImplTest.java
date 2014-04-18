/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.dbmanager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

/**
 *
 * @author dilshat
 * @todo use classpath sql file with one single table
 */
//@Ignore
public class DbManagerImplTest {

    @Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("pi.sql", true, true, "test"));
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final DbManager dbManager = new DbManagerImpl();

    private final String source = "source";
    private final String key = "key";
    private final String content = "content";

    @Before
    public void setUp() {
        ((DbManagerImpl) dbManager).setSession(cassandraCQLUnit.session);
    }

    @After
    public void tearDown() {
        dbManager.deleteInfo(source, key);
    }

    @Test
    public void testSaveInfo() {
        assertTrue(dbManager.saveInfo(source, key, new MyPojo(content)));
    }

    @Test
    public void testGetInfo() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        MyPojo myPojo2 = dbManager.getInfo(source, key, MyPojo.class);

        assertEquals(myPojo, myPojo2);
    }

    @Test
    public void testGetInfoList() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        List<MyPojo> list = dbManager.getInfo(source, MyPojo.class);

        assertFalse(list.isEmpty());
    }

    @Test
    public void testDeleteInfo() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        dbManager.deleteInfo(source, key);

        List<MyPojo> list = dbManager.getInfo(source, MyPojo.class);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testExecuteUpdate() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        MyPojo myPojo2 = new MyPojo("test");

        dbManager.executeUpdate("update product_info set info = ? where source = ? and key = ?", gson.toJson(myPojo2), source, key);

        MyPojo myPojo3 = dbManager.getInfo(source, key, MyPojo.class);

        assertEquals(myPojo2.getContent(), myPojo3.getContent());
    }

    @Test
    public void testExecuteQuery() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        ResultSet rs = dbManager.executeQuery("select * from product_info where source = ? and key = ?", source, key);

        Row row = rs.one();

        assertNotNull(row);
    }

}
