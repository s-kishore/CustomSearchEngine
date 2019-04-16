package search.scorer;
/**
 * @author KishoreKumar
 *
 */
public class query {
	public int qno;
	public String full_query;
	public String short_query;
	public String stemed_query;
	
	public query(int qno,String full_query, String short_query, String stemed_query)
	{
		this.qno = qno;
		this.full_query = full_query;
		this.short_query = short_query;
		this.stemed_query = stemed_query;
	}
	public query(int qno,String full_query, String short_query)
	{
		this.qno = qno;
		this.full_query = full_query;
		this.short_query = short_query;
	}
	public query(int qno, String short_query)
	{
		this.qno = qno;
		this.short_query = short_query;
	}
}
