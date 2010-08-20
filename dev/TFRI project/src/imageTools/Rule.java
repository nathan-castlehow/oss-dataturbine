package imageTools;

/**
 * Rule.java (  imageTools )
 * Created: Dec 5, 2009
 * @author Michael Nekrasov
 * 
 * Description: Defines a 
 *
 */
public interface Rule {

	/**
	 * Gets the name of the rule
	 * CAUTION: all rules should have unique name, two rules with the same name
	 * 			will be treated as identical
	 * @return the name of the rule
	 */
	public String getName();

	/**
	 * Gets a Description of the Rule
	 * @return the full description of the rule
	 */
	public String getDescription();
	
	/**
	 * The type of rule stored
	 * (should give a hint about the function of this rule)
	 * @return A short string explaining the type
	 */
	public String getType();

}
