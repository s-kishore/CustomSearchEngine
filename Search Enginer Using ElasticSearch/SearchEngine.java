package search.scorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.json.simple.parser.ParseException;

public class SearchEngine {
	static Node node = NodeBuilder.nodeBuilder().clusterName("elasticsearch").node();
	static Client client = node.client();
	static Map<String, Map<String,Integer>> word_doc_list = new HashMap<String,Map<String,Integer>>();
	static Map<String, Integer> word_ttf = new HashMap<String,Integer>();
	static Set<String> stopwords = new LinkedHashSet<String>();
	static Hashtable<String, String> stemTbl = new Hashtable<String, String>();
	static ArrayList<String> queries = new ArrayList<String>();
	static ArrayList<query> qarray = new ArrayList<query>();
	static String tmp;
	static Integer ttf;
	
	public static void main(String[] args) throws IOException, ParseException {
		long start = System.currentTimeMillis();
		
		QueryIndex();
		WriteResultsToFile();
		WriteQueriesToFile();
		
//		 Program end process
		long end = System.currentTimeMillis();
		System.out.println("Run time: " + (end - start) / 1000);

		client.close();		
		node.stop();
		node.close();
	}

	public static void QueryIndex()
	{
		CreateStopStemList();
		qarray = ReadQueryFile();
		for(String s: queries)
			phraseSearch(s);
	}

	private static void WriteResultsToFile() {
		// TODO Auto-generated method stub
		try 
		{
			PrintWriter wrtr = new PrintWriter("searchResults.txt");
			Map<String,Integer> termResult;
			Set<String> keys = word_doc_list.keySet(); 
			for(String key : keys)
			{
				wrtr.println("TERM: " + key);
				wrtr.println("TTF: " + word_ttf.get(key));
				termResult = word_doc_list.get(key);
				Set<String> docs = termResult.keySet();
				for(String docid : docs)
				{
					wrtr.println(docid + " " + termResult.get(docid) );
				}
			}
			wrtr.close();
		} 
		
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void WriteQueriesToFile() 
	{
		try 
		{
			PrintWriter wrtr = new PrintWriter("Querys.txt");
			for(query q:qarray)
			{
				wrtr.println(q.qno + " " + q.short_query);
			}
			wrtr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void CreateStopStemList() {
		String temp;
		try 
		{
			FileReader flrdr = new FileReader(esConstants.StopListLocation);
			BufferedReader rdr = new BufferedReader(flrdr);
			while((temp = rdr.readLine())!= null && (temp.trim().length() > 0))
			{
				stopwords.add(temp);
			}
			flrdr.close();
			rdr.close();
		
			File stemfl = new File(esConstants.DiffLocation);
			Scanner scnr;
			
				scnr = new Scanner(stemfl);
				while(scnr.hasNextLine())
				{
					Scanner scnr1 = new Scanner(scnr.nextLine());
					while(scnr1.hasNext())
					{
						String key = scnr1.next();
						if(scnr1.hasNext())
							stemTbl.put(key, scnr1.next());
						else
							System.out.println("error for key :" + key);
					}
					scnr1.close();
				}
				scnr.close();
		}
		 catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	public static ArrayList<query> ReadQueryFile()
	{
		ArrayList<query> queriesList = new ArrayList<query>();
		int qno;
		
		FileReader queryfile;
		BufferedReader flrdr;
		String curline,temp;
		try 
		{
			queryfile = new FileReader(esConstants.QueryLocation);
			flrdr = new BufferedReader(queryfile);
			while((curline = flrdr.readLine())!= null && (curline.trim().length() > 0))
			{
				query qry;
				String[] q = curline.split(" ", 7);
				qno = Integer.parseInt((String)q[0].subSequence(0,(q[0].length()-1)));
				if(q[6].startsWith("or"))
				{	
					q= q[6].split(" ",3);
					temp = RemoveStopWords(q[2]);
					qry = new query(qno, q[2],temp);
					queriesList.add(qry);
				}
				else
				{
					temp = RemoveStopWords(q[6]);
					qry = new query(qno, q[6],temp);
					queriesList.add(qry);
				}
				queries.add(temp);
				temp="";
			}
			queryfile.close();
			return queriesList;
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void phraseSearch(String query)
	{
		 for(String word : query.split(" "))
			 if (!word_doc_list.containsKey(word))
			 {
				 word_doc_list.put(word, searchIndexByWord(word));
				 word_ttf.put(word, ttf);
			 } 		
	}	
	
	public static Map<String,Integer> searchIndexByWord(String query)
	{
		int tf = 0;
		ttf = 0;
		Map<String,Integer> doc_list = new HashMap<String,Integer>();
		SearchResponse response = client.prepareSearch("ap_dataset")
				.setTypes("document")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchPhraseQuery("text",query)) //matchPhraseQuery
				.setFrom(0).setSize(100000).setExplain(true)
				.execute()
				.actionGet();
		
		SearchHit[] results = response.getHits().getHits();
		for(SearchHit r : results)
		{
			tmp = r.getId();
			Explanation[] ea = r.getExplanation().getDetails();
			ea = ea[0].getDetails();
			ea = ea[0].getDetails();
			tf = (int) ea[0].getValue();
			ttf = tf + ttf;
			doc_list.put(tmp,tf);
		}
		System.out.println(query +" : " + results.length);
		return doc_list;
	}
	
	public static  String RemoveStopWords(String query)
	{
		String temp= new String();
		
		//.replaceAll("-", " ")
		query = query.replaceAll("^\\p{Punct}$", "").replaceAll("\"", "");
		for(String s: query.split(" "))
		{
			if(!stopwords.contains(s))
				if(s.endsWith(",") || s.endsWith("."))
				{
					temp = temp.concat(s.substring(0,s.length()-1) + " ");
				}
				else
					temp = temp.concat(s + " ");
		}
		temp = temp.trim();
		temp = temp.replaceAll("  ", " ");
		return temp;
	}
}
