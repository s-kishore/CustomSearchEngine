package search.scorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class mySearch_Indexer {
	String text;
	String text_clean;
	String doc_id_text;
	String[] tokens;
	int doc_no;
	Long tot_words_idx;
	int wcnt =0;
	int doc_length= 0;
	Pattern pattern = Pattern.compile(constants.token_pattern);
	Matcher ptrn_matcher;
	static Set<String> stopwords = new HashSet<String>();
	static Map<String, String> stemTbl = new HashMap<String,String>();
	Set<String> term_loc_wdup = new TreeSet<String>();
	Map<String,String> doc_idx = new HashMap<String,String>();
	Set<String> doc_terms = new TreeSet<String>();
	
	mySearch_Indexer()
	{
		createStopStemList();
		retrieveIndexReport();
	}

	public void indexDocument(String doc_txt, String doc_id) 
	{	
		text = doc_txt;
		this.doc_id_text = doc_id;
		doc_no ++;
		
		if(text.length() > 1)
		{
			tokenise();
			consolidateTokens();
			populateInvertedIndex();
		}
		updateDocInCatalog();
		initialiseVar();
	}

	private void initialiseVar() {
		doc_idx.clear();
		text ="";
		text_clean = "";
		doc_id_text="";
		tokens = null;
		wcnt =0;
		doc_length = 0;
		term_loc_wdup.clear();
	}

	public void populateInvertedIndex() {
		try{
			Boolean word_written = false, idx_line_written = true,EOF = false;
			int comp,line=0;
			//String flname = doc_id_text.substring(0,8)+".txt";
			//String flpath = "D:/IR/results/assign 2/idx/"+flname;
			//File fl = new File(flpath);
			File fl2 = new File("D:/IR/results/assign 2/idx_cat.txt");
			File fl = new File(constants.file_inv_idx_loc); 
			
			File fltmp = new File(constants.file_temp_fl1_loc);
			FileWriter flwrtr = new FileWriter(fltmp);
			FileWriter flwrtr2 = new FileWriter(fl2);
			
			doc_terms.addAll(doc_idx.keySet());
			String tmp="",word="";
			String[] idx_line;
			Iterator<String> it = doc_terms.iterator();
			if(fl.exists())
			{
				FileReader flrdr = new FileReader(fl);
				BufferedReader rdr = new BufferedReader(flrdr);
				
				while(it.hasNext() && !EOF)
				{
					word = it.next();
					word_written = false;
					
						do
						{
							if(idx_line_written)
							{
								tmp = rdr.readLine();
							}
							if(tmp != null)
							{
								idx_line_written = false;
							
								idx_line = tmp.split(":");
								comp = idx_line[0].compareToIgnoreCase(word);
								if(comp > 0)
								{
									flwrtr.write(word +":"+doc_no+"#"+doc_idx.get(word)+System.lineSeparator());
									flwrtr2.write(word+" " + line + System.lineSeparator());
									line++;
									word_written = true;
								}
								else if(comp < 0)
								{
									flwrtr.write(tmp+System.lineSeparator());
									flwrtr2.write(word+" " + line + System.lineSeparator());
									line++;
									idx_line_written = true;
								}
								else if(comp == 0)
								{
									flwrtr.write(idx_line[0]+":"+doc_no+"#"+doc_idx.get(idx_line[0])+";"+idx_line[1]+System.lineSeparator());
									idx_line_written = true;
									word_written = true;
									flwrtr2.write(word+" " + line + System.lineSeparator());
									line++;
								}
							}
							else 
								EOF = true;
							
						}while(!word_written && !EOF);

				}
				if(!EOF)
				{
					if(idx_line_written == false)
					{
						flwrtr.write(tmp+System.lineSeparator());
						String[] spl = tmp.split(":");
						flwrtr2.write(spl[1]+" " + line + System.lineSeparator());
						line++;
					}
					while((tmp = rdr.readLine()) != null)
					{
						flwrtr.write(tmp+System.lineSeparator());
						String[] spl = tmp.split(":");
						flwrtr2.write(spl[1]+" " + line + System.lineSeparator());
						line++;
					}
				}
				if(it.hasNext() || (word_written == false))
				{
					if(word_written == false)
					{
						flwrtr.write(word +":"+doc_no+"#"+doc_idx.get(word)+System.lineSeparator());
						flwrtr2.write(word+" " + line + System.lineSeparator());
						line++;
					}
					
					while(it.hasNext())
					{
						word = it.next();
						flwrtr.write(word +":"+doc_no+"#"+doc_idx.get(word)+System.lineSeparator());
						flwrtr2.write(word+" " + line + System.lineSeparator());
						line++;
					}
				}
				rdr.close();
				flrdr.close();
			}
			else
			{
				for(String tmp1: doc_terms)
				{
					flwrtr.write(tmp1 +":"+doc_no+"#"+doc_idx.get(tmp1)+System.lineSeparator());
					flwrtr2.write(tmp1+" " + line + System.lineSeparator());
					line++;
				}
			}
			flwrtr.close();
			flwrtr2.close();
			fl.delete();
			fltmp.renameTo(fl);
			doc_terms.clear();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void tokenise() 
	{
		String term,tmp,tmp1;
		int tot_term_count=0;
		ptrn_matcher = pattern.matcher(text.toLowerCase());
		while(ptrn_matcher.find())
		{
			for(int i=0; i< ptrn_matcher.groupCount(); i++)
			{
				term = ptrn_matcher.group(i);
				if(term.trim() != "")
				{	
					doc_length ++;
					if(term.startsWith("_"))
						term = term.substring(1);
					// AS - IS
	//				term_loc_wdup.add(term + "#"+tot_term_count);
					
					// STOP WORDS REMOVE
					/*if(!stopwords.contains(term))
					{
						term_loc_wdup.add(term + "#"+tot_term_count);
					}*/
					
					// STEM WORDS
					/*tmp = stemTbl.get(term);
					tmp1 = (tmp != null)? tmp : term;
					term_loc_wdup.add(tmp1 + "#"+tot_term_count);*/
					
					
					//STOP AND STEM
					if(!stopwords.contains(term))
					{
						tmp = stemTbl.get(term);
						tmp1 = (tmp != null)? tmp : term;
						term_loc_wdup.add(tmp1+ "#"+tot_term_count);
					}
					
					tot_term_count ++;
					if(doc_length == 999)
					{
						consolidateTokens();
						populateInvertedIndex();
					}
				}
			}
		}
	}

	private void consolidateTokens() 
	{
		try {
			String curr ="",loc,tmp;
			String[] split;
			int term_count= 0;
			Iterator<String> it = term_loc_wdup.iterator();
			tmp = it.next();
			split = tmp.split("#");
			curr = split[0];
			loc = (split[1]);
			term_count ++;
			while(it.hasNext())
			{
				tmp = it.next();
				split = tmp.split("#");
				if(curr.equals(split[0]))
				 {
					 loc = loc +","+ split[1];
					 term_count++;
				 }
				 else
				 {
					 doc_idx.put(curr, loc);
					 tot_words_idx =tot_words_idx + term_count;
					 term_count = 1;
					 curr = split[0];
					 loc = split[1];
				 }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateDocInCatalog() 
	{
		addToCatalog();
		saveDocumentData();
	}

	private void addToCatalog() 
	{
		try 
		{
			File catfl = new File(constants.file_doc_cat_loc);
			FileWriter flwrtr = new FileWriter(catfl,true);
			flwrtr.write(doc_id_text+":"+doc_no+":"+doc_length+System.lineSeparator());
			flwrtr.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void saveDocumentData() {
		// TODO Auto-generated method stub
		try 
		{
			FileWriter flwrtr = new FileWriter(constants.file_org_doc_loc+doc_no+".txt");
			flwrtr.write(text);
			flwrtr.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void createStopStemList() {
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
				doc_no = Integer.parseInt(words[1]);
				tot_words_idx = Long.parseLong(words[3]);
				flrdr.close();
				rdr.close();
			}
			else
			{
				doc_no = 0;
				tot_words_idx = 0l;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	protected void finalize()
	{
		reWriteIdxReport();
	}

	private void reWriteIdxReport() {
		try 
		{	
			FileWriter wrtr = new FileWriter(constants.file_idx_report_loc);
			wrtr.write("<TOT DOCS> :"+ doc_no + ":"+"<TOT TERM COUNT> :" + tot_words_idx);
			wrtr.close();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}