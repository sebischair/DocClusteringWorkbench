package spark.ranking;

import org.apache.spark.ml.linalg.Vector;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class JaccardCoefficient{

    public Float jaccardSimilarity(Set<String> s1, Set<String> s2){
        Set<String> union = new HashSet<>(s1);
        union.addAll(s2);
        Integer union_length = union.size();

        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);

        Integer intersection_length = intersection.size();

        Float jaccardIndex = Float.valueOf(intersection_length) / Float.valueOf(union_length);
        return jaccardIndex;
    }

    public Double getSimilarity(Vector v1, Vector v2){
        if(v1.size() == v2.size()){
            double min_sum = 0, max_sum = 0;
            for(int i = 0;i<v1.size();i++ ){
                min_sum += min(v1.apply(1), v2.apply(i));
                max_sum += max(v1.apply(1), v2.apply(i));
            }
            return min_sum/max_sum;
        }
        return 0.0;
    }

}
