package search.scorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class idx_merger {
	static Map<String,String> Index = new HashMap<String,String>();
	static Map<String,String> Pindex;
	static Set<String> terms = new TreeSet<String>();
	static File fl = new File(constants.file_inv_idx_loc);
	
	public static void main(String args[])
	{
		long start = System.currentTimeMillis();
//		createSingleIndex();
		SecondFknMethod();
		long end = System.currentTimeMillis();
		System.out.println("TIME : " + (end-start)/1000);
	}

	private static void SecondFknMethod() {
		readMainIndex();
		File sfol = new File("D:/IR/results/assign 2/idx/");
		File[] flnames = sfol.listFiles();
		String tmp;
		String[] tsplit;
		try
		{
			for(File f: flnames)
			{
				System.out.println(f.getName());
				Pindex = new HashMap<String,String>();
				FileReader flrdr = new FileReader(f);
				BufferedReader rdr = new BufferedReader(flrdr);
				while((tmp = rdr.readLine()) != null)
				{
					tsplit = tmp.split(":");
					Pindex.put(tsplit[0],tsplit[1]);
				}
				addMainIndex();
				rdr.close();
			}
			writeIndex();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void writeIndex() {
		try
		{
			File fl2 = new File("D:/IR/results/assign 2/idx_cat.txt");
			FileWriter flwrtr = new FileWriter(fl2);
			FileWriter fwrtr = new FileWriter(fl);
			int line=0;
			terms.addAll(Index.keySet());
			String word,tmp;
			Iterator<String> it = terms.iterator();
			while(it.hasNext())
			{
				word = it.next();
				tmp = word+":"+Index.get(word)+System.lineSeparator();
				fwrtr.write(tmp);
				//byteln = byteln + tmp.getBytes().length;
				flwrtr.write(word+" " + line + System.lineSeparator());
				line++;
			}
			flwrtr.close();
			fwrtr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	private static void readMainIndex() 
	{
		String tmp;
		String[] tsplit;
		try{
			if(fl.exists())
			{
				FileReader flrdr = new FileReader(fl);
				BufferedReader rdr = new BufferedReader(flrdr);
				
				while((tmp = rdr.readLine()) != null)
				{
					tsplit = tmp.split(":");
					Index.put(tsplit[0],tsplit[1]);
				}
				rdr.close();
				flrdr.close();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void addMainIndex() {
		
		Set<String> words = Pindex.keySet();
		String value, newval;
		for(String s: words)
		{
			newval = Pindex.get(s);
			if(Index.containsKey(s))
			{
				value = Index.get(s);
				Index.replace(s, newval+ ";" +value);
			}
			else
				Index.put(s, newval);
		}
	}

	/*private static void createSingleIndex() {
		// TODO Auto-generated method stub
		File index = new File(constants.file_inv_idx_loc);
		File tmpfl = new File(constants.file_temp_fl1_loc);
		File sfol = new File("D:/IR/results/assign 2/idx/");
		File[] flnames = sfol.listFiles();
		boolean EOF_PIDX= false, EOF_IDX = false;
		String tmp, mtmp, ptmp;
		String[] psplit,msplit;
		try
		{
			for(File f: flnames)
			{
				FileWriter tmpflwrtr = new FileWriter(tmpfl);
//				System.out.println(f.getName());
				FileReader mflrdr = new FileReader(index);
				BufferedReader mrdr = new BufferedReader(mflrdr);
				FileReader pflrdr = new FileReader(f);
				BufferedReader prdr = new BufferedReader(pflrdr);
				
				if(index.exists())
				{
					FileReader idxflrdr = new FileReader(index);
					BufferedReader idxrdr = new BufferedReader(idxflrdr);
					
					while(!EOF_IDX && !EOF_PIDX)
					{
						if((ptmp = prdr.readLine()) != null)
						{
							psplit = ptmp.split(":");
							do
							{
								mtmp = mrdr.readLine();
								if(mtmp != null)
								{
									msplit = mtmp.split(":");
									
								}
								else
									EOF_IDX = true;
							}while(!EOF_IDX);
						}
						else
						{
							EOF_PIDX = true;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}*/
}
