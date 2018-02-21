package spark.dataloaders;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import spark.SparkSessionComponent;

import java.io.File;

public class CSVDataLoader implements ISparkDataLoader {
    private SparkSessionComponent sparkSessionComponent;

    public CSVDataLoader() {
        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
    }

    public Dataset<Row> loadData(String path) {
        String csvFile = new File(path).getAbsolutePath();
        return sparkSessionComponent.getSparkSession().read().csv(csvFile);
    }
}
