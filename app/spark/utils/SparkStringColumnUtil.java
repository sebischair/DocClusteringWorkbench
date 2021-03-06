package spark.utils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

public class SparkStringColumnUtil {

    /*
    * Concats row wise all values of passed string type columns
    * Returns Dataset containing a new column called "document" containing concatenated string values
    */
    public static Dataset<Row> concatStringTypeColumns(String[] columns, Dataset<Row> inputDataset) {
        int noofcolumns = columns.length;
        Dataset<Row> newDataset = inputDataset;
        System.out.println(columns[0]);
        newDataset.show();
        newDataset = newDataset.withColumn("document", newDataset.col(columns[1]));
        for(int i = 2; i < noofcolumns; i++) {
            newDataset = newDataset.withColumn("document", functions.concat_ws(" ", newDataset.col("document"), newDataset.col(columns[i])));
        }
        newDataset.show();
        return newDataset;
    }

    /*
    * Remove all punctuations from the text corpus
    * Returns Dataset containing a new or renames column called "document"
    * */
    public static Dataset<Row> removePunctuation(String columnName, Dataset<Row> inputDataset) {
        inputDataset = inputDataset.withColumn("document", functions.regexp_replace(inputDataset.col(columnName), "^\\w", " "));
        return inputDataset.withColumn("document", functions.regexp_replace(inputDataset.col(columnName), "\\p{Punct}|\\d", " "));
    }

    public static Dataset<Row> toLowerCase(String columnName, Dataset<Row> inputDataset){
        return inputDataset.withColumn("document", functions.lower(inputDataset.col(columnName)));
    }

    public static Dataset<Row> addIDColumn(Dataset<Row> inputDataset){
        return inputDataset.withColumn("DOC_ID", functions.monotonically_increasing_id());
    }
}
