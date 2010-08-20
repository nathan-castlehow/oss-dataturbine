package imageTools;

import java.io.Serializable;

/**
 * BasicRule.java (  imageTools )
 * Created: Dec 5, 2009
 * @author Michael Nekrasov
 * 
 * Description: Basic skeleton of a rule.
 *
 */
public abstract class BasicRule implements Rule, Serializable{
	
	private static final long serialVersionUID = 6508702887209888228L;
	
	private final String name, description;
	
	/**
	 * Constructs a rule
	 * @param ruleName unique name or the rule
	 * @param description lengthy description of rule
	 */
	public BasicRule(String ruleName, String description){
		this.name = ruleName;
		this.description = description;
	}			
	
	@Override
	public final String getName(){
		return name;
	}
	
	@Override
	public final String getDescription(){
		return description;
	}
	
	
	@Override
	public String toString(){ 
		return getName()+"("+getType()+")";
	}

	@Override
	public final int hashCode(){
		return getName().hashCode();
	}
	
	/**
	 * Returns the Type as the class name of the rule
	 */
	@Override
	public final String getType(){
		return this.getClass().getSimpleName();
	}

}