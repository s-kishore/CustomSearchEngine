package search.scorer;

import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

public class ScoringFns {
	
	static Map<String, Integer> doc_length = new HashMap<String,Integer>();
	static Map<String, Map<String,Integer>> word_doc_list = new HashMap<String,Map<String,Integer>>();
	static Map<String, Integer> word_ttf = new HashMap<String,Integer>();
	static Map<String, Set<String>> cons_queries = new HashMap<String,Set<String>>();
	static Map<String,Integer> qarray =  new HashMap<String,Integer>();
	static Map<Integer,Map<String,Double>> OkapiTF = new HashMap<Integer,Map<String,Double>>();
	static Map<Integer,Map<String,Double>> OkapiIDF = new HashMap<Integer,Map<String,Double>>();
	static Map<Integer,Map<String,Double>> OkapiBM25 = new HashMap<Integer,Map<String,Double>>();
	static Map<Integer,Map<String,Double>> Laplace = new HashMap<Integer,Map<String,Double>>();
	static Map<Integer,Map<String,Double>> JelinekMercer = new HashMap<Integer,Map<String,Double>>();
	static SortedSet<Entry<String, Double>> sortedMap;
	static List<Integer> queryno = new ArrayList<Integer>();
	static long corpusLength = 0L;
	static double avg_doc_legth;
	static long TOT_DOC_COUNT = constants.TOT_DOC_COUNT;
	static double OBM25_CONS_K1 = constants.OBM25_K1;
	static double OBM25_CONS_K2 = constants.OBM25_K2;
	static double OBM25_CONS_B = constants.OBM25_B;
	static double LAMBDA = constants.LAMBDA;
	static long VOC_SIZE = constants.VOC_SIZE;
	static int RESULT_SIZE = constants.NUMBER_OF_RESULTS;
	
	
	public static void scoreSearchResults(Map<String, Integer> doc_length2, Map<String, Map<String, Integer>> word_doc_list2, 
			Map<String, Integer> word_ttf2, Map<String, Set<String>> cons_queries2, 
			Map<String, Integer> qarray2, long corpusLength1) 
	{
		doc_length = doc_length2;
		word_doc_list = word_doc_list2;
		word_ttf = word_ttf2;
		cons_queries = cons_queries2;
		qarray = qarray2;
		corpusLength = corpusLength1;
		avg_doc_legth = (double)corpusLength/constants.TOT_DOC_COUNT;
		beginScoring();
		System.out.println("Writing results");
		writeResults();
	}
	
	private static void writeResults() 
	{
		Map<String, Double> results = new LinkedHashMap<String,Double>();
		try 
		{
			//PrintWriter wrtr_TF = new PrintWriter(constants.RESULT_OKAPI_TF);
			PrintWriter wrtr_IDF = new PrintWriter(constants.RESULT_OKAPI_IDF);
			PrintWriter wrtr_BM25 = new PrintWriter(constants.RESULT_OKAPI_BM25);
			PrintWriter wrtr_L = new PrintWriter(constants.RESULT_LAPLACE);
			//PrintWriter wrtr_JM= new PrintWriter(constants.RESULT_JM);
			
			for(Integer qno : queryno)
			{
				//results = sortByComparator(OkapiTF.get(qno),false);
				//writeOutputFile(qno,results,wrtr_TF);
				results = sortByComparator(OkapiIDF.get(qno),false);
				writeOutputFile(qno,results,wrtr_IDF);
				results = sortByComparator(OkapiBM25.get(qno),false);
				writeOutputFile(qno,results,wrtr_BM25);
				results = sortByComparator(Laplace.get(qno),false);
				writeOutputFile(qno,results,wrtr_L);
				//results = sortByComparator(JelinekMercer.get(qno),false);
				//writeOutputFile(qno,results,wrtr_JM);
			}
			//wrtr_TF.close();
			wrtr_IDF.close();
			wrtr_BM25.close();
			wrtr_L.close();
			//wrtr_JM.close();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    private static void writeOutputFile(Integer qno,Map<String, Double> results, PrintWriter wrtr) 
    {
    	Object[] docs = results.keySet().toArray();
    	String[] doc_ids = Arrays.copyOf(docs, docs.length, String[].class);
    	int looptimes = (doc_ids.length > RESULT_SIZE) ? RESULT_SIZE:doc_ids.length;
    	for(int i =0; i < looptimes ; i++)
    	{
    		wrtr.println(qno + " Q0 " + doc_ids[i]+ " " + (i+1) +" " + results.get(doc_ids[i]) + " Exp");
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
	
	@SuppressWarnings("unused")
	private static void beginScoring() 
	{
		Map<String,Double> doc_scores_OTF;
		Map<String,Double> doc_scores_OIDF;
		Map<String,Double> doc_scores_OBM25;
		Map<String,Double> doc_scores_LS;
		Map<String,Double> doc_scores_JMS;
		int query_no;
		double score_tf = 0;
		double score_idf = 0;
		double score_bm25 = 0;
		double score_ls = 0;
		double score_jms = 0;
		
		for(String query: cons_queries.keySet())
		{
			doc_scores_OTF = new HashMap<String,Double>();
			doc_scores_OIDF = new HashMap<String,Double>();
			doc_scores_OBM25 = new HashMap<String,Double>();
			doc_scores_LS = new HashMap<String,Double>();
			doc_scores_JMS = new HashMap<String,Double>();
			int c_OTF =0, c_IDF=0,c_OBM=0,c_ls=0,c_jms=0;
			
			for(String docid : cons_queries.get(query))
			{
				for(String term: query.split(" "))
				{
					double dfw = (double)word_doc_list.get(term).size();
					double cur_doc_length = (double)doc_length.get(docid);
					double tfq = (double)getWordCountInQuery(query,term);
					double tfd = 0;
					if(word_doc_list.get(term).containsKey(docid))
					{	tfd = (double)word_doc_list.get(term).get(docid);
						//score_tf = score_tf + calcOkapiTF(tfd,dfw,cur_doc_length);
						score_idf = score_idf + calcOkapiIDF(tfd,dfw,cur_doc_length);
						score_bm25 = score_bm25 + calcOkapiBM25(tfd,tfq,dfw,cur_doc_length);
					}
					score_ls = score_ls + calcLaplace(tfd,cur_doc_length);
					//score_jms = score_jms + calcJelinekMercer(tfd,cur_doc_length,term);
					
				}
				
				//doc_scores_OTF.put(docid, score_tf);
				doc_scores_OIDF.put(docid, score_idf);
				doc_scores_OBM25.put(docid, score_bm25);
				doc_scores_LS.put(docid, score_ls);
				//doc_scores_JMS.put(docid, score_jms);

				score_tf = 0;
				score_idf = 0;
				score_bm25 = 0;
				score_ls = 0;
				score_jms = 0;
			}
			query_no = qarray.get(query);
			queryno.add(query_no);
			//OkapiTF.put(query_no, doc_scores_OTF);
			OkapiIDF.put(query_no, doc_scores_OIDF);
			OkapiBM25.put(query_no, doc_scores_OBM25);
			Laplace.put(query_no,doc_scores_LS);
			//JelinekMercer.put(query_no, doc_scores_JMS);
		}
	}

	public static double calcJelinekMercer(double tfd, double cur_doc_length,String term) 
	{
		double jm_part1 = LAMBDA * (tfd/cur_doc_length);
		double jm_part2 = (1- LAMBDA) * (((double)word_ttf.get(term) -  tfd)/((double)corpusLength - cur_doc_length));
		double scr = Math.log(jm_part1 + jm_part2);
		if(scr < -200)
			return 0;
		return scr;
	}

	private static double calcLaplace(double tfd, double cur_doc_length) 
	{
		double scr = (tfd + 1)/(cur_doc_length +(double)VOC_SIZE);
		scr = Math.log(scr);
		return scr;
	}

	private static double calcOkapiBM25(double tfd, double tfq,double dfw, double cur_doc_length) 
	{
		double scr_part1 = (Math.log(TOT_DOC_COUNT + 0.5)/Math.log(dfw +0.5));
		double scr_part2 = (tfd+(OBM25_CONS_K1 *tfd))/(tfd + (OBM25_CONS_K1 *((1-OBM25_CONS_B)+(OBM25_CONS_B*(cur_doc_length/avg_doc_legth)))));
		double scr_part3 = (tfq+(OBM25_CONS_K2 *tfq))/(tfq + OBM25_CONS_K2);
		double scr = scr_part1*scr_part2*scr_part3;
		return scr;
	}

	private static double calcOkapiIDF(double tfd, double dfw,double cur_doc_length) 
	{
		double scr1 = tfd / (tfd + 0.5 + (1.5 * (cur_doc_length /avg_doc_legth)));
		double scr2 = (Math.log(TOT_DOC_COUNT)/Math.log(dfw));
		 return scr1*scr2;
	}

	public static double calcOkapiTF(double tfd, double dfw, double cur_doc_length) 
	{
		double scr= (tfd /(tfd + 0.5 + (1.5 * (cur_doc_length / avg_doc_legth))));
		return scr;
	}
	
	private static int getWordCountInQuery(String query, String term) {
		int count = 0;
		String word = term;
		for(String s: query.split(" "))
			if (s.equalsIgnoreCase(word))
				count++;
		return count;
	}
}