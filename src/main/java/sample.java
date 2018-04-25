
import java.io.*;
import java.lang.Object;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

import edu.stanford.nlp.trees.TreeCoreAnnotations;

/**
 * This class demonstrates building and using a Stanford CoreNLP pipeline.
 */
public class sample {

    public static String text = "I bought this for my husband who plays the piano. " +
            "He is having a wonderful time playing these old hymns. " +
            "The music  is at times hard to read because we think the book was published for singing from more than playing from. " +
            "Great purchase though!";

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        ArrayList<Integer> sentence_sentiment_scores = new ArrayList<Integer>();
        //pipeline.prettyPrint(document,System.out);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            //System.out.println(sentiment);
            //sentence_sentiment_scores.add(sentiment);
            //Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            //tree.pennPrint(System.out);

            if (sentiment.equals("Strongly Positive")) {
                sentence_sentiment_scores.add(2);
            } else if (sentiment.equals("Positive")) {
                sentence_sentiment_scores.add(1);
            } else if (sentiment.equals("Neutral")) {
                sentence_sentiment_scores.add(0);
            } else if (sentiment.equals("Negative")) {
                sentence_sentiment_scores.add(-1);
            } else {
                sentence_sentiment_scores.add(-2);
            }

        }
        System.out.println(sentence_sentiment_scores);
        int rating = 0;
        if (sentence_sentiment_scores.size() > 1) {
            int minimum = Collections.min(sentence_sentiment_scores);
            int minIndex = sentence_sentiment_scores.indexOf(minimum);
            int maximum = Collections.max(sentence_sentiment_scores);
            int maxIndex = sentence_sentiment_scores.indexOf(maximum);
            //System.out.println(minimum+"\t"+maximum);
        }
    }
}