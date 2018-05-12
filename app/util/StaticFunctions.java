package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import db.DefaultMongoClient;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static spark.utils.SparkDatasetUtil.clusterTableToJson;

/**
 * Created by Manoj on 10/24/2016.
 */
public class StaticFunctions {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static String getStringValueFromSCObject(JsonNode entityAttributes, String attributeName) {
        if (entityAttributes != null)
            for (int j = 0; j < entityAttributes.size(); j++) {
                JsonNode attr = entityAttributes.get(j);
                if (attr.get("name").asText("").equals(attributeName)) {
                    JsonNode jsonValue = attr.get("values");
                    if (jsonValue.size() > 0) {
                        return jsonValue.get(0).asText("");
                    }
                }
            }
        return null;
    }

    public static String deserializeToJSON(List<?> objList, String... removeAttributes) {
        List<DBObject> dbObjList = new ArrayList<>(objList.size());
        DBObject dbObj;
        for (Object obj : objList) {
            dbObj = DefaultMongoClient.morphia.toDBObject(obj);
            for (String removeAttribute : removeAttributes) {
                dbObj.removeField(removeAttribute);
            }
            dbObjList.add(dbObj);
        }
        return JSON.serialize(dbObjList);
    }

    public static String deserializeToJSON(Object obj, String... removeAttributes) {
        DBObject dbObj;
        dbObj = DefaultMongoClient.morphia.toDBObject(obj);
        for (String removeAttribute : removeAttributes) {
            dbObj.removeField(removeAttribute);
        }
        return JSON.serialize(dbObj);
    }

    //Method for sorting the TreeMap based on values
    public static <K, V extends Comparable<V>> TreeMap<K, V> sortByValues(final Map<K, V> map) {
        TreeMap<K, V> sortedByValues = new TreeMap<K, V>(Collections.reverseOrder());
                //new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    public static JsonNode getSortedClusterResults(Dataset<Row> dataset) {
        Dataset<Row> sortedResults = dataset.sort("cluster_label");
        return clusterTableToJson(sortedResults);
    }

    public static String truncate(String text){
        if (text.length() > 50)
            return text.substring(0, 50) + " ...";
        else
            return text;
    }
}