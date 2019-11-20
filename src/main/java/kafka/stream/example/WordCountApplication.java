package kafka.stream.example;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
/**
 * 	Input messages
 * 	----------------- 
 * 	all streams lead to kafka
   	hello kafka streams
   	join kafka summit
   	Output messages
   	---------------------
   	all     1
	streams 1
	lead    1
	to      1
	kafka   1
	hello   1
	kafka   2
	streams 2
	join    1
	kafka   3
	summit  1  
 * @author 255016
 *
 */
public class WordCountApplication {
	
	  public static void main(String[] args) {
	        Properties config = new Properties();
	        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
	        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
	        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
	        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

	        StreamsBuilder builder = new StreamsBuilder();
	        // 1 - stream from Kafka

	        KStream<String, String> textLines = builder.stream("word-count-input");
	        KTable<String, Long> wordCounts = textLines
	                // 2 - map values to lowercase
	                .mapValues(textLine -> textLine.toLowerCase())
	                // can be alternatively written as:
	                // .mapValues(String::toLowerCase)
	                // 3 - flatmap values split by space
	                .flatMapValues(textLine -> Arrays.asList(textLine.split("\\W+")))
	                // 4 - select key to apply a key (we discard the old key)
	                .selectKey((key, word) -> word)
	                // 5 - group by key before aggregation
	                .groupByKey()
	                // 6 - count occurrences
	                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("Counts"));

	        // 7 - to in order to write the results back to kafka
	        wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

	        KafkaStreams streams = new KafkaStreams(builder.build(), config);
	        streams.start();



	        // shutdown hook to correctly close the streams application
	        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

	        // Update:
	        // print the topology every 10 seconds for learning purposes
	        while(true){
	            System.out.println(streams.toString());
	            try {
	                Thread.sleep(5000);
	            } catch (InterruptedException e) {
	                break;
	            }
	        }


	    }

}
