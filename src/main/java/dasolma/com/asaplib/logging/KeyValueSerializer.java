package dasolma.com.asaplib.logging;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by dasolma on 15/05/15.
 */
public class KeyValueSerializer extends TypeAdapter<KeyValueList> {
    @Override
    public void write(JsonWriter out, KeyValueList data) throws IOException {
        out.beginObject();
        for(int i=0; i<data.size();i++){
            out.name(data.get(i).getKey());
            Object value = data.get(i).getValue();
            if (value == null) value = "null";
            else {
                if( !(value instanceof String) && !(value instanceof Integer) &&
                        !(value instanceof Float) && !(value instanceof Double) ) {

                    Gson gson = new Gson();
                    value = gson.toJson(value);

                }
            }
            out.value(value.toString());
        }
        out.endObject();
    }
    /*I only need Serialization*/
    @Override
    public KeyValueList read(JsonReader in) throws IOException {
        return null;
    }
}