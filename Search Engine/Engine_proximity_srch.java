package search.scorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;


public class Engine_proximity_srch {

	static Map<String,HashMap<String,TreeSet<Integer>>> word_doc_pos = new HashMap<String,HashMap<String,TreeSet<Integer>>>();
	static Map<String,String> doc_name_map = new HashMap<String,String>();
	static Map<String,Integer> qarray =  new LinkedHashMap<String,Integer>();
	static Map<Integer,Set<String>> qry_doclist = new HashMap<Integer,Set<String>>();
	static Map<Integer,Map<String,Double>> OkapiIDF = new HashMap<Integer,Map<String,Double>>();
	static Map<String,Integer> doc_length = new HashMap<String,Integer>();
	static int Result_Size = constants.NUMBER_OF_RESULTS;
	static long TOT_DOC_COUNT = constants.TOT_DOC_COUNT;
	static double OBM25_CONS_K1 = constants.OBM25_K1;
	static double OBM25_CONS_K2 = constants.OBM25_K2;
	static double OBM25_CONS_B = constants.OBM25_B;
	static double avg_doc_legth;
	static double corpusLength=0l;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long start = System.currentTimeMillis();
		
		readQuery();
		readCatalog();
		retrieveSearchResult();
		avg_doc_legth = (double)corpusLength/constants.TOT_DOC_COUNT;
		calcProximity();
//		writeOutput();
		
		System.out.println("TOTAL RUN TIME : " + (System.currentTimeMillis() - start)/1000);
	}

	private static void readQuery() {
		// TODO Auto-generated method stub
		String temp;
		String[] temp1;
		try 
		{
			FileReader flrdr = new FileReader(constants.file_queries_trmd);
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
	
	private static void readCatalog() {
		// TODO Auto-generated method stub
		String temp;
		String[] temp1;
		try 
		{
			FileReader flrdr = new FileReader(constants.file_doc_cat_loc);
			BufferedReader rdr = new BufferedReader(flrdr); 
			while ((temp = rdr.readLine()) != null && (temp.trim().length() > 1))
			{
				temp1 = temp.split(":");
				doc_name_map.put(temp1[1], temp1[0]);
				corpusLength = corpusLength + Long.parseLong(temp1[2]);
				doc_length.put(temp1[0],Integer.parseInt(temp1[2]));
//				System.out.println(temp1[0]+ ": " + temp1[2]);
			}
			rdr.close();
			flrdr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void retrieveSearchResult() {
		// TODO Auto-generated method stub
		File fl = new File(constants.prox_srch_results);
		String line;
		TreeSet<Integer> loc;
		HashMap<String,TreeSet<Integer>> doc_loc;
		try{
			if(fl.exists())
			{
				FileReader flrdr = new FileReader(fl);
				BufferedReader rdr = new BufferedReader(flrdr);
				String[] words,doclist,tmp;
				while((line = rdr.readLine())!= null)
				{
					doc_loc = new HashMap<String,TreeSet<Integer>>();
					
					words = line.split(":");
					doclist = words[1].split(";");
					for(String s: doclist)
					{
						loc = new TreeSet<Integer>();
						tmp = s.split("#");
						for(String t: tmp[1].split(","))
							loc.add(Integer.parseInt(t));
						
						doc_loc.put(doc_name_map.get(tmp[0]), loc);
					}
					word_doc_pos.put(words[0], doc_loc);
				}
			rdr.close();
			flrdr.close();
			}
			else
			{
				System.out.println("RUN PROXIMITIY SEARCH TO FETCH PESUDO RESULTS FROM INDEX");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void calcProximity() {
		// TODO Auto-generated method stub
		consQueryDocument();
		int qno;
		Set<String> doclist;
		ArrayList<TreeSet<Integer>> locs = new ArrayList<TreeSet<Integer>>();
		Map<String,Double> doc_scores = new HashMap<String,Double>();;
		TreeSet<Integer> tmp;
		try
		{
			File fl2 = new File(constants.prox_results);
			FileWriter flwrtr = new FileWriter(fl2);
			BufferedWriter wrtr = new BufferedWriter(flwrtr);
			int docL;
			double tfscore=0,tfd,dfw;
			for(String s: qarray.keySet())
			{
				doc_scores.clear(); 
				qno = qarray.get(s);
				doclist = qry_doclist.get(qno);
				for(String dname:doclist )
				{
					docL = doc_length.get(dname);
					dfw =0; tfd=0;
					for(String w: s.split(" "))
					{
						dfw =0;
						if(word_doc_pos.containsKey(w))
						{
							dfw = (int) word_doc_pos.get(w).size();
							tfd = 0;
							if(word_doc_pos.get(w).containsKey(dname))
							{
								tfd = (int)word_doc_pos.get(w).get(dname).size();
								tmp = word_doc_pos.get(w).get(dname);
								locs.add(tmp);
							}
						}
//						tfscore = tfscore + calcOkapiIDF(tfd,dfw,docL);
						tfscore = tfscore + calcOkapiBM25(tfd,1,dfw,docL);
					}
					
					doc_scores.put(dname,calcValue(locs,qno,dname)+tfscore);
					tfscore =0;
					locs.clear();
				}
				writeOutput(qno,doc_scores,wrtr);
			}
			wrtr.close();
			flwrtr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void consQueryDocument() {
		// TODO Auto-generated method stub
		Set<String> tmp;
		for(String qry: qarray.keySet())
		{
			tmp = new HashSet<String>();
			for(String s:qry.split(" "))
			{
				if(word_doc_pos.containsKey(s))
					tmp.addAll(word_doc_pos.get(s).keySet());
			}
			qry_doclist.put(qarray.get(qry), tmp);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static double calcValue(ArrayList<TreeSet<Integer>> locs, int qno,String dname) 
	{
		if(dname.startsWith("AP890107-0001"))
			qno = (int) qno;
		int count = locs.size(),minspan=90000;
		int min=9000,max=-1,min_idx=0;
		Iterator[] its = new Iterator[count];
		boolean not_end = true;
		for(int i=0;i<count;i++)
		{
			Iterator it = locs.get(i).iterator();
			its[i] = it;
		}
		int[] val = new int[count];
		for(int i=0; i<count;i++)
			val[i] = (int)its[i].next();
		
		do
		{
			for(int i=0; i<val.length;i++)
			{
				if(val[i] < min)
					{
						min = val[i]; 
						min_idx = i;
					}
				if(val[i] > max)
					max = val[i];
			}
			minspan = Integer.min(minspan,(max-min));
			
			if(its[min_idx].hasNext())
				val[min_idx] = (int) its[min_idx].next();
			else
				not_end = false;
		}while(not_end);
		System.out.println(dname + " : " + minspan);
		double rslt = Math.pow(0.8,((minspan-count)/count));
		return rslt;
	}
	
	private static double calcOkapiBM25(double tfd, double tfq,double dfw, double cur_doc_length) 
	{
		double scr_part1 = (Math.log(TOT_DOC_COUNT + 0.5)/Math.log(dfw +0.5));
		double scr_part2 = (tfd+(OBM25_CONS_K1 *tfd))/(tfd + (OBM25_CONS_K1 *((1-OBM25_CONS_B)+(OBM25_CONS_B*(cur_doc_length/avg_doc_legth)))));
		double scr_part3 = (tfq+(OBM25_CONS_K2 *tfq))/(tfq + OBM25_CONS_K2);
		double scr = scr_part1*scr_part2*scr_part3;
		return scr;
	}
	
	public static double calcOkapiIDF(double tfd, double dfw,double cur_doc_length) 
	{
		double scr1 = tfd / (tfd + 0.5 + (1.5 * (cur_doc_length /avg_doc_legth)));
		double scr2 = (Math.log(TOT_DOC_COUNT)/Math.log(dfw));
		 return scr1*scr2;
	}
	
	private static void writeOutput(int qno, Map<String, Double> doc_scores, BufferedWriter wrtr) {
		// TODO Auto-generated method stub
		Map<String,Double> results;
		results = sortByComparator(doc_scores, false);
		Object[] docs = results.keySet().toArray();
    	String[] doc_ids = Arrays.copyOf(docs, docs.length, String[].class);
    	int looptimes = (doc_ids.length > Result_Size) ? Result_Size:doc_ids.length;
    	try{
    		for(int i =0; i < looptimes ; i++)
        	{
        		wrtr.write(qno + " Q0 " + doc_ids[i]+ " " + (i+1) +" " + results.get(doc_ids[i]) + " Exp"+System.lineSeparator());
        	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}

	private static Map<String, Double> sortByComparator(Map<String, Double> map, final boolean order) {
		List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(map.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}