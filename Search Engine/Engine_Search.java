package search.scorer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;


public class Engine_Search {
	static Map<String, Map<String,Integer>> word_doc_list = new HashMap<String,Map<String,Integer>>();
	static Map<String, Integer> word_ttf = new HashMap<String,Integer>();
	static Set<String> stopwords = new LinkedHashSet<String>();
	static Hashtable<String, String> stemTbl = new Hashtable<String, String>();
	static ArrayList<String> queries = new ArrayList<String>();
	static ArrayList<query> qarray = new ArrayList<query>();
	static String tmp;
	static Integer ttf;
	
	public static void main(String[] args){
		long start = System.currentTimeMillis();
//		Program start operations 
		
		QueryIndex();
		WriteResultsToFile();
		WriteQueriesToFile();
		
//		 Program end process
		long end = System.currentTimeMillis();
		System.out.println("Run time: " + (end - start) / 1000);
	}

	public static void QueryIndex()
	{
		CreateStopStemList();
		qarray = ReadQueryFile();
		//for(String s: queries)
		phraseSearch(queries);
		//proximitySearch(queries);
	}

	public static void WriteResultsToFile() {
		// TODO Auto-generated method stub
		try 
		{
			PrintWriter wrtr = new PrintWriter(constants.file_query_results);
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
	
	public static void WriteQueriesToFile() 
	{
		try 
		{
			PrintWriter wrtr = new PrintWriter(constants.file_queries);
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
			FileReader flrdr = new FileReader(constants.stop_list);
			BufferedReader rdr = new BufferedReader(flrdr);
			while((temp = rdr.readLine())!= null && (temp.trim().length() > 0))
			{
				stopwords.add(temp);
			}
			flrdr.close();
			rdr.close();
		
			File stemfl = new File(constants.stem_list);
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
			queryfile = new FileReader(constants.query_location);
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
	
	public static void phraseSearch(ArrayList<String> qry)//String query)
	{
		mySearch_Search srch = new mySearch_Search();
		word_doc_list.putAll(srch.searchPhrase(qry,true));
		word_ttf.putAll(srch.word_ttf);
	}	
	
	public static  String RemoveStopWords(String query)
	{
		String temp= new String(),tmp1;
		String stem_temp = new String();
		//.replaceAll("-", " ")
		query = query.replaceAll("^\\p{Punct}$", "").replaceAll("\"", "").replaceAll("-", " ");
		query = query.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("'"," ");
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
		for(String s: temp.split(" "))
		{
			//tmp1 =s;
			tmp1 = stemTbl.containsKey(s)? stemTbl.get(s): s;
			stem_temp = stem_temp.concat(tmp1 +" ");
		}
//		return temp;
		return stem_temp;
	}
}
