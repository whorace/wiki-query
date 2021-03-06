package code.lemma;

import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import util.StringIntegerList;
import edu.umd.cloud9.collection.wikipedia.WikipediaPage;
import util.WikipediaPageInputFormat;
import code.lemma.Tokenizer;

/**
 * 
 *
 */
public class LemmaIndexMapred {
	public static class LemmaIndexMapper extends Mapper<LongWritable, WikipediaPage, Text, StringIntegerList> {

		@Override
		public void map(LongWritable offset, WikipediaPage page, Context context) throws IOException,
				InterruptedException {
			// TODO: implement Lemma Index mapper here
			Tokenizer tokenier=new Tokenizer();
			Text title=new Text();
			title.set(page.getTitle());
			List<String> content=tokenier.tokenize(page.getContent());	
			//String[] content=page.getContent().split(" ");
			HashMap<String,Vector<Integer>> word_map=new HashMap<String,Vector<Integer>>();
			int position=0;
			for(String lemma:content){
				Vector<Integer> cur;
				if(word_map.containsKey(lemma)){
					cur=word_map.get(lemma);
					cur.add(position);
					word_map.put(lemma, cur);
				}
				else{
					cur=new Vector<Integer>();
					cur.add(position);
					word_map.put(lemma, cur);
				}
				position++;
			}			
			StringIntegerList stringIntegerList=new StringIntegerList(word_map);		
			context.write(title,stringIntegerList);	
//			HashMap<String,Integer> word_frequency=new HashMap<String,Integer>();
//			for(String lemma:content){
//				if(word_frequency.containsKey(lemma)){
//					word_frequency.put(lemma,new Integer(word_frequency.get(lemma).intValue()+1));
//				}
//				else{
//					word_frequency.put(lemma, new Integer(1));
//				}			
//			}			
//			StringIntegerList stringIntegerList=new StringIntegerList(word_frequency);		
//			context.write(title,stringIntegerList);	
		}
	}
	
	public static void main(String[] args) throws Exception{
		
		 Configuration conf = new Configuration();
		 GenericOptionsParser gop=new GenericOptionsParser(conf, args);
		 String[] otherArgs=gop.getRemainingArgs();
		    Job job = Job.getInstance(conf, "word count");
		    job.setJarByClass(LemmaIndexMapred.class);
		    job.setMapperClass(LemmaIndexMapper.class);
		    
		    job.setInputFormatClass(WikipediaPageInputFormat.class);
		    
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(StringIntegerList.class);
		    

		    //job.setOutputFormatClass(OutputFormat.class);	 
		    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		    System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
