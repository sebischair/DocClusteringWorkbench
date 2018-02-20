package spark.pipelines;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import interfaces.IPredictPipeline;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import play.libs.Json;
import services.PipelineService;
import spark.SparkSessionComponent;
import spark.preprocessing.SparkCommonPreprocessor;
import spark.ranking.CosineSimilarity;
import spark.ranking.JaccardCoefficient;
import util.StaticFunctions;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.parseInt;

public class SparkPredictPipeline implements IPredictPipeline {

    private PipelineModel predictModel;
    private String pipelineName;
    private Integer predictedLabel;
    private StructType schema;
    private SparkSessionComponent sparkSessionComponent;
    private SparkSession spark;
    private String modelPath;
    private Dataset<Row> results;
    private Dataset<Row> cluster;
    private CosineSimilarity cosineSimilarity;
    private JaccardCoefficient jaccardCoefficient;
    private Map<Long, Double> cosineSimilarityMap;
    private Map<Long, Float> jaccardSimilarityMap;


    public SparkPredictPipeline(String pipelineName) {
        cosineSimilarity = new CosineSimilarity();
        jaccardCoefficient = new JaccardCoefficient();
        this.pipelineName = pipelineName;
        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
        spark = sparkSessionComponent.getSparkSession();
        modelPath = "myresources/models/" + pipelineName;
    }

    public ArrayNode predict(String textToCluster) {
        createSchema();
        readModel();
        transformText(textToCluster);
        getClusterByLabel();
        ArrayNode mapResults = Json.newArray();
        try {
            mapResults = applyRanking();
        } catch (FileAlreadyExistsException f) {
            File file = new File("myresources/models/word2vec");
            try {
                FileUtils.deleteDirectory(file);
                mapResults = applyRanking();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapResults;
    }

    private void createSchema() {
        this.schema = new StructType(new StructField[]{
                new StructField("document", DataTypes.StringType, false, Metadata.empty())
        });
    }

    private void readModel() {
        this.predictModel = PipelineModel.load(modelPath);
    }

    private void transformText(String textToCluster) {
        List<Row> textInput = Arrays.asList(RowFactory.create(textToCluster));
        Dataset<Row> inputDocuments = spark.createDataFrame(textInput, this.schema);
        inputDocuments = SparkCommonPreprocessor.commonPreprocess(inputDocuments, null);
        results = predictModel.transform(inputDocuments);
    }

    private void getClusterByLabel() {
        predictedLabel = results.select("cluster_label").first().getInt(0);
        Dataset<Row> cluster_table = PipelineService.getPipelineClusters(pipelineName);
        cluster_table.schema();
        cluster = cluster_table.filter("cluster_label=" + predictedLabel);
    }

    private ArrayNode applyRanking() throws IOException {
        Word2VecModel model = new Word2Vec().setInputCol("filtered")
                .setOutputCol("vectors")
                .setVectorSize(100)
                .setMinCount(0).fit(cluster);
        Dataset<Row> clusterDocuments = model.transform(cluster);
        Dataset<Row> queryDocuments = model.transform(results);
        calculateSimilarities(clusterDocuments, queryDocuments);
        TreeMap<Long, Double> sortedMap = StaticFunctions.sortByValues(cosineSimilarityMap);
        return getJsonFromCosineSimilarityMap(sortedMap);
    }

    private ArrayNode getJsonFromCosineSimilarityMap(TreeMap<Long, Double> sortedMap) {
        ArrayNode array = new ArrayNode(new JsonNodeFactory(true));
        sortedMap.forEach((doc_id, doc_similarity) -> {
            ObjectNode topDoc = (ObjectNode) Json.parse(cluster.filter("DOC_ID=" + doc_id).limit(1).toJSON().collectAsList().get(0));
            if (doc_similarity != null) {

                Float cosineSimilarity = doc_similarity.floatValue() * 100;
                Float jaccardsimilarity = (jaccardSimilarityMap.get(doc_id)) * 100;

                if (cosineSimilarity >= 40 || jaccardsimilarity >= 8) {
                    topDoc.set("cosinesimilarity", Json.toJson(String.format("%.2f", cosineSimilarity)));
                    topDoc.set("jaccardsimilarity", Json.toJson(String.format("%.2f", jaccardsimilarity)));
                    array.add(topDoc);
                }
            }
        });
        return array;
    }

    private void calculateSimilarities(Dataset<Row> clusterDocuments, Dataset<Row> queryDocuments) {
        cosineSimilarityMap = new HashMap<>();
        jaccardSimilarityMap = new HashMap<>();
        for (Row row : queryDocuments.collectAsList()) {
            Vector queryVector = (Vector) row.get(row.fieldIndex("vectors"));
            Set<String> queryWords = new HashSet<>(row.getList(row.fieldIndex("filtered")));
            for (Row document : clusterDocuments.collectAsList()) {
                Long documentId = document.getLong(document.fieldIndex("DOC_ID"));

                //Cosine Similarity
                Vector docVector = (Vector) document.get(document.fieldIndex("vectors"));
                Double cosinesimilarity = cosineSimilarity.getSimilarity(queryVector, docVector);
                cosineSimilarityMap.put(documentId, cosinesimilarity);

                //Jaccard Similarity
                Set<String> documentWords = new HashSet<>(document.getList(document.fieldIndex("filtered")));
                Float jaccardSimilarity = jaccardCoefficient.jaccardSimilarity(queryWords, documentWords);
                jaccardSimilarityMap.put(documentId, jaccardSimilarity);
            }
        }
    }

}