package search.scorer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class IndexerEngine {

	static String file_name;
	static Scanner scnr, scnr1;
	static String doc_id = new String();
	static String doc_txt = new String();
	static boolean begin_text_record = false;
	static final String doc_end_tag = "</DOC>";
	static final String doc_no_tag = "<DOCNO>";
	static final String text_start_tag = "<TEXT>";
	static final String text_end_tag = "</TEXT>";
	static Node node = NodeBuilder.nodeBuilder().clusterName("elasticsearch").node();
	static Client client = node.client();
	static int count= 0;
	public static void main(String[] args) {
		
		long start = System.currentTimeMillis();
//		client.admin().cluster().prepareHealth().setWaitForActiveShards(1).execute();
//		client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute();
		
		indexFilesFromDisk();
		client.close();
		node.close();
		long end = System.currentTimeMillis();
		System.out.println("Run Time: " + (end - start) / 1000);
		System.out.println("count: " + count);
	}

	private static void indexFilesFromDisk() {
		File path = new File("D:/AP89_DATA/AP_DATA/ap89_collection/");
		File[] filelist = path.listFiles();
		File newfile;

		for (int i = 0; i < filelist.length; i++) {
			newfile = new File(filelist[i].toURI());
//			System.out.println(newfile);
			readfile(newfile);
		}
	}

	private static void readfile(File newfile) {
		try {
		//	BulkRequestBuilder idx_request = client.prepareBulk();
			scnr = new Scanner(newfile);
			file_name = newfile.getName();

			while (scnr.hasNextLine()) {
				scnr1 = new Scanner(scnr.nextLine());
				while (scnr1.hasNext()) {
					String rtrvdword = scnr1.next();
					switch (rtrvdword) {
					case doc_no_tag:
						doc_id = scnr1.next();
						break;

					case text_start_tag:
						begin_text_record = true;
						break;

					case text_end_tag:
						begin_text_record = false;
						break;

					case doc_end_tag:
//						idx_request.add(client.prepareIndex("ap_dataset","document").setSource(GenerateJson(doc_txt, doc_id)));
						count++;
						indexDocuments(GenerateJson(doc_txt, doc_id),doc_id);
						doc_txt = "";
						break;

					default:
						if (begin_text_record) {
							doc_txt = doc_txt.concat(rtrvdword + " ");
						}
						break;
					}
				}
			}
			/*
			 * BulkResponse bulkResponse = idx_request.execute(); 
			 * if(bulkResponse.hasFailures()) {
			 * System.out.println(bulkResponse.buildFailureMessage()); }
			 */
			scnr1.close();
			scnr.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private static Map<String, Object> GenerateJson(String doc_txt,String doc_id) 
	{
		Map<String, Object> doc_json = new HashMap<String, Object>();
		doc_json.put("docno", doc_id);
		doc_json.put("text", doc_txt);
		return doc_json;
	}

	private static void indexDocuments(Map<String, Object> input,String doc_id) {
		
		IndexResponse response = client.prepareIndex("ap_dataset", "document",doc_id)
				.setSource(input).execute().actionGet();
		if(!response.isCreated())
		{
			System.out.println("error: " + input);
			System.out.println(response.getId());
		}
	}
}