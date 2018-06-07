package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.PersistentEntity;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.PipelineService;
import util.StaticFunctions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by Manoj on 10/26/2016.
 */
public class ClusteringPipelineController extends Controller {

    public Result getAllClusterPipelines() {
        JsonNode pipelines_json = Json.toJson(Json.parse("{}"));
        List<? extends PersistentEntity> pipelines = PipelineService.getAllClusterPipelines();
        JsonNode deserializedPipelines = Json.parse(StaticFunctions.deserializeToJSON(pipelines));
        return ok(((ObjectNode) pipelines_json).set("pipelines", deserializedPipelines));
    }

    public Result getPipeline(String pipelineName){
        PersistentEntity pipeline = PipelineService.getClusterPipeline(pipelineName);
        if(pipeline != null) {
            return ok(Json.parse(StaticFunctions.deserializeToJSON(pipeline)));
        } else {
            return ok(Json.newObject());
        }
    }

    public Result getTrainedPipelines() {
        List<String> results = PipelineService.getAllTrainedModels();
        return ok(Json.toJson(results));
    }

    public Result getLibraries() {
        try {
            FileInputStream conf = new FileInputStream(Play.application().getFile("conf/libraries.json"));
            JsonNode libraries = Json.parse(conf);
            return ok(libraries);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ok(Json.parse("{results: null}"));
        }
    }
}
