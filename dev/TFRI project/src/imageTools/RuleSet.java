package imageTools;

import java.util.Set;

/**
 * RuleSet.java (  imageTools )
 * Created: Dec 5, 2009
 * @author Michael Nekrasov
 * 
 * Description: Interface that describes what constitutes a set of rules.
 * 				An extension of Java's Set.
 * 				CAUTION: Classes implementing RuleSet should not be able to
 *						 add multiple rules with identical names
 * 
 * @param <R> Type of rule to store
 * @see Set
 */
public interface RuleSet<R extends Rule> extends Set<R>{

	/**
	 * Gets a Rule from the set
	 * @param rulename name of rule to retrieve
	 * @return the rule or null if no such rule was found
	 */
	public R get(String rulename);
	
}
