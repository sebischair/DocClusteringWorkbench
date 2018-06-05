package spark;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

import javax.inject.Singleton;

@Singleton
public class SparkSessionComponent {
    private static SparkSessionComponent sparkSessionComponent;
    private final SparkSession sparkSession;

    public SparkSessionComponent() {
        SparkConf conf = new SparkConf().setAppName("SparkPipelines").setMaster("local");
        conf.set("spark.testing.memory", "471859200");
        conf.set("spark.ui.port", "9090");
        sparkSession = SparkSession.builder().config(conf).getOrCreate();
    }

    public static SparkSessionComponent getSparkSessionComponent(){
        if(null == sparkSessionComponent){
            sparkSessionComponent = new SparkSessionComponent();
        }
        return sparkSessionComponent;
    }

    public SparkSession getSparkSession() {
        return sparkSession;
    }
}
