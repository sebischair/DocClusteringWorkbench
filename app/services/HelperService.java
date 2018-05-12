package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.*;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;
import static util.StaticFunctions.getStringValueFromSCObject;

public class HelperService {
    private WSClient ws;

    private static Configuration configuration = Configuration.root();
    private static String SC_BASE_URL = configuration.getString("sc.base.url");
    private static String USER_NAME = configuration.getString("sc.userName");
    private static String PASSWORD = configuration.getString("sc.password");

    public HelperService(WSClient ws) {
        this.ws = ws;
    }

    private CompletionStage<JsonNode> getWSResponseWithAuth(String url) {
        return ws.url(url).setAuth(USER_NAME, PASSWORD, WSAuthScheme.BASIC).get().thenApply(WSResponse::asJson);
    }

    private CompletionStage<JsonNode> entitiesForPath(String path) {
        return getWSResponseWithAuth(path);
    }

    private CompletionStage<JsonNode> entityForUid(String entityId) {
        return getWSResponseWithAuth(SC_BASE_URL + "entities/" + entityId);
    }

    public List<ObjectNode> getSCData(String type_url, List<String> miningAttributes) {
        Map<String, ArrayList<String>> map = new HashMap<>();
        List<ObjectNode> entityArray = new ArrayList<>();
        miningAttributes.forEach(attribute -> map.put(attribute, new ArrayList<String>()));
        Logger.info("Begin getting data from SC");
        HelperService hs = new HelperService(ws);
        hs.entitiesForPath(type_url + "/entities").thenApply(entityObject -> {
            entityObject.forEach(entity -> this.entityForUid(entity.get("id").asText()).thenApply(e -> {
                JsonNode entityAttributes = e.get("attributes");
                ObjectNode entityAttributePairs = new ObjectNode(new JsonNodeFactory(false));
                miningAttributes.forEach(miningAttribute -> {
                    String text = "";
                    String textValue = getStringValueFromSCObject(entityAttributes, miningAttribute);
                    if (textValue != null) text = text.concat("," + textValue);
                    if (!Objects.equals(text, "")) (map.get(miningAttribute)).add(text.replaceAll("class", ""));
                    entityAttributePairs.set(miningAttribute, Json.toJson(text));
                });
                entityArray.add(entityAttributePairs);
                return ok();
            }).toCompletableFuture().join());
            return ok();
        }).toCompletableFuture().join();
        Logger.info("Completed getting data from SC");
        return entityArray;
    }
}