import java.io.*;
import java.lang.Object;
import java.util.*;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ie.util.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import edu.stanford.nlp.trees.TreeCoreAnnotations;
import org.apache.spark.api.java.function.PairFunction;


public final class TestNLP {

    private static PairFunction<Tuple2<String, Tuple2<Double, Integer>>,String,Double> getAverageByKey = (tuple) -> {
        Tuple2<Double, Integer> val = tuple._2;
        double total = val._1;
        double count = val._2;
        Tuple2<String, Double> averagePair = new Tuple2<String, Double>(tuple._1, total / count);
        return averagePair;
    };

    public static double nlp(String review){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(review);
        pipeline.annotate(document);

        ArrayList<Integer> sentence_sentiment_scores = new ArrayList<Integer>();
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);

            switch (sentiment) {
                case "Strongly Positive":
                    sentence_sentiment_scores.add(5);
                    break;
                case "Positive":
                    sentence_sentiment_scores.add(4);
                    break;
                case "Neutral":
                    sentence_sentiment_scores.add(3);
                    break;
                case "Negative":
                    sentence_sentiment_scores.add(2);
                    break;
                default: // "Strongly Negative":
                    sentence_sentiment_scores.add(1);
            }
        }

        System.out.println("sentence scores: " + sentence_sentiment_scores);

        double rating = 0.0;
        double sum = 0;

        for (int score : sentence_sentiment_scores)
            sum += score;

        rating = sum / (sentence_sentiment_scores.size());

        return rating;
    }

    public static void main(String[] args) throws Exception {

        SparkSession sc = SparkSession
                .builder()
                .appName("testNLP")
                .getOrCreate();

        //Local Application initialization
        //SparkConf conf = new SparkConf().setMaster("spark://jackson:30280").setAppName("IdealPageRank");
        //JavaSparkContext sc = new JavaSparkContext(conf);

        //read in the reviews from input json file and put into the Dataset
        Dataset<Row> json_dataset = sc.read().json(args[0]);
        
        //put the asin and user given star ratings into JavaRDD
        JavaPairRDD<String, Double> asin_overall = json_dataset.javaRDD().mapToPair( row -> 
            new Tuple2<>(row.getString(0), row.getDouble(2)));
            
        //put the user give and user written review into JavaRDD
        JavaPairRDD<String, String> asin_review = json_dataset.javaRDD().mapToPair( row -> 
            new Tuple2<>(row.getString(0), row.getString(3)));

        //returns an RDD with <asin, nlp calculated rating>
        JavaPairRDD<String, Double> nlpRatings = asin_review.mapValues( review -> nlp(review) );
        
        //Calculate averages for each of the asin for the overall ratings (from JSON)
        //count each values per key
        JavaPairRDD<String, Tuple2<Double, Integer>> valueCount = asin_overall.mapValues(value -> new Tuple2<>(value,1));
        //add values by reduceByKey
        JavaPairRDD<String, Tuple2<Double, Integer>> reducedCount = valueCount.reduceByKey((tuple1,tuple2) ->  new Tuple2<>(tuple1._1 + tuple2._1, tuple1._2 + tuple2._2));
        //calculate average
        JavaPairRDD<String, Double> overallAveragePair = reducedCount.mapToPair(getAverageByKey);

        //TODO: calculate averages for each of the asin for nlpRatings
        //Calculate averages for each of the asin for the overall ratings (from JSON)
        //count each values per key
        JavaPairRDD<String, Tuple2<Double, Integer>> valueCount2 = nlpRatings.mapValues(value -> new Tuple2<>(value,1));
        //add values by reduceByKey
        JavaPairRDD<String, Tuple2<Double, Integer>> reducedCount2 = valueCount2.reduceByKey((tuple1,tuple2) ->  new Tuple2<>(tuple1._1 + tuple2._1, tuple1._2 + tuple2._2));
        //calculate average
        JavaPairRDD<String, Double> nlpRatingsAveragePair = reducedCount2.mapToPair(getAverageByKey);

        
        //TODO: join together the two averages (one using overall and one using nlpRankings) by product ID
        JavaPairRDD<String, Tuple2<Double, Double>> joinedAverages = overallAveragePair.join(nlpRatingsAveragePair);
        
        //output file (asin, [avergae overallRating, avergae nlpRanking])
        joinedAverages.saveAsTextFile(args[1]);

        sc.stop();

    }
}
