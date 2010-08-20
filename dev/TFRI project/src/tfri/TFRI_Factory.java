package tfri;
import imageTools.AnnotatedImage;
import imageTools.BasicRule;
import imageTools.BasicRuleSet;
import imageTools.FileBank;
import imageTools.ImageBank;
import imageTools.ImageProcessor;
import imageTools.RuleSet;
import imageTools.StorableRuleSet;
import imageTools.blobDetection.BlobProcessor;
import imageTools.blobDetection.BlobRule;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import rbnb.RBNB_ImageBank;


import com.rbnb.sapi.SAPIException;

public class TFRI_Factory {
	
	public static BlobRule BEE_RULE = new BlobRule("Bee","Bee Count", 7,7,60,60,0.16f);
	
	public static RuleSet<BlobRule> generateRuleSet(){
		BasicRuleSet<BlobRule> rules = new StorableRuleSet<BlobRule>( new File("Rules")) {

			@Override
			protected BlobRule handleRuleLoadException(File source, Exception e) {
				e.printStackTrace();
				return null;
			}

			@Override
			protected void handleRuleStoreException(BlobRule rule,
					File destination, Exception e) {
				e.printStackTrace();
			}
			
			@Override
			protected void handleRuleDeleteException(BasicRule rule, File file,
					Exception e) {
				e.printStackTrace();				
			}
			
			@Override
			protected void onLoadHook(BlobRule rule, File source){
				System.out.println("Loading \t" + rule +" -> " + source);
			}
			
			@Override
			protected void onStoreHook(BlobRule rule, File destination){
				System.out.println("Storing \t" + rule +" -> " + destination);
			}
			
			@Override
			protected void onUpdateHook(BlobRule oldRule, BlobRule newRule, File destination){
				System.out.println("Updating\t" + oldRule +" -> " + newRule +" -> "+ destination);
			}

			
		};
		
		rules.add(BEE_RULE);
		return rules;
		
	}
	
	public static ImageProcessor<AnnotatedImage, BlobRule> generateImageProcessor(RuleSet<BlobRule> rules){
		return new BlobProcessor<AnnotatedImage>(rules);
	}
	
//	public static ImageBank<AnnotatedImage> generateImageBank(String caller, String[] args) throws IOException, IllegalArgumentException, SAPIException{
//		if(args.length == 0) {
//			System.out.println("Loading default.jpg ");
//			return new FileBank(new File("default.jpg"));
//		}
//		else if(args.length == 1 && args[0].charAt(0) != '-') {
//			System.out.println("Loading file " + args[1]);
//			return new FileBank(new File(args[0]));
//		}
//		else if(args.length == 1 && args[0].equals("-a")){
//			System.out.println("Loading Web Archive");
//			return new TFRIWebArchive("Bee Archive", "http://srb2.tfri.gov.tw", 
//									  "SP_CAMERA-2", 2008,8,28,5,0, 2009,8,7,18,0);
//		}
//		else if(args.length == 6 && args[0].equals("-a")){
//			int year = Integer.valueOf(args[1]);
//			int month = Integer.valueOf(args[2]);
//			int day = Integer.valueOf(args[3]);
//			int hour = Integer.valueOf(args[4]);
//			int min = Integer.valueOf(args[5]);
//			
//			System.out.println("Loading Web archive at "+year+"/"+month+"/"
//								+day +" "+hour+":"+min);
//			return new TFRIWebArchive("Bee Archive", "http://srb2.tfri.gov.tw", 
//					  "SP_CAMERA-2", year, month, day, hour, min);
//		}
//		else if(args.length == 6 && args[0].equals("-rbnb")){
//			int year = Integer.valueOf(args[1]);
//			int month = Integer.valueOf(args[2]);
//			int day = Integer.valueOf(args[3]);
//			int hour = Integer.valueOf(args[4]);
//			int min = Integer.valueOf(args[5]);
//			
//			System.out.println("Loading Archive from RBNB at "+year+"/"+month+"/"
//								+day +" "+hour+":"+min);
//			return new RBNB_ImageBank("TFRI Archive", year, month, day, hour, min);
//		}
//		else {
//			System.out.println("Available Options:\n---\n" +
//					caller+"\t\t\t: Use Default file\n" +
//					caller+" <file>\t\t: Use Specified Image\n" +
//					caller+" -a\t\t\t: Use TFRI web Archive\n" +
//					caller+" -a yyyy M d h m\t: Use TFRI Archive starting at date" +
//					caller+" -rbnb yyyy M d h m\t: Use RBNB Archive on local source pulling from a source named 'TFRI Archive' starting at date");
//			throw new IllegalArgumentException();
//		}
//	
//	}
//	
	
