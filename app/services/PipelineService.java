package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import model.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import play.Logger;
import play.Play;
import spark.SparkSessionComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static spark.utils.FileUtil.getFilesList;

public class PipelineService {

    public static List<String> getAllTrainedModels(){
        return getFilesList("myresources/models");
    }

    public static List<String> getAllClustersResults(){
        File resultsFile = Play.application().getFile("myresources/results");
        return getFilesList(resultsFile.getAbsolutePath());
    }

    public static Dataset<Row> getPipelineClusters(String pipelineName){
        SparkSessionComponent sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
        SparkSession spark = sparkSessionComponent.getSparkSession();
        File resultsFile = Play.application().getFile("myresources/results/" + pipelineName);
        try {
            return spark.read().json(resultsFile.getAbsolutePath());
        } catch ( Exception e) {
            Logger.error("File not found: " + resultsFile.getAbsolutePath());
        }
        return null;
    }

    public static void saveClusterPipelineSettings(JsonNode settings){
        JsonNode algorithm_settings = settings.get("algorithm");
        Algorithm algorithm = new Algorithm();
        algorithm.setId(algorithm_settings.get("id").asText());
        algorithm.setName(algorithm_settings.get("name").asText());

        if(algorithm_settings.has("options")){
            List<Option> options = new ArrayList<>();
            ArrayNode options_settings = (ArrayNode) algorithm_settings.get("options");
            options_settings.forEach(option -> {
                Option optionObject = new Option();
                optionObject.setName(option.get("name").asText());
                optionObject.setValue(option.get("value").asInt());
                options.add(optionObject);
            });
            algorithm.setOptions(options);
        }

        ClusterPipeline clusterPipeline = new ClusterPipeline(
                settings.get("href").asText(""),
                settings.get("name").asText(""),
                settings.get("library").get("id").asText(""),
                algorithm,
                settings.get("transformer").get("id").asText(""),
                settings.get("dataset").asText("")
                );
        if(settings.get("scLink").asBoolean()){
            JsonNode scData = settings.get("scData");
            ArrayNode miningAtts = (ArrayNode) scData.get("miningAttributes");
            List<String> miningAttributes = new ArrayList<>();
            for(JsonNode att: miningAtts)
                miningAttributes.add(att.asText());
            clusterPipeline.setMiningAttributes(miningAttributes);

            SCTypeEntity type = new SCTypeEntity();
            JsonNode typeJson = scData.get("type");
            type.setHref(typeJson.get("href").asText());
            type.setId(typeJson.get("id").asText());
            type.setName(typeJson.get("name").asText());
            clusterPipeline.setType(type);
        }
        clusterPipeline.save();
    }

    public static List<? extends PersistentEntity> getAllClusterPipelines() {
        return new ClusterPipeline().getAll();
    }

    public static ClusterPipeline getClusterPipeline(String pipelineName) {
        return (ClusterPipeline) new ClusterPipeline().findByName("name", pipelineName);
    }
}
