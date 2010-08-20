package imageTools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import sun.misc.Sort;

public abstract class StorableRuleSet<R extends BasicRule> extends BasicRuleSet<R> {
	
	private static final String FILE_EXTENSION = ".rule";
	
	private final File archiveDir;
	
	public StorableRuleSet(File archiveDir){
		this.archiveDir = archiveDir;
		
		load();
		archiveDir.mkdirs(); //make dir if not already exist
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));
	}
	
	@Override
	public boolean add(R rule){
		storeRule(rule);
		return super.add(rule);
	}
	
	
	@Override
	public boolean remove(Object o) {
		if(!( o instanceof BasicRule))
			throw new IllegalArgumentException("Expected BasicRule but recieved " +o.getClass().getName());
		
		DeleteRule((BasicRule)o);
		return super.remove(o);
	}

	@SuppressWarnings("unchecked")
	private R loadRule(File ruleFile){		
		try {
		      ObjectInputStream is = new ObjectInputStream (
		    		  					new BufferedInputStream( 
		    		  					new FileInputStream( ruleFile ) )
		    		  				);
		      R rule = (R)is.readObject();
		      is.close();
		      onLoadHook(rule, ruleFile);
		      return rule;
		} catch (Exception e) {
			return handleRuleLoadException(ruleFile, e);
		}
	}
	
	private void storeRule(R rule){		
		File destination = new File(archiveDir.getPath()+ "/" 
									 +rule.getName() + FILE_EXTENSION);
		try {
			
			
			ObjectOutputStream os = 
				new ObjectOutputStream(
				new BufferedOutputStream(
				new FileOutputStream(
						destination
				)));
			
			os.writeObject(rule);
			os.close();
			
			if(map.containsKey(rule.getName()))
				onUpdateHook(map.get(rule.getName()), rule, destination);
			else
				onStoreHook(rule, destination);
			
		} catch (Exception e) {
			handleRuleStoreException(rule, archiveDir, e);
		}
	}
	
	private void DeleteRule(BasicRule rule){
		File file = (new File(archiveDir.getPath()+ "/" +rule.getName() + FILE_EXTENSION));
		if(map.containsKey(rule.getName())){
			try{
				file.delete();
				onDeleteHook(rule, file);
			}catch(Exception e){
				handleRuleDeleteException(rule, file, e);
			}
		}
	}
	
	private final void load(){	
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(FILE_EXTENSION);
			}
		};
		
		if(!archiveDir.isDirectory() )
			archiveDir.mkdir();
		
		for(File ruleFile: archiveDir.listFiles(filter)){
			R rule = loadRule(ruleFile);
			if( rule != null)
				map.put(rule.getName(), rule);
		}		
	}
	
	private final class ShutdownHook extends Thread{	
		private StorableRuleSet<R> rules;
		
		private ShutdownHook(StorableRuleSet<R> rules){
			this.rules = rules;
		}
		
		public void run() {
			for(R rule: rules) rules.storeRule(rule);
		}
	}
	
	protected void onLoadHook(R rule, File source){}
	protected void onStoreHook(R rule, File destination){}
	protected void onUpdateHook(R oldRule, R newRule, File destination){
		onStoreHook(newRule,destination);
	}
	protected void onDeleteHook(BasicRule rule, File file){}
	
	protected abstract void handleRuleStoreException(R rule, File destination, Exception e);
	protected abstract void handleRuleDeleteException(BasicRule rule, File file, Exception e);

	//should return null should this fail
	protected abstract R handleRuleLoadException(File source, Exception e);
	
		
}