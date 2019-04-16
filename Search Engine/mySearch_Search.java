package search.scorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class mySearch_Search {
	public int tot_docs_cnt;
	public Long tot_words;
	Boolean index_present = true;
	Set<String> words = new HashSet<String>();
	Map<String,Integer> word_loc = new HashMap<String,Integer>();
	Map<String,Map<String,Integer>> srchResult = new HashMap<String,Map<String,Integer>>();
	Map<Integer,String> doc_name_id = new HashMap<Integer,String>();
	Map<String,Integer> doc_term_count;
	File fl = new File(constants.file_inv_idx_loc);
	public Map<String, Integer> word_ttf = new HashMap<String,Integer>();
	Map<String, HashMap<String, String>> word_doc_loc = new HashMap<String,HashMap<String,String>>();
	
	mySearch_Search()
	{
		retrieveIndexReport();
		retrieveDocumentCatalog();
		retrieveIndexCatalog();
	}
	
	private void retrieveIndexCatalog() {
		// TODO Auto-generated method stub
		File fl = new File(constants.idx_cat);
		
		try
		{
			FileReader flrdr = new FileReader(fl);
			BufferedReader rdr = new BufferedReader(flrdr);
			if(fl.exists())
			{
				String line;
				String[] sp;
				while( (line = rdr.readLine()) != null)
				{
					sp = line.split(" ");
					word_loc.put(sp[0],Integer.parseInt(sp[1]));
				}
			}
			rdr.close();
			flrdr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void retrieveDocumentCatalog() {
		try
		{
			File catfl = new File(constants.file_doc_cat_loc);
			FileReader flrdr = new FileReader(catfl);
			BufferedReader rdr = new BufferedReader(flrdr);
			String tmp;
			String[] doc_cat;
			while((tmp = rdr.readLine()) != null)
			{
				doc_cat = tmp.split(":");
				doc_name_id.put(Integer.parseInt(doc_cat[1]), doc_cat[0]);
			}
			rdr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Map<String, Map<String, Integer>> searchPhrase( ArrayList<String> qry, boolean prox_search)//String query)
	{
		if(index_present && fl.exists())
		{
			for(String s: qry)
				for(String word: s.split(" "))
					words.add(word);
			searchIndex(prox_search);
			return srchResult;
		}
		else
		{
			System.out.println("INDEX NOT FOUND");
			return null;
		}
	}
	private void searchIndex(boolean prox_search)
	{
		try
		{
			FileWriter flwrtr = new FileWriter(new File(constants.prox_srch_results));
			BufferedWriter wrtr = new BufferedWriter(flwrtr);
			FileReader flrdr1 = new FileReader(fl);
			LineNumberReader rdr = new LineNumberReader(flrdr1);
//			BufferedReader rdr = new BufferedReader(flrdr1);
			String line,w;
			Set<String> words_arranged = new TreeSet<String>();
			int ttf=0,count,loc,curr_line=0;
			String[] idx_data,doc_list,doc_term;
			//Boolean EOF = false;
			
			words_arranged.addAll(words);
			Iterator<String> it = words_arranged.iterator();
			
			while(it.hasNext())
			{
				w = it.next();
				if(word_loc.containsKey(w))
					{
						loc = word_loc.get(w);
						while(curr_line <= loc-1)
							{rdr.readLine();
							curr_line++;}
						line = rdr.readLine();
						curr_line++;
						wrtr.write(line+System.lineSeparator());
						idx_data = line.split(":");
						doc_term_count = new HashMap<String,Integer>();
						doc_list = idx_data[1].split(";");
						for(String tmp: doc_list)
						{
							doc_term = tmp.split("#");
							count = doc_term[1].split(",").length;
							doc_term_count.put(doc_name_id.get(Integer.parseInt(doc_term[0])),count);
							ttf = ttf+ (count);
						} //
						srchResult.put(idx_data[0], doc_term_count);
						word_ttf.put(idx_data[0],ttf);
						ttf = 0;
					}
				else
				{
					srchResult.put(w,new HashMap<String,Integer>());
					word_ttf.put(w,0);
				}
			}
		/*  while(!words.isEmpty() && !EOF)
			{
				
				if((line = rdr.readLine())!= null)
				{
					idx_data = line.split(":");
					if(words.contains(idx_data[0]))
					{
						wrtr.write(line+System.lineSeparator());
						doc_term_count = new HashMap<String,Integer>();
						doc_list = idx_data[1].split(";");
						for(String tmp: doc_list)
						{
							doc_term = tmp.split("#");
							count = doc_term[1].split(",").length;
							doc_term_count.put(doc_name_id.get(Integer.parseInt(doc_term[0])),count);
							ttf = ttf+ (count);
						} //
						srchResult.put(idx_data[0], doc_term_count);
						words.remove(idx_data[0]);
						word_ttf.put(idx_data[0],ttf);
						ttf = 0;
					}
				}
				else
					EOF = true;
			}*/
			
/*			if(!words.isEmpty())
			{
				String s;
				Iterator<String> it = words.iterator();
				while(it.hasNext())
				{
					s = it.next();
					srchResult.put(s,new HashMap<String,Integer>());
					word_ttf.put(s,0);
				}
			}*/
			rdr.close();
			wrtr.close();
			flwrtr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void retrieveIndexReport() 
	{
		try{
			File idx_report = new File(constants.file_idx_report_loc);
			String tmp;
			String[] words;
			if(idx_report.exists())
			{
				FileReader flrdr = new FileReader(idx_report);
				BufferedReader rdr = new BufferedReader(flrdr);
				tmp=rdr.readLine();
				words = tmp.split(":");
				tot_docs_cnt = Integer.parseInt(words[1]);
				tot_words = Long.parseLong(words[3]);
				flrdr.close();
				rdr.close();
			}
			else
			{
				System.out.println("INDEX IS EMPTY");
				index_present = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