	@SuppressWarnings("static-access")
	public static Options generateCommonComandlineOptions(){

			OptionGroup imageBankOpts = new OptionGroup();
			Options options = new Options();
			
			imageBankOpts.addOption(  OptionBuilder.withLongOpt("local")
	                			.withDescription("Load from local file or directory" )
	                			.hasArgs(1).withArgName( "file|dir path" )
	                			.create('l')
							
			);
			imageBankOpts.addOption(  OptionBuilder.withLongOpt("archive")
        			.withDescription("Load from a TFRI Web Archive" )
        			.hasArgs(2).withArgName( "name [url]" )
        			.create('a')
				
			);
			imageBankOpts.addOption(  OptionBuilder.withLongOpt("rbnb")
        			.withDescription("Load From RBNB Server" )
        			.hasArgs(2).withArgName( "name server:port" )
        			.create('r')
				
			);
			imageBankOpts.addOption(  OptionBuilder.withLongOpt("rbnbLocal")
        			.withDescription("Load From RBNB Server on \'localhost\'" )
        			.hasArgs(1).withArgName( "name" )
        			.create('R')
				
			);
			
			
			options.addOption(  OptionBuilder.withLongOpt("start")
					.withDescription("Start Date to pull images from " +
							"(does not apply to local files)" )
					.hasArgs(5).withArgName("YYYY MM DD HH mm")
					.create('s')
			);
			options.addOption(  OptionBuilder.withLongOpt("end")
					.withDescription("End Date to stop pulling images " +
							"(does not apply to local files)" )
					.hasArgs(5).withArgName("YYYY MM DD HH mm")
					.create('e')
			);
			options.addOption(  OptionBuilder.withLongOpt("help")
					.withDescription("Print Help Message" )
					.create('h')
			);
			
			options.addOptionGroup(imageBankOpts);
			return options;
		
	}
	
	
	public static ImageBank<AnnotatedImage> generateImageBank(String caller, String[] args) throws IOException, IllegalArgumentException, SAPIException{
		
		try {
	        CommandLine cmd = new GnuParser().parse( generateCommonComandlineOptions(), args );

	        if(cmd.hasOption('h')){
	        	printHelp(caller, "");
	        	System.exit(0);
	        }
	        
	        Date end, start;
	        
	        String[] sVal = cmd.getOptionValues('s');
	        if(sVal != null && sVal.length >=5){
		        int startYear =		Integer.parseInt(sVal[0]);
				int startMonth =	Integer.parseInt(sVal[1]);
				int startDay =		Integer.parseInt(sVal[2]);
				int startHour =		Integer.parseInt(sVal[3]);
				int startMin =		Integer.parseInt(sVal[4]);
				start = new GregorianCalendar(startYear, startMonth-1, startDay, startHour, startMin).getTime();
	        }
	        else start = new GregorianCalendar(2009, 7, 27, 5, 0).getTime();
	        
			String[] eVal = cmd.getOptionValues('s');
			if(eVal != null && eVal.length >=5){
		        int endYear =		Integer.parseInt(eVal[0]);
				int endMonth =		Integer.parseInt(eVal[1]);
				int endDay =		Integer.parseInt(eVal[2]);
				int endHour =		Integer.parseInt(eVal[3]);
				int endMin =		Integer.parseInt(eVal[4]);
				end = new GregorianCalendar(endYear, endMonth, endDay, endHour, endMin).getTime();
			}
			else end = new GregorianCalendar(2009, 8, 7, 18, 0).getTime();

	        if(cmd.hasOption('a')){
	        	System.out.println("Loading Web archive at "+ start);
	        	
	        	String name = cmd.getOptionValues('a')[0];
	        	String url = cmd.getOptionValues('a')[1];
	        	return new TFRIWebArchive(name, url, "SP_CAMERA-2", start, end);	
	        }
	        else if(cmd.hasOption('r')){
	        	String name = cmd.getOptionValues('r')[0];
	        	String path = cmd.getOptionValues('r')[1];
	        	
	        	return new RBNB_ImageBank(name, path, start);	
	        }
	        else if(cmd.hasOption('R')){
	        	String name = cmd.getOptionValue('R');
	        	return new RBNB_ImageBank(name, "localhost", start);	
	        }
	        else if(cmd.hasOption('l')){
	        	String path = cmd.getOptionValue('l');
	        	return new FileBank(new File(path));
	      
	        }else{
	        	return new FileBank(new File("default.jpg"));
	        }		
	    }
	    catch( ParseException e ) {
	    	printHelp(caller, e.getMessage());
	    }
	    catch(NumberFormatException e){
	    	printHelp(caller, "Invalid Number " + e.getMessage());
	    } 
		return null;
	}
	
	/**
	 * Prints usage info
	 */
	private static void printHelp(String caller, String errormsg){
		if(!errormsg.equals("")) System.out.println(errormsg+"\n");
		new HelpFormatter().printHelp(caller, generateCommonComandlineOptions());
	}
}
