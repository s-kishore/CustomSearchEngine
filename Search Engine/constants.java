package search.scorer;

public class constants {
	static final String data_source = "D:/IR/AP89_DATA/AP_DATA/ap89_collection/";
	static final String sample_data = "D:/IR/AP89_DATA/AP_DATA/sample";
	static final String stop_list = "D:/IR/AP89_DATA/AP_DATA/stoplist.txt";
	static final String stem_list = "D:/IR/AP89_DATA/AP_DATA/diffs.txt";
	static final String query_location = "D:/IR/AP89_DATA/AP_DATA/query_desc.51-100.short.txt";
	static final String file_query_results = "D:/IR/results/assign 2/QueryResults.txt";
	static final String file_queries = "D:/IR/results/assign 2/Querys.txt";
	static final String file_queries_trmd = "D:/IR/results/assign 2/P_Querys.txt";
	static final String token_pattern = "\\w+(\\.?\\w+)*";
	
	static final long TOT_DOC_COUNT = 84678;
	static final double OBM25_K1 = 1.2;
	static final double OBM25_K2 = 0;
	static final double OBM25_B = 0.75;
	static final double LAMBDA = 0.7;
	static final int NUMBER_OF_RESULTS = 1000;
	static final long VOC_SIZE = 189327;
	
	static final String file_idx_report_loc = "D:/IR/results/assign 2/idx_rpt.txt";
	static final String file_idx_report = "idx_rpt.txt";
	
	static final String idx_cat = "D:/IR/results/assign 2/idx_cat.txt";
	static final String file_doc_cat_loc = "D:/IR/results/assign 2/doc_cat.txt";
	static final String file_doc_cat = "doc_cat.txt";
	
	static final String file_inv_idx_loc = "D:/IR/results/assign 2/inverted_idx.txt";
	static final String file_inv_idx = "inverted_idx.txt";
	
	static final String file_org_doc_loc = "D:/IR/results/assign 2/docs/";
	
	static final String file_temp_fl_loc = "D:/IR/results/assign 2/temp_fl.txt";
	
	static final String file_temp_fl1_loc = "D:/IR/results/assign 2/idx/temp_fl.txt";
	static final String file_temp_fl1 = "temp_fl.txt";
	
	static final String prox_srch_results = "D:/IR/results/assign 2/p_search_results.txt";
	static final String prox_results = "D:/IR/results/assign 2/results/p_results.txt";
	
	
	static final String RESULT_OKAPI_TF ="D:/IR/results/assign 2/results/Okapi_TF.txt";
	static final String RESULT_OKAPI_IDF = "D:/IR/results/assign 2/results/Okapi_IDF.txt";
	static final String RESULT_OKAPI_BM25 = "D:/IR/results/assign 2/results/Okapi_BM25.txt";
	static final String RESULT_LAPLACE ="D:/IR/results/assign 2/results/Laplace.txt";
	static final String RESULT_JM ="D:/IR/results/assign 2/results/JM.txt";
}
