import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;


public class MiddleOnly {

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
        double sum = 0.0;

        for (int score = 1; score < sentence_sentiment_scores.size() - 1; score++)
            sum += sentence_sentiment_scores.get(score);

        rating = sum / (sentence_sentiment_scores.size() - 2);

        System.out.println("rating is: " + (rating));
    }
}
