package tfri;
import java.io.*;

import javax.sound.sampled.*;
import com.rbnb.sapi.SAPIException;
import rbnb.GenericDataSource;

public class GenericSoundStreamSource {

	private static final float		DEFAULT_SAMPLE_TIME 	= 30;		//SECONDS
	 private static final float		DEFAULT_SAMPLE_RATE		= 88000;		//HZ
	 private static final int		DEFAULT_SAMPLE_SIZE		= 16;		//BITS
	 private static final int		DEFAULT_NUM_CHANNELS	= 1;			
	 private static final boolean	DEFAULT_SIGNED			= true;
	 private static final boolean	DEFUALT_BIGENDIAN		= false;
	 
	 
	 public static final AudioFormat BLOCK_FORMAT = new AudioFormat(
			 DEFAULT_SAMPLE_RATE, DEFAULT_SAMPLE_SIZE, 
			 DEFAULT_NUM_CHANNELS, DEFAULT_SIGNED, DEFUALT_BIGENDIAN );
	 
	 public static final int BUFFER_SIZE = (int)(DEFAULT_SAMPLE_RATE* DEFAULT_SAMPLE_SIZE)/8;
     
	 private GenericDataSource src;
	 private TargetDataLine line;
	 private AudioInputStream linearStream;
	 
	 public GenericSoundStreamSource( ) throws IOException, LineUnavailableException, SAPIException{
		
		line = createLine(BLOCK_FORMAT);
		linearStream = new AudioInputStream(line);
		src = new GenericDataSource("AUDIO");
		src.addChannel("audio");
		
		System.out.println("Recording sample... ");
		for(int x=0; x< DEFAULT_SAMPLE_TIME*40; x++){
			long i;
			for(i =0; i< (DEFAULT_SAMPLE_RATE/40 ); i++){
				
				byte[] soundData = new byte[2];
		 	 	linearStream.read(soundData);
		 	 	
		 	 	//Integer intr = 
		 	 	src.put("audio", makeInt(soundData));
		 	 	
			} 
			src.flush();
			//System.out.println("Recorded " + i + " samples");
		}
		closeLine(line); 
		src.close();
	}
     

	 public static final int makeInt(byte[] b) 
	 {
	     short l = 0;
	     l |= b[0] & 0xFF;
	     l <<= 8;
	     l |= b[1] & 0xFF;
	     return l;
	 }
	 
     private void writeToFile(String filename, byte[] voiceData ) throws IOException{
    	//Write to file
         File audioFile = new File(filename);
         ByteArrayInputStream baiStream = new ByteArrayInputStream(voiceData);
         AudioInputStream aiStream = new AudioInputStream(baiStream,BLOCK_FORMAT,voiceData.length);
         AudioSystem.write(aiStream,AudioFileFormat.Type.AU,audioFile);
         aiStream.close();
         baiStream.close();
     }
     
     private TargetDataLine createLine(AudioFormat format) throws LineUnavailableException{
    	 DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
         TargetDataLine targetDataLine = (TargetDataLine)AudioSystem.getLine(info);
         
         targetDataLine.open(format);
         targetDataLine.start();
         return targetDataLine;
     }
     
     private void closeLine(TargetDataLine targetDataLine){
    	 targetDataLine.stop();
         targetDataLine.close();
     }
     
     public static void main(String[] args) throws IOException, LineUnavailableException, SAPIException{
    	 new GenericSoundStreamSource();
	 }
}