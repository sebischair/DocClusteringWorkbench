package spark;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import javax.inject.Inject;

import java.io.IOException;

import static spark.SparkStringColumnUtil.concatStringTypeColumns;
import static spark.SparkStringColumnUtil.removePunctuation;
import static spark.SparkStringColumnUtil.toLowerCase;

public class BaseClusterPipeline implements ISparkClusterPipeline {
    private @Inject
    SparkSessionComponent sparkSessionComponent;

    private ISparkDataLoader dataLoader;
    private PipelineStage[] basePipelineStages;
    private Pipeline basePipeline;
    private Tokenizer tokenizer;
    private StopWordsRemover stopWordsRemover;
    private Word2Vec word2Vec;
    private KMeans kMeans;


    public BaseClusterPipeline(ISparkDataLoader Dl){
        if(Dl.getClass() == CSVDataLoader.class){
            dataLoader = new CSVDataLoader();
        }
        //TODO: Initialize PreprocessStaging Component
        createPipelineStages();
    }

    private void createPipelineStages(){
        tokenizer = new Tokenizer()
                .setInputCol("document")
                .setOutputCol("words");

        stopWordsRemover = new StopWordsRemover()
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("filtered");

        word2Vec = new Word2Vec()
                .setInputCol(stopWordsRemover.getOutputCol())
                .setOutputCol("features")
                .setVectorSize(100)
                .setMinCount(0);


        kMeans = new KMeans()
                .setK(20)
                .setFeaturesCol("features")
                .setPredictionCol("cluster_label")
                .setMaxIter(20);

        basePipelineStages = new PipelineStage[] {tokenizer, stopWordsRemover, word2Vec, kMeans} ;
        basePipeline =  new Pipeline().setStages(basePipelineStages);
    }

    public void trainPipeline(String path, String[] listOfStringAttributeNames){
        Dataset<Row> dataSet = dataLoader.loadData(path);
        dataSet = SparkCommonPreprocessor.commonPreprocess(dataSet, listOfStringAttributeNames);
        PipelineModel pipelineModel = basePipeline.fit(dataSet);
        try {
            pipelineModel.save("../myresources/models/base-kmeans-model");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
