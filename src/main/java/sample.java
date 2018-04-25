import java.io.*;
import java.util.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class sample
{
    public static String text = "Joe Smith was born in California.In 2017, he went to Paris, France in the summer.His flight left at 3:00pm on July 10th, 2017." +
            "max is a slut";


    /** Usage: java -cp "*" StanfordCoreNlpDemo [inputFile [outputTextFile [outputXmlFile]]] */
    public static void main(String[] args) throws IOException
    {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        //System.out.println("hello");

        //pipeline.prettyPrint(document,System.out);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences)
        {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            System.out.println(sentiment + "\t" + sentence);
            //Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            //tree.pennPrint(System.out);
        }
    }

}