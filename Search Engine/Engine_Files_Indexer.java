package search.scorer;
import java.io.File;
import java.util.Scanner;

public class Engine_Files_Indexer {

	static String file_name;
	static Scanner scnr, scnr1;
	static String doc_id = new String();
	static String doc_txt = new String();
	static boolean begin_text_record = false;
	static final String doc_end_tag = "</DOC>";
	static final String doc_no_tag = "<DOCNO>";
	static final String text_start_tag = "<TEXT>";
	static final String text_end_tag = "</TEXT>";
	static mySearch_Indexer idxobj = new mySearch_Indexer();
	static int count= 0;
	public static void main(String[] args) {
		
		long start = System.currentTimeMillis();
		
		indexFilesFromDisk();

		long end = System.currentTimeMillis();
		System.out.println("Run Time: " + (end - start) / 1000);
		System.out.println("count: " + count);
	}

	private static void indexFilesFromDisk() {
		File path = new File(constants.data_source);
//		File path = new File(constants.sample_data);
		File[] filelist = path.listFiles();
		File newfile;

		for (int i = 0; i < filelist.length; i++) {
			newfile = new File(filelist[i].toURI());
			System.out.println(newfile);
			readfile(newfile);
		}
	}

	private static void readfile(File newfile) {
		try {
			scnr = new Scanner(newfile);
			file_name = newfile.getName();

			while (scnr.hasNextLine()) {
				scnr1 = new Scanner(scnr.nextLine());
				while (scnr1.hasNext()) {
					String rtrvdword = scnr1.next();
					if(rtrvdword.contains(doc_no_tag))
						doc_id = scnr1.next();
					else if (rtrvdword.contains(text_start_tag))
							begin_text_record = true;
					else if(rtrvdword.contains(text_end_tag))
						begin_text_record = false;
					else if(rtrvdword.contains(doc_end_tag))
					{
						count++;
						indexDocuments(doc_txt, doc_id);
						doc_txt = "";
					}
					else
					{
						if (begin_text_record) 
						{
							doc_txt = doc_txt.concat(rtrvdword + " ");
						}
					}
				}
			}
			scnr1.close();
			scnr.close();
			idxobj.finalize();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private static void indexDocuments(String doc_txt,String doc_id) 
	{
		idxobj.indexDocument(doc_txt,doc_id);
	}
}