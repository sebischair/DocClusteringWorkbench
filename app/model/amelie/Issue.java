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

    private ObjectNode getIssueDetails(JsonNode obj) {
        ObjectNode issue = Json.newObject();
        issue.set("name", obj.get("name"));
        if(obj.has("fields")) {
            JsonNode fields = obj.get("fields");
            issue.put("summary", fields.get("summary").asText(""));

            String description = fields.get("description") != null ? fields.get("description").asText("") : "temp";
            issue.put("description", description);

            issue.put("created", fields.get("created").asText(""));
            issue.put("resolved", fields.get("resolutiondate").asText(""));

            if(fields.has("project"))
                issue.put("belongsTo", fields.get("project").get("key").asText(""));
            if(fields.has("issuetype"))
                issue.put("issueType", fields.get("issuetype").get("name").asText(""));
            if(fields.has("status"))
                issue.put("status", fields.get("status").get("name").asText(""));
            if(fields.has("resolution") && fields.get("resolution").get("name") != null)
                issue.put("resolution", fields.get("resolution").get("name").asText(""));
            if(fields.has("priority"))
                issue.put("priority", fields.get("priority").get("name").asText(""));
            if(fields.has("assignee") && fields.get("assignee").has("displayName"))
                issue.put("assignee", fields.get("assignee").get("displayName").asText(""));
            if(fields.has("reporter"))
                issue.put("reporter", fields.get("reporter").get("name").asText(""));
        }
        if(obj.has("amelie")) {
            JsonNode amelie = obj.get("amelie");
            issue.set("designDecision", amelie.get("designDecision"));
            issue.set("decisionCategory", amelie.get("decisionCategory"));
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
        }
        return issue;
    }
}
