package weka.filters;

import weka.core.stemmers.NullStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * Created by Manoj on 7/12/2017.
 */
public class WekaStringToWordVector {
    private StringToWordVector stw;
    private boolean tf = false;
    private boolean idf = false;
    private double periodicPruning = -1.0;
    private String attributeIndex = "first-last";
    private int minTermFrequency = 1;
    private int wordsToKeep = 1000;

    public WekaStringToWordVector() {
        stw = new StringToWordVector();
        init();
    }

    public void init() {
        stw.setTFTransform(tf);
        stw.setIDFTransform(idf);
        stw.setPeriodicPruning(periodicPruning);
        stw.setAttributeIndices(attributeIndex);
        stw.setMinTermFreq(minTermFrequency);
        stw.setWordsToKeep(wordsToKeep);
        stw.setStemmer(new NullStemmer());
        stw.setTokenizer(WekaTokenizer.getDefaultWordTokenizer());
        stw.setDebug(false);
    }

    public StringToWordVector get() {
        return stw;
    }
}
