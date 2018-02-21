package spark.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import play.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FileUtil {
    public static List<String> getFilesList(String path) {
        File folder = new File(path);
        FilenameFilter filter = (current, name) -> new File(current, name).isDirectory();
        List<String> filenames = new ArrayList<>();
        File[] files = folder.listFiles(filter);
        if(null != files) {
            for(File file : files) {
                filenames.add(file.getName());
            }
        }
        return filenames;
    }

    public static StringBuilder jsonToCSVConverter(ArrayNode jsonData, List<String> attributes) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode jsonObject : jsonData) {
            if(jsonObject.isArray()) Logger.info("One of columns was found to be an Array and was ignored");
            for(int i=0; i<attributes.size(); i++) {
                String attributeName = attributes.get(i);
                String attributeValue = jsonObject.get(attributeName).asText(" ");
                if(attributeName != "name")
                    attributeValue = attributeValue.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("^\\w{1,10}\\b", " ").replaceAll("\\r\\n|\\r|\\n", " ");;
                sb.append(attributeValue);
                sb.append(",");
            }
            sb.append("\n");
        }
        return sb;
    }

    public static StringBuilder jsonToCSVConverter(ArrayNode jsonData) {
        StringBuilder sb = new StringBuilder();
        jsonData.forEach(jsonObject -> {
            Iterator<Map.Entry<String, JsonNode>> attributes = jsonObject.fields();
            while(attributes.hasNext()) {
                Map.Entry<String, JsonNode> attribute = attributes.next();
                String attributeValue = attribute.getValue().asText("");
                if(!attributeValue.equals("")) {
                    attributeValue = attributeValue.replaceAll(",", "");
                    attributeValue = attributeValue;
                    sb.append(attributeValue);
                }
                sb.append(",");
            }
            sb.append("\n");
        });
        return sb;
    }

    public static File saveDataAsCSV(String path, StringBuilder content) throws FileNotFoundException {
        File file = new File(path);
        PrintWriter pw = new PrintWriter(file);
        pw.write(content.toString());
        pw.close();
        return file;
    }
}