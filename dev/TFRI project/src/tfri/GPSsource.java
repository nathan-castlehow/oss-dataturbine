package tfri;
import rbnb.GenericDataSource;

import com.rbnb.sapi.SAPIException;


public class GPSsource {

	public static void main(String[] args) throws SAPIException, InterruptedException {
		
		//GenericDataSource src = new GenericDataSource("gpsData", "75.25.160.76", 3333, 1000, 1000);
		GenericDataSource src = new GenericDataSource("gpsData", "localhost", 3333, 1000, 1000);
		src.addChannel("gps", "application/x-gps");
		src.addChannel("gps2", "application/x-gps");

		
		for(double i=-.1; i<.1; i += 0.0001 ){
			double[] crd = new double[]{8.157858+i,-78.846916+i};
			double[] crd2 = new double[]{8.157858-i,-78.846916-i};
			
			src.put("gps", crd);
			src.put("gps2", crd2);
			src.flush();
			System.out.println("ADDED: " +crd[0]+" , "+crd[1] + "\t| "+crd2[0]+" , "+crd2[1]);
			Thread.sleep(10);
		}
		
//		for(int i=0; i<100; i++){
//			double[] crd = new double[]{r.nextDouble()*100,r.nextDouble()*100};
//			src.put("gps2", crd);
//			src.flush();
//			System.out.println("ADDED: " +crd[0]+" , "+crd[1]);
//			Thread.currentThread().sleep(100);
//		}
//		
//		for(int i=0; i<100; i++){
//			double[] crd = new double[]{r.nextDouble()*100,r.nextDouble()*100};
//			src.put("gps3", crd);
//			src.flush();
//			System.out.println("ADDED: " +crd[0]+" , "+crd[1]);
//			Thread.currentThread().sleep(100);
//		}
		
		src.close();
	}
}
