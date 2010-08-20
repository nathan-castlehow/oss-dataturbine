package tfri;
import java.io.IOException;
import java.math.BigInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import rbnb.GenericDataSource;

import com.rbnb.sapi.SAPIException;

public class GenericSoundSource {

	 private static final float		DEFAULT_SAMPLE_TIME 	= 5;		//SECONDS
	 private static final float		DEFAULT_SAMPLE_RATE		= 44100;	//HZ
	 private static final int		DEFAULT_SAMPLE_SIZE		= 16;		//BITS
	 private static final int		DEFAULT_NUM_CHANNELS	= 1;			
	 private static final boolean	DEFAULT_SIGNED			= true;
	 private static final boolean	DEFUALT_BIGENDIAN		= false;
	 
	 
	 public static final AudioFormat BLOCK_FORMAT = new AudioFormat(
			 DEFAULT_SAMPLE_RATE, DEFAULT_SAMPLE_SIZE, 
			 DEFAULT_NUM_CHANNELS, DEFAULT_SIGNED, DEFUALT_BIGENDIAN );
	 
	 public static int BUFFER_SIZE = (int)(DEFAULT_SAMPLE_TIME * DEFAULT_SAMPLE_RATE	* DEFAULT_SAMPLE_SIZE)/8;
	 public static int NUM_SAMPLES = (int)(DEFAULT_SAMPLE_TIME * DEFAULT_SAMPLE_RATE);
     
	 private GenericDataSource src;
	 private TargetDataLine line;
	 private AudioInputStream linearStream;
	 private boolean stop = false;
	 
	 public GenericSoundSource( ) throws IOException, LineUnavailableException, SAPIException, InterruptedException{
		BigInteger i = new BigInteger("3");
		i.intValue();
		
		line = createLine(BLOCK_FORMAT);
		linearStream = new AudioInputStream(line);
		src = new GenericDataSource("AUDIOS");
		src.addChannel("audioX", GenericDataSource.MIME_AUDIO);
		src.addChannel("sample");
		
		System.out.println("Begin Recording! Hit ENTER to stop \n");
		Thread go = new Thread() {
			public void run() {
				for(long i =0; !stop; i++){
					System.out.println("Recording sample "+i+" ... ");
					
					byte[] voiceData = new byte[BUFFER_SIZE];
			 		
			 	 	try {
						linearStream.read(voiceData,0,voiceData.length);
		
						src.put("audioX", voiceData);
						src.put("sample", i);
						src.flush();
			 	 	} catch (Exception e) {e.printStackTrace();}
				}
				closeLine(line); 
				src.close();
				System.out.println("\n RECORDING STOPPED");
			}};
		
		go.start();
		System.in.read();
		stop = true;
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
     
     public static void main(String[] args) throws IOException, LineUnavailableException, SAPIException, InterruptedException{
    	 new GenericSoundSource();
	 }
}