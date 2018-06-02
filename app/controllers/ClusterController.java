package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import model.ClusterPipeline;
import model.amelie.Issue;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.HelperService;
import services.PipelineService;
import spark.SparkSessionComponent;
import spark.pipelines.SparkPipelineFactory;
import spark.pipelines.SparkPredictPipeline;
import util.StaticFunctions;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static spark.utils.FileUtil.jsonToCSVConverter;
import static spark.utils.FileUtil.saveDataAsCSV;
import static spark.utils.SparkDatasetUtil.datasetToJson;
import static spark.utils.SparkDatasetUtil.extractClusterTablefromDataset;

public class ClusterController extends Controller {
    @Inject
    WSClient ws;

    public Result getClusterResults() {
        List<String> results = PipelineService.getAllClustersResults();
        return ok(Json.toJson(results));
    }

    public Result getAllClustersFromPipeline(String pipelineName){
        Dataset<Row> results = PipelineService.getPipelineClusters(pipelineName);
        results = results.drop("features");
        JsonNode jsonResults = StaticFunctions.getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(Json.toJson(jsonResults));
    }

    public Result datasetUpload() {
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> dataset = body.getFile("file");
        if (dataset != null) {
            String fileName = dataset.getFilename();
            File file = dataset.getFile();
            String newPath = "myresources/datasets/"+fileName;
            file.renameTo(new File(newPath));
            return ok(Json.parse("{ \"results\": { \"path\": \""+newPath+ " \"}}"));
        } else{
            return ok();
        }
    }

    public Result createClusterPipeline() {
        JsonNode data = request().body().asJson().get("pipeline");
        String filepath = data.has("dataset") ? data.get("dataset").asText("") : "temp";
        String projectKey = data.get("mongoProjectKey").asText("");

        if(data.hasNonNull("scLink") && data.get("scLink").asBoolean()) {
            String filename = data.get("dataset").asText();
            filepath = "myresources/datasets/"+filename;
            String scTypeURL = data.get("scData").get("type").get("href").asText();
            ArrayNode attributesToMine = (ArrayNode) data.get("scData").get("miningAttributes");
            List<String> miningAttributes = new ArrayList<>();
            for(JsonNode attribute: attributesToMine){
                miningAttributes.add(attribute.asText());
            }

            HelperService hs = new HelperService(this.ws);
            ArrayNode scData = hs.getSCData(scTypeURL, miningAttributes);
            StringBuilder records = jsonToCSVConverter(scData, miningAttributes);
            try {
                saveDataAsCSV(filepath, records);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if(projectKey != "") {
            Issue issueModel = new Issue();
            //ArrayNode decisions = issueModel.findAllIssuesInAProject(projectKey);
            ArrayNode decisions = issueModel.findAllDesignDecisionsInAProject(projectKey);
            List<String> miningAttributes = new ArrayList<>();
            miningAttributes.add("name");
            miningAttributes.add("summary");
            miningAttributes.add("description");
            filepath = "myresources/datasets/" + projectKey;
            StringBuilder records = jsonToCSVConverter(decisions, miningAttributes);
            try {
                saveDataAsCSV(filepath, records);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        JsonNode results = Json.toJson(Json.parse("{}"));

        switch(parseInt(data.get("library").get("id").toString())){
            case 2:{
                Logger.info("....WEKA.....");
                //Do Nothing : In Progress
            }
            break;
            case 1:
            default:
            {
                Logger.info("....Spark.....");
                SparkPipelineFactory sparkPipelineFactory = new SparkPipelineFactory(data);
                Dataset<Row> spark_results = sparkPipelineFactory.trainPipeline(projectKey, filepath, "csv");
                results = Json.toJson(datasetToJson(extractClusterTablefromDataset(spark_results)));
            }
            break;
        }
        JsonNode json_results = Json.toJson(Json.parse("{}"));
        return ok(((ObjectNode)json_results).set("results", results));
    }

    public Result getSimilarDocuments(){
        String pipelineName = request().body().asJson().get("pipelineName").asText();
        String textToCluster = request().body().asJson().get("textToClassify").asText();
        if(!pipelineName.isEmpty() && !textToCluster.isEmpty()) {
            ClusterPipeline pipeline = PipelineService.getClusterPipeline(pipelineName);
            ObjectNode results = Json.newObject();
            switch(pipeline.getLibrary()){
                case "1":
                    Logger.info(".....Prediction Library: Spark..................");
                    SparkPredictPipeline predictPipeline = new SparkPredictPipeline(pipelineName);
                    ArrayNode result = predictPipeline.predict(textToCluster);
                    results.set("result", Json.toJson(result));
                    break;
                case "2":
                    Logger.info("....Building in Progress.....");
                    //Do Nothing : In Progress
                    results.set("result", Json.toJson("In Progress"));
                    break;
                default:
                    results.set("result", Json.toJson("Not Found"));
                    Logger.info(".....Prediction Library: Not Found..................");
            }
            return ok(results);
        } else {
            ObjectNode result = Json.newObject();
            result.put("status", "KO");
            result.put("result", "{Missing pipeline name}");
            return ok(result);
        }
    }

    public Result updateSimilarDocuments(String projectKey) {
        SparkSessionComponent sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
        sparkSessionComponent.getSparkSession();

        ObjectNode result = Json.newObject();
        ClusterPipeline pipeline = PipelineService.getClusterPipeline(projectKey);
        SparkPredictPipeline predictPipeline = new SparkPredictPipeline(projectKey);
        if(pipeline != null) {
            Issue issueModel = new Issue();
            ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectKey);
            issues.forEach(issue -> {
                Logger.debug("Processing issues: " + issue.get("name").asText(""));
                String text = StaticFunctions.cleanText(issue.get("summary").asText("") + " " + issue.get("description").asText("")).toLowerCase();
                ArrayNode predictResult = predictPipeline.predict(text);
                List similarDocuments = new ArrayList();
                for(int i=0; i<predictResult.size(); i++) {
                    ObjectNode sd = (ObjectNode) predictResult.get(i);
                    Map similarDocument = new HashMap();
                    similarDocument.put("name", sd.get("_c0").asText(""));
                    if(sd.has("_c1")) similarDocument.put("summary", sd.get("_c1").asText("").trim());
                    else similarDocument.put("summary", "");
                    if(sd.has("_c2")) similarDocument.put("description", sd.get("_c2").asText("").trim());
                    else similarDocument.put("description", "");
                    //similarDocument.set("features", sd.get("filtered"));
                    similarDocument.put("cosinesimilarity", sd.get("cosinesimilarity").asText(""));
                    similarDocument.put("jaccardsimilarity", sd.get("jaccardsimilarity").asText(""));
                    similarDocuments.add(similarDocument);
                }
                if(similarDocuments.size() > 0) {
                    BasicDBObject newConcepts = new BasicDBObject();
                    newConcepts.append("$set", new BasicDBObject().append("amelie.similarDocuments", similarDocuments));
                    issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
                }
            });
            result.put("status", "OK");
            result.put("statusCode", "200");
        } else {
            result.put("status", "KO");
            result.put("statusCode", "400");
            result.put("result", "Could not find the pipeline: " + projectKey);
        }
        return ok(result);
    }
}

