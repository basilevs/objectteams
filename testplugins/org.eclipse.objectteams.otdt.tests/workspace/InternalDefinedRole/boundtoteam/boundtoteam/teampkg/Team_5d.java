package boundtoteam.teampkg;

/**
 * $Id: Team_5d.java 5955 2005-06-21 16:04:55Z haebor $
 * 
 * testcase:
 * a bound role class with a method and a method mapping
 * the method is concrete, has no parameters but a callin modifier
 * the method mapping is a replace-callin mapping
 *  the base class of the role is a team
 */
public team class Team_5d
{
	public class SampleRole playedBy TeamC
	{
	    public callin void roleMethod() {}
	    roleMethod <- replace baseMethod;    
	}
}
