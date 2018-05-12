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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        issue.put("name", obj.get("name"));
        if (obj.has("fields")) {
            JsonNode fields = obj.get("fields");
            issue.put("summary", fields.get("summary").asText(""));

            String description = fields.get("description") != null ? fields.get("description").asText("") : "";
            issue.put("description", description);
            if (description != null) {
                issue.put("shortDescription", StaticFunctions.truncate(description));
            } else {
                issue.put("shortDescription", "");
            }

            issue.put("created", fields.get("created").asText(""));
            issue.put("resolved", fields.get("resolutiondate").asText(""));

            if (fields.has("project") && fields.get("project").has("key"))
                issue.put("belongsTo", fields.get("project").get("key").asText(""));
            if (fields.has("issuetype") && fields.get("issuetype").has("name"))
                issue.put("issueType", fields.get("issuetype").get("name").asText(""));
            if (fields.has("status") && fields.get("status").has("name"))
                issue.put("status", fields.get("status").get("name").asText(""));
            if (fields.has("resolution") && fields.get("resolution").get("name") != null)
                issue.put("resolution", fields.get("resolution").get("name").asText(""));
            if (fields.has("priority") && fields.get("priority").has("name"))
                issue.put("priority", fields.get("priority").get("name").asText(""));
            if (fields.has("assignee") && fields.get("assignee").has("displayName"))
                issue.put("assignee", fields.get("assignee").get("displayName").asText(""));
            if (fields.has("reporter") && fields.get("reporter").has("name"))
                issue.put("reporter", fields.get("reporter").get("name").asText(""));
        }
        if (obj.has("amelie")) {
            JsonNode amelie = obj.get("amelie");
            issue.put("designDecision", amelie.get("designDecision"));
            issue.put("decisionCategory", amelie.get("decisionCategory"));
            if (amelie.hasNonNull("concepts")) {
                issue.set("concepts", amelie.get("concepts"));
            } else
                issue.put("concepts", "");
            issue.set("keywords", amelie.get("keywords"));
            if (amelie.hasNonNull("qualityAttributes"))
                issue.set("qualityAttributes", amelie.get("qualityAttributes"));
            else
                issue.put("qualityAttributes", "");
        }
        return issue;
    }

    public List<ObjectNode> orderIssuesByResolutionDate(ArrayNode issues) {
        List<ObjectNode> jsonValues = new ArrayList<>();
        issues.forEach(issue -> jsonValues.add((ObjectNode) issue));

        Collections.sort(jsonValues, new Comparator<ObjectNode>() {
            private static final String KEY_NAME = "resolved";

            @Override
            public int compare(ObjectNode a, ObjectNode b) {
                String valA = a.get(KEY_NAME).asText("");
                String valB = b.get(KEY_NAME).asText("");

                if (!valA.equals("") && !valB.equals("")) {
                    LocalDate dateA = LocalDate.parse(valA, StaticFunctions.DATE_FORMAT);
                    LocalDate dateB = LocalDate.parse(valB, StaticFunctions.DATE_FORMAT);
                    return dateA.compareTo(dateB);
                } else {
                    if (valA.equals(valB)) {
                        return 0;
                    }
                    if (!valA.equals("")) {
                        return 1;
                    }
                    if (!valB.equals("")) {
                        return -1;
                    }
                    return -1;
                }
            }
        });
        return jsonValues;
    }

    public ArrayNode findAllIssuesInAProject(String projectKey) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("fields.project.key", projectKey)).iterator();
        while (cursor.hasNext()) {
            issues.add(getIssueDetails(Json.toJson(cursor.next())));
        }
        return issues;
    }
}
