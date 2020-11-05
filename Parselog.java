import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.util.*;
import java.time.LocalDateTime; // Import the LocalDateTime class
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class Parselog {

    public static class Map extends MapReduceBase implements
            Mapper<LongWritable, Text, Text, IntWritable> {

        @Override
        public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter)
                throws IOException {

                String line = value.toString();
                StringTokenizer tokenizer = new StringTokenizer(line);

                // Scanner scanner = new Scanner(System.in);
                while (tokenizer.hasMoreTokens()) {

                    String t = scanner.nextToken().split("- - \\[")[1].split(" -")[0];
                    SimpleDateFormat accesslogDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", new Locale("es","ES"));
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-mm-dd HH:00:00.000\t1");
                    // %Y-%m-%d %H:00:00.000

                    Date result = null;
                    String result_s = null;
                    try {
                            result = accesslogDateFormat.parse(t);
                            result_s = outputDateFormat.format(result);
                            output.collect(result_s, new IntWritable(1)); //System.out.println(result_s);

                    }catch(ParseException e) {
                            e.printStackTrace();
                    }// End of Try Catch

                }// End of while

        }
    }

    public static class Reduce extends MapReduceBase implements
            Reducer<Text, IntWritable, Text, IntWritable> {

        @Override
        public void reduce(Text key, Iterator<IntWritable> values,
                OutputCollector<Text, IntWritable> output, Reporter reporter)
                throws IOException {

            Map<String, Integer> map = new HashMap<String, Integer>();

            // Scanner scanner = new Scanner(System.in);
            while (values.hasNext()) {

                String data[] = values.next().split("\t");

              // System.out.println("debug: " +  data[0] + ":" + data[1]);

              if( map.get(data[0]) != null ){
                map.put(data[0], map.get(data[0]) + 1);  
              }else{
                map.put(data[0], 1);
              }
            }// End of while

            for (String key : map.keySet()) {
              // use the key here
              output.collect(key+ ":" + map.get(key) , new IntWritable(sum)); //System.out.println(key+ ":" + map.get(key));

            }// End of for
            
        }
    }

    public static void main(String[] args) throws Exception {

        JobConf conf = new JobConf(WordCount.class);
        conf.setJobName("parselog");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        conf.setMapperClass(Map.class);
        conf.setReducerClass(Reduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        JobClient.runJob(conf);

    }
}