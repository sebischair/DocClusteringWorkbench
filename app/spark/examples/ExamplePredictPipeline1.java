package spark.examples;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import spark.SparkSessionComponent;

import java.util.Arrays;
import java.util.List;

public class ExamplePredictPipeline1 {

    private static SparkSessionComponent sparkSessionComponent;

    public static Dataset<Row> predictLables(String pipelineName, String textToCluster){
        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
        SparkSession spark = sparkSessionComponent.getSparkSession();
        // Prepare test documents.
        List<Row> dataText = Arrays.asList(RowFactory.create(textToCluster));

        StructType schema = new StructType(new StructField[]{
                new StructField("document", DataTypes.StringType, false, Metadata.empty())
        });

        Dataset<Row> textData = spark.createDataFrame(dataText, schema);
        PipelineModel savedModel = PipelineModel.load("myresources/models/" + pipelineName);
        return savedModel.transform(textData);
    }
}
