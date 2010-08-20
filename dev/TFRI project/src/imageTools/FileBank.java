package imageTools;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class FileBank implements ImageBank<AnnotatedImage>{

	private final LinkedList<AnnotatedImage> images = new LinkedList<AnnotatedImage>();
	private final String name;
	
	public FileBank(File path) throws IOException{
		LinkedList<File> files = new LinkedList<File>();
		name = path.getName();
		
		if(path.isFile()){
			files.add(path);
		}
		else if(path.isDirectory()){
			for(File file : path.listFiles())
				files.add(file);
		}
		else throw new IOException();
		
		for(File file: files){
			try{
				images.add(new AnnotatedImage(file));
			}catch (Exception e) {}
		}
	}

	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public Iterator<AnnotatedImage> iterator() {
		return images.iterator();
	}
	
	@Override
	public String toString(){
		return getName();
	}

	
}