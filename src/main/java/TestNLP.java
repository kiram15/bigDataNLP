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


public final class TestNLP {

    public static String text = "I bought this for my husband who plays the piano. " +
            "He is having a wonderful time playing these old hymns. " +
            "The music  is at times hard to read because we think the book was published for singing from more than playing from. " +
            "Great purchase though!";

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

        Dataset<Row> json_dataset = sc.read().json(args[0]);
        
        //need to grab the reviews
        JavaPairRDD<Double, String> overall_review = json_dataset.javaRDD().mapToPair( row -> 
            new Tuple2<>(row.getDouble(2), row.getString(3)));
	
        
        JavaPairRDD<Double, Double> ratings = overall_review.mapValues( review -> nlp(review) );
        
        ratings.saveAsTextFile(args[1]);

        //need to pass each review to the nlp function to each of the reviews
	        

        sc.stop();

    }
}
