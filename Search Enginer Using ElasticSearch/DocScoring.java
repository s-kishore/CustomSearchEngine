package search.scorer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.termvector.TermVectorRequestBuilder;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;


public class DocScoring {
	static long corpusLength;
	static List<String> all_docs = new ArrayList<String>();
	static Map<String, Integer> doc_length = new HashMap<String,Integer>();
	static Node node = NodeBuilder.nodeBuilder().clusterName("elasticsearch").node();
	static Client client = node.client();
	
	public static void main(String[] args) {
		retrieveAllDocIds();
		doc_length = getDocumentLength(all_docs);
		writeDocStats();
		node.stop();
		client.close();
		node.close();
	}
	
	private static void writeDocStats() {
		try 
		{
			PrintWriter wrtr = new PrintWriter("docStats.txt");
			Set<String> docids = doc_length.keySet();
			wrtr.println("Total: " + corpusLength);
			for(String docid: docids)
			{
				wrtr.println(docid + " " + doc_length.get(docid));
			}
			wrtr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void retrieveAllDocIds() 
	{
		FileReader doclist;
		String temp;
		String[] temp1;
		try 
		{
			doclist = new FileReader("D:\\IR\\AP89_DATA\\AP_DATA\\doclist.txt");
			BufferedReader rdr = new BufferedReader(doclist);
			rdr.readLine();
			while((temp = rdr.readLine())!= null)
			{
				 temp1 = temp.split(" ",2);
				 all_docs.add(temp1[1]);
			}
			rdr.close();
			doclist.close();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	public static Map<String, Integer> getDocumentLength(List<String> doc_list) {
		Map<String, Integer> documentLengthList = new HashMap<String, Integer>();
		int doc_count = doc_list.size();
		
		for (int idx = 0; idx < doc_count; idx++) {
			String docNo = doc_list.get(idx);
			TermVectorRequestBuilder qb = new TermVectorRequestBuilder(client,"ap_dataset","document",docNo)
											.setFieldStatistics(true)
											.setOffsets(false).setTermStatistics(true)
											.setPayloads(true).setPositions(true);
			TermVectorResponse termVectorResponse = qb.execute().actionGet();
			int documentLength = 0;
			try {
				Fields fields = termVectorResponse.getFields();
				Terms terms = fields.terms("text");
				if (terms == null) 
				{} 
				else 
				{
					boolean isFound = false;
					TermsEnum iterator = terms.iterator(null);
					for (int i = 0; i < fields.getUniqueTermCount(); i++) 
					{
						BytesRef next = iterator.next();
						DocsEnum docsEnum = iterator.docs(null, null);
						int termFrequency = docsEnum.freq();
						documentLength += termFrequency;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			documentLengthList.put(docNo, documentLength);
			System.out.println(docNo + " : " + documentLength);
			corpusLength += documentLength;
		}
		System.out.println("CORPUS LENGTH " + corpusLength);
		return documentLengthList;
	}

}
