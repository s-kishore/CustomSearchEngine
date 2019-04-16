package search.scorer;

public class esConstants 
{
	static final String clustername = "elasticsearch";
	static final long TOT_DOC_COUNT = 84678;
	static final double OBM25_K1 = 1.2;
	static final double OBM25_K2 = 0;
	static final double OBM25_B = 0.75;
	static final double LAMBDA = 0.7;
	static final int NUMBER_OF_RESULTS = 1000;
	static final long VOC_SIZE = 189327;
	
	static final String QUERY_FILE = "Querys.txt";	
	static final String DOC_STATS = "docStats.txt";
	static final String SEARCH_RESULTS = "searchResults.txt";
	
	static final String QueryLocation = "D:/IR/AP89_DATA/AP_DATA/query_desc.51-100.short.txt";
	static final String StopListLocation = "D:/IR/AP89_DATA/AP_DATA/stoplist.txt";
	static final String DiffLocation = "D:/IR/AP89_DATA/AP_DATA/diffs.txt";
	static final String RESULT_OKAPI_TF ="D:/IR/results/assign 1/Okapi_TF.txt";
	static final String RESULT_OKAPI_IDF = "D:/IR/results/assign 1/Okapi_IDF.txt";
	static final String RESULT_OKAPI_BM25 = "D:/IR/results/assign 1/Okapi_BM25.txt";
	static final String RESULT_LAPLACE ="D:/IR/results/assign 1/Laplace.txt";
	static final String RESULT_JM ="D:/IR/results/assign 1/JM.txt";

}
