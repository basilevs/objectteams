package boundtoordinary.teampkg;

/**
 * $Id: Team_3d.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase
 * an internal defined bound role class with a method
 * the method has a throw-clause
 * the base class of the role is an ordinary class
 */
public team class Team_3d
{
	public class SampleRole playedBy boundtoordinary.basepkg.SampleBase
	{
	    public void roleMethod() throws Exception {}
	}
}
