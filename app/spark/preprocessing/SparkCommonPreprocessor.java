package spark.preprocessing;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static spark.utils.SparkStringColumnUtil.concatStringTypeColumns;
import static spark.utils.SparkStringColumnUtil.removePunctuation;
import static spark.utils.SparkStringColumnUtil.toLowerCase;

public class SparkCommonPreprocessor {
    public static Dataset<Row> commonPreprocess(Dataset<Row> dataset, String[] listOfStringAttributeNames) {
        if(listOfStringAttributeNames != null) {
            dataset = concatStringTypeColumns(listOfStringAttributeNames, dataset);
        }
        dataset = toLowerCase("document", dataset);
        dataset = removePunctuation("document", dataset);
        return dataset;
    }
}
