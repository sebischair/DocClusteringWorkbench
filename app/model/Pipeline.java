package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Manoj on 10/26/2016.
 */
@Entity("pipelines")
@Indexes(@Index(value = "name", fields = @Field("name")))
public class Pipeline extends  PersistentEntity {
    @Id
    private ObjectId _id;

    @Indexed
    private String name;

    @Embedded
    List<Label> labels;

    private String classifier;

    private List<String> stages;

    private List<String> miningAttributes;

    private int split;

    private String modelPath;

    private Date createdAt;

    private String tag;

    public Pipeline() { }

    public Pipeline(String name, List<Label> labels, int split, List<String> stages, String model_path, Date created_at) {
        this.name = name;
        this.labels = labels;
        this.split = split;
        this.stages = stages;
        this.modelPath = model_path;
        this.createdAt = created_at;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Label> getLabels() {
        return this.labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public int getSplit() {
        return split;
    }

    public void setSplit(int split) {
        this.split = split;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getMiningAttributes() {
        return miningAttributes;
    }

    public void setMiningAttributes(List<String> miningAttributes) {
        this.miningAttributes = miningAttributes;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }
}
