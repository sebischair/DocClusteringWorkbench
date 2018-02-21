package spark.examples;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import play.Logger;
import spark.SparkSessionComponent;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

/*
* This is an Example ML KMeans Pipeline with Preprocessors: Tokenizer, Remove Stop Words, Word2Vec.
* Input: CSV file with Summary & Description of Design Decisions
* Output: Cluster Labeled Dataset
*
*
* */
@Singleton
public class ExampleKMeansPipeline1 {

    private SparkSessionComponent sparkSessionComponent;

    public Dataset<Row> trainPipeline() {
        Logger.debug("\n...........................Example Predict 1: Tokenizer, Remove StopWords, Word2Vec, KMeans...........................");
        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();

        SparkSession spark = sparkSessionComponent.getSparkSession();
        Logger.debug("\n");

        // Load and parse data
        String path = new File("myresources/datasets/tasksNoHeader.csv").getAbsolutePath();
        Dataset<Row> inputData = spark.read().csv(path);

        //Display Loaded Dataset
        inputData.show();

        //Concat the String columns
        inputData = inputData.withColumn("document", functions.concat_ws(" ", inputData.col("_c0"), inputData.col("_c1")));

        //Setup tokenizer
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("document")
                .setOutputCol("words");

        //Setup Stop words removal
        StopWordsRemover stopWordsRemover = new StopWordsRemover()
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("filtered");

        //Setup Word2Vec
        Word2Vec word2Vec = new Word2Vec()
                .setInputCol(stopWordsRemover.getOutputCol())
                .setOutputCol("features")
                .setVectorSize(100)
                .setMinCount(0);

        KMeans kmeans = new KMeans()
                .setK(20)
                .setFeaturesCol("features")
                .setPredictionCol("cluster_label")
                .setMaxIter(20);

        Pipeline pipeline = new Pipeline()
                .setStages(new PipelineStage[]{tokenizer, stopWordsRemover, word2Vec, kmeans});

        // Fit the pipeline to training documents.
        PipelineModel model = pipeline.fit(inputData);
        try {
            model.write().overwrite().save("myresources/models/kmeans-example-1-model");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Dataset<Row> results = model.transform(inputData);

        Logger.debug("\n......Saving Results...........................");
        results.write().mode("overwrite").format("json").save("myresources/results/example-pipeline-1");
        Logger.debug("\n...........................Example PipeLine 1: The End...........................");

        Dataset<Row> sortedResults = results.sort(results.col("cluster_label"));
        return sortedResults;
    }
}
