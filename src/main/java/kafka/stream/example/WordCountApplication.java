package kafka.stream.example;

import java.util.Arrays;
import java.util.Properties;

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
	
	 public static void main(final String[] args) throws Exception {
	        Properties props = new Properties();
	        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
	        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-broker1:9092");
	        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
	        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
	 
	        StreamsBuilder builder = new StreamsBuilder();
	        KStream<String, String> textLines = builder.stream("TextLinesTopic");
	        KTable<String, Long> wordCounts = textLines
	            .flatMapValues(textLine -> Arrays.asList(textLine.toLowerCase().split("\\W+")))
	            .groupBy((key, word) -> word)
	            .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store"));
	        wordCounts.toStream().to("WordsWithCountsTopic", Produced.with(Serdes.String(), Serdes.Long()));
	 
	        KafkaStreams streams = new KafkaStreams(builder.build(), props);
	        streams.start();
	    }

}