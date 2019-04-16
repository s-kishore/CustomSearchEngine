package search.scorer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class ScoringEngine {
	
	static Map<String, Integer> doc_length = new HashMap<String,Integer>();
	static Map<String, Map<String,Integer>> word_doc_list = new HashMap<String,Map<String,Integer>>();
	static Map<String, Integer> word_ttf = new HashMap<String,Integer>();
	static Map<String, Set<String>> cons_queries = new HashMap<String,Set<String>>();
	static Map<String,Integer> qarray =  new LinkedHashMap<String,Integer>();

	static long corpusLength = 0;

	public static void main(String args[])
	{
		long start = System.currentTimeMillis();
		
		getDataFromFiles();
		generateQueryResults();
		
		long end = System.currentTimeMillis();
		System.out.println("Run time: " + (end - start) / 1000);
	}
	

	public static void generateQueryResults() 
	{
		consQueryResults();
		ScoringFns.scoreSearchResults(doc_length,word_doc_list,word_ttf,cons_queries,qarray,corpusLength);
	}

	public static void consQueryResults()
	{
			Map<String, Integer> s;
			for(String q: qarray.keySet())
			{
				Set<String> docs = new HashSet<String>();
				for(String term: q.split(" "))
				{
//					System.out.println(term + " " +word_doc_list.get(term).keySet().size());
					s = word_doc_list.get(term);
					docs.addAll(s.keySet());
				}
				cons_queries.put(q, docs);
			}
	}

	private static void getDataFromFiles() {
		getQueries();
		getDocStats();
		getSearchResults();
	}

	private static void getQueries() {
		String temp;
		String[] temp1;
		try 
		{
			FileReader flrdr = new FileReader(esConstants.QUERY_FILE);
			BufferedReader rdr = new BufferedReader(flrdr);
			while ((temp = rdr.readLine()) != null && (temp.trim().length() > 1))
			{
				temp1 = temp.split(" ",2);
				qarray.put(temp1[1],Integer.parseInt(temp1[0]));
			}
			rdr.close();
			flrdr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	private static void getSearchResults() {
		String temp,term = null;
		String[] temp1;
		boolean term_written = true;
		Map<String,Integer> term_freq_holder = new HashMap<String,Integer>();
		try 
		{
			FileReader flrdr = new FileReader(esConstants.SEARCH_RESULTS);
			BufferedReader rdr = new BufferedReader(flrdr);
			while ((temp = rdr.readLine()) != null && (temp.trim().length() > 1))
			{
				temp1 = temp.split(" ",2);
				if(temp1[0].contains("TERM:"))
				{
					if(!term_written)
					{
						word_doc_list.put(term,term_freq_holder);
						term_freq_holder = new HashMap<String,Integer>();
					}
					term_written = false;
					term = temp1[1];
					temp = rdr.readLine();
					temp1 = temp.split(" ",2);
					word_ttf.put(term,Integer.parseInt(temp1[1]));
				}
				else
				{
					term_freq_holder.put(temp1[0], Integer.parseInt(temp1[1]));
				}
			}
			word_doc_list.put(term,term_freq_holder);
			term_freq_holder = new HashMap<String,Integer>();
			rdr.close();
			flrdr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void getDocStats() {
		String temp;
		String[] temp1;
		try 
		{
			FileReader flrdr = new FileReader(esConstants.DOC_STATS);
			BufferedReader rdr = new BufferedReader(flrdr);
			temp = rdr.readLine();
			temp1 = temp.split(" ", 2);
			corpusLength = Long.parseLong(temp1[1]);
			while ((temp = rdr.readLine()) != null && (temp.trim().length() > 1))
			{
				temp1 = temp.split(" ",2);
				doc_length.put(temp1[0], Integer.parseInt(temp1[1]));
			}
			rdr.close();
			flrdr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}	
}
