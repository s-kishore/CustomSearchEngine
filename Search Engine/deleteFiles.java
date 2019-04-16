package search.scorer;
import java.io.File;


public class deleteFiles {

	public static void main(String[] args){
		// TODO Auto-generated method stub
		long start = System.currentTimeMillis();

		File fl1 = new File(constants.file_doc_cat_loc);
		File fl2 = new File(constants.file_idx_report_loc);
		File fl3 = new File(constants.file_inv_idx_loc);
		
		fl1.delete();
		fl2.delete();
		fl3.delete();
		System.out.println("DELETE SUCCESSFUL");
//		 Program end process
		long end = System.currentTimeMillis();
		System.out.println("Run time: " + (end - start) / 1000);
	}
}