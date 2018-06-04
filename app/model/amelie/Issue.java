package model.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.AmelieMongoClient;
import org.bson.Document;
import play.libs.Json;
import util.StaticFunctions;

/**
 * Created by Manoj on 11/28/2017.
 */
public class Issue {
    private MongoCollection<Document> issueCollection;

    public Issue() {
        issueCollection = AmelieMongoClient.amelieDatabase.getCollection("issues");
    }

    public ArrayNode findAllDesignDecisionsInAProject(String projectKey) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("fields.project.key", projectKey).append("amelie.designDecision", true)).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(Json.toJson(cursor.next())));
        }
        return issues;
    }

    public ObjectNode getDesignDecisionByKey(String projectKey) {
        return getIssueDetails(Json.toJson(issueCollection.find(new BasicDBObject().append("name", projectKey)).first()));
    }

    private ObjectNode getIssueDetails(JsonNode obj) {
        ObjectNode issue = Json.newObject();
        issue.put("name", obj.get("name"));
        if(obj.has("fields")) {
            JsonNode fields = obj.get("fields");
            issue.put("summary", fields.get("summary").asText("").replaceAll("\\$", ""));

            String description = fields.get("description") != null ? fields.get("description").asText("").replaceAll("\\$", "") : "";
            issue.put("description", description);
            if(description != null) { issue.put("shortDescription", StaticFunctions.truncate(description)); }
            else { issue.put("shortDescription", ""); }

            issue.put("created", fields.get("created").asText(""));
            if(fields.has("resolutiondate"))
                issue.put("resolved", fields.get("resolutiondate").asText(""));

            if(fields.has("project"))
                issue.put("belongsTo", fields.get("project").get("key").asText(""));
            if(fields.has("issuetype"))
                issue.put("issueType", fields.get("issuetype").get("name").asText(""));
            if(fields.has("status"))
                issue.put("status", fields.get("status").get("name").asText(""));
            if(fields.has("resolution") && fields.get("resolution").get("name") != null)
                issue.put("resolution", fields.get("resolution").get("name").asText(""));
            if(fields.has("priority") && fields.get("priority").has("name"))
                issue.put("priority", fields.get("priority").get("name").asText(""));
            if(fields.has("assignee") && fields.get("assignee").has("name")) {
                String name = fields.get("assignee").get("name").asText("");
                if(name.contains("(")) {
                    name = name.split("\\(")[0];
                }
                issue.put("assignee", name);
            }
            if(fields.has("reporter") && fields.get("reporter").has("name"))
                issue.put("reporter", fields.get("reporter").get("name").asText(""));
        }
        if(obj.has("amelie")) {
            JsonNode amelie = obj.get("amelie");
            issue.put("designDecision", amelie.get("designDecision"));
            issue.put("decisionCategory", amelie.get("decisionCategory"));
            if(amelie.hasNonNull("concepts")) {
                issue.set("concepts", amelie.get("concepts"));
            }
            else
                issue.put("concepts", "");
            issue.set("keywords", amelie.get("keywords"));
            if(amelie.hasNonNull("qualityAttributes"))
                issue.set("qualityAttributes", amelie.get("qualityAttributes"));
            else
                issue.put("qualityAttributes", "");

            if(amelie.has("similarDocuments")) {
                issue.put("similarDocuments", amelie.get("similarDocuments"));
            }
        }
        return issue;
    }

    public void updateIssueByKey(String key, BasicDBObject newConcepts) {
        issueCollection.updateOne(new BasicDBObject().append("name", key), newConcepts);
    }
}
