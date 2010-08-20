package imageTools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * BasicRuleSet.java (  imageTools )
 * Created: Dec 5, 2009
 * @author Michael Nekrasov
 * 
 * Description: A basic abstract implementation of a RuleSet using a HashMap
 * 
 * @param <R> Type of Rule to store
 */
public class BasicRuleSet<R extends Rule> implements RuleSet<R> {
		
	protected final HashMap<String,R> map;
	
	/**
	 * Constructs an empty RuleSet
	 */
	public BasicRuleSet(){
		map = new HashMap<String,R>();
	}

	@Override
	public boolean add(R rule){
		map.put(rule.getName(), rule);
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends R> rules){
		for(R rule : rules)
			add(rule);
		return true;
	}
	
	@Override
	public R get(String rulename) {
		return map.get(rulename);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsValue(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return map.values().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<R> iterator() {
		return map.values().iterator();
	}

	@Override
	public boolean remove(Object o) {
		if(!( o instanceof Rule))
			throw new IllegalArgumentException("Expected Rule but recieved " +o.getClass().getName());
		
		return map.remove(((Rule)o).getName()) != null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean val = true;
		for(Object o : c)
			val = val && remove(o);
		return val;
			
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Object[] toArray() {
		return map.values().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return map.values().toArray(a);
	}
	
	
}