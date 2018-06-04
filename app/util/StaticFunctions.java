package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import db.DefaultMongoClient;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.util.*;

import static spark.utils.SparkDatasetUtil.clusterTableToJson;

/**
 * Created by Manoj on 10/24/2016.
 */
public class StaticFunctions {

    public static String truncate(String text){
        int limit = 150;
        if (text.length() > limit)
            return text.substring(0, limit) + " \u2026";
        else
            return text;
    }

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

    public static String cleanText(String str) {
        String result;
        if (str == null) return str;
        Document document = Jsoup.parse(str);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));// makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        result = document.html().replaceAll("\\\\n", "\n");
        result = Jsoup.clean(result, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        result = removeUrl(result);
        result = removeExtendedChars(result);
        return result;
    }

    public static String removeUrl(String str) {
        String regex = "\\b(https?|ftp|file|telnet|http|Unsure)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        str = str.replaceAll(regex, "");
        return str;
    }

    public static String removeExtendedChars(String str) {
        return str.toLowerCase().replaceAll("[^\\x00-\\x7F]", " ").replaceAll(" +", " ").replaceAll("[^a-zA-Z\\s]", " ").replaceAll("^\\w{1,10}\\b", " ").replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("class", "");
    }
}