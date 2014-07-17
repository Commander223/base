package utils;


import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.safehaus.subutai.configuration.manager.impl.utils.PropertiesConfigurationLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/16/14.
 */
public class PropertiesConfigurationLoaderTest {

    @Test
    public void test() {
        PropertiesConfigurationLoader loader = new PropertiesConfigurationLoader();
        //        Config o = loader.getConfiguration( null, null, null );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println( json );


        JsonObject  jo = new JsonObject();
        jo.addProperty( "path", "full/path/to/file" );
        jo.addProperty( "type", "YAML" );

        JsonObject cf = new JsonObject();
        cf.addProperty( "fieldName", "field_name" );
        cf.addProperty( "label", "Label" );
        cf.addProperty( "required", true );
        cf.addProperty( "uiType", "TextField" );
        cf.addProperty( "value", "Value" );

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(cf);
        jo.add( "configField", jsonArray );

        Gson gsonNew = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson( jo ));

        Set<Map.Entry<String, JsonElement>> set = jo.entrySet();
        for ( Map.Entry<String, JsonElement> stringJsonElementEntry : set ) {
            System.out.println(stringJsonElementEntry.getKey() + " " + stringJsonElementEntry.getValue());

        }

    }
}
