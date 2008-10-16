// UDPproxy - receives UDP packets, writes them to a RBNB
// MJM 10/2007, adapted to output floating point data type
// MJM 2/19/2008 - generalized, e.g. datatype field in header, re-arm logic
// MJM 2/25/2008 - increased UDP socket receive buffer size - big improvement re dropped packets


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.RBNBProcess;

public class UDPproxyD {
	DatagramSocket ds=null; 				// listen for data here
	Source src=null; 						// write data here
	String serverAddress="localhost:3333";
	String Sname=new String("Test");
	int cache=1;							// default to get one fileset (was 60, try 1)
	String mode=new String("create");
	int archive=3600;						// default open-ended archive
	int ssNum=4444;
	boolean Debug=false, someData=false;
	double baseTime = 0.;					// base time from which relative times increment
	double lastTime = 0., lastiTime=0.;		// autoRestart previous time for comparison
	double autoRestart = 0.;				// default no autorestart
	double currentTime = 0.;
	int testNumber = 0;
	double tskootch = 0.;					// image time-skootch

	public static void main(String[] arg) {
		new UDPproxyD(arg).start();
	}
	
	public UDPproxyD(String[] arg) {
		try {
		    System.err.println("");
			ArgHandler ah=new ArgHandler(arg);
			if (ah.checkFlag('h')) throw(new Exception("Help Arguments"));
			if (ah.checkFlag('a')) {
				String serverAddressL=ah.getOption('a');
				if (serverAddress!=null) serverAddress=serverAddressL;
			}
			if (ah.checkFlag('d')) Debug = true;
			if (ah.checkFlag('r'))
			{
				String naf = ah.getOption('r');
				if (naf != null) autoRestart = Double.parseDouble(naf);
			}
			if (ah.checkFlag('t'))
			{
				String naf = ah.getOption('t');
				if (naf != null) tskootch = Double.parseDouble(naf);
			}
			if (ah.checkFlag('c')) {
				String naf=ah.getOption('c');
				if (naf!=null) cache=Integer.parseInt(naf);
			}
			if (ah.checkFlag('k')) {
				String naf=ah.getOption('k');
				if (naf!=null) archive=Integer.parseInt(naf);
				if (archive>0) {
					mode=new String("append");  // was "create"
					if (archive<cache) cache=archive;
				}
			}
			if (ah.checkFlag('K')) {
				String naf=ah.getOption('K');
				if (naf!=null) archive=Integer.parseInt(naf);
				if (archive>0) {
					mode=new String("create");  
					if (archive<cache) archive=cache;
				}
			}
			if (ah.checkFlag('n')) {
				String nameL=ah.getOption('n');
				if (nameL!=null) Sname=nameL;
			}
			if (ah.checkFlag('s')) {
				String nss=ah.getOption('s');
				if (nss!=null) ssNum=Integer.parseInt(nss);
			}
		} catch (Exception e) {
			System.err.println("UDPproxy usage:");
			System.err.println(" -h                     : print this usage info");
			System.err.println(" -d                     : debug mode");
			System.err.println(" -a <server address>    : address of RBNB server to write packets to");
			System.err.println("                default : "+serverAddress);
			System.err.println(" -n <Sname>              : name of RBNB source to write packets to");
			System.err.println("                default : "+Sname);
			System.err.println(" -s <server socket>     : socket number to listen for UDP packets on");
			System.err.println("                default : "+ssNum);
			System.err.println(" -c <num>               : cache frames");
			System.err.println("                default : "+cache);
			System.err.println(" -k <num>               : archive (disk) frames, append");
			System.err.println(" -K <num>               : archive (disk) frames, create");
			System.err.println(" -r <nsec>              : restart new source if stale data > nsec");
			System.err.println("                default : 0 (no archiving)");
			RBNBProcess.exit(0);
		}
		
		System.err.println("\nDatagram Socket: " + ssNum);
		System.err.println("RBNB Server: " + serverAddress);
		System.err.println("Source name: " + Sname);
		System.err.println("Cache size: " + cache);
		System.err.println("Archive mode: " + mode);
		System.err.println("Archive size: " + archive);

		try {
			ds=new DatagramSocket(ssNum);			//open port for incoming UDP
			ds.setSoTimeout(100);					// set 100 msec timeout
//			System.err.println("<ds recieveSize: " + ds.getReceiveBufferSize());
			ds.setReceiveBufferSize(10000000);		// old default was 8192 (ugh, lost packets)
//			System.err.println(">ds recieveSize: " + ds.getReceiveBufferSize());
		} catch (Exception e) { e.printStackTrace(); }

//		startSource();     // get an initial source going
	}

//-----------------------------------------------------------------------------------------------------------------------
// (re)open RBNB Source
	void startSource()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yy");
		Date now = new Date();

		baseTime = System.currentTimeMillis() / 1000.;		// update baseTime for relative timestamps

		try
		{
			if (src != null) src.Detach();
			src = new Source(cache, mode, archive);
			src.OpenRBNBConnection(serverAddress, Sname + "_" + formatter.format(now)+"_"+TestSequence());
			someData = false;
			System.err.println("\n" + src.GetClientName());
		}
		catch (Exception e) { e.printStackTrace(); }
	}

//-----------------------------------------------------------------------------------------------------------------------
// write metadata channel (stub)
	void writeMetaChan()
	{
		ChannelMap cm = new ChannelMap();
		SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss");
		Date now = new Date();
		String metachan = formatter.format(now);
		try {
			cm.Add(metachan);
			src.Register(cm);
		}
		catch (Exception e) { e.printStackTrace(); }

		if(Debug) System.err.println("metachan: "+metachan);
	}

//-----------------------------------------------------------------------------------------------------------------------
// Test sequence number string for Archive names
	String TestSequence()
	{
		String ts;

		if (autoRestart > 0.)
		{
			if (testNumber < 10) ts = new String("000" + testNumber);
			else if (testNumber < 100) ts = new String("00" + testNumber);
			else if (testNumber < 1000) ts = new String("0" + testNumber);
			else ts = new String("" + testNumber);

			++testNumber;
		}
		else {
			ts = new SimpleDateFormat("HH.mm.ss").format(new Date());
		}

		return (ts);
	}

//-----------------------------------------------------------------------------------------------------------------------
// Start method
	
	public void start() {
		try {
			DatagramPacket dp=new DatagramPacket(new byte[65536],65536);
			while (true) {
				try {
					ds.receive(dp);

					int packetSize = dp.getLength();
//					System.err.println("got DataGram! " + dp.getLength());
					if (packetSize > 0) {
						ChannelMap cm=process(dp);
						if (cm!=null) src.Flush(cm);
					}
				} catch(Exception e) {		// timeout - check for autoRestart
//					if(Debug) System.err.print("t");
					currentTime = System.currentTimeMillis() / 1000.;
					if (someData && (autoRestart > 0.) && ((currentTime - lastTime) > autoRestart))
					{
						startSource();	// (re)start new source
// OLD: a zero-time packet meant resync with system time, then do relative time increments from there
//						baseTime = currentTime;		// incoming packet-times should restart at 0 if relative time
					}
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

//-------------------------------------------------------------------------------------------------------------------	
// Process datagram into RBNB channelMap (single channel per call)

// NOTE:  this version presumes images (or at least byte-arrays).
// Eventually want to add a header field so one UDPproxy can handle Floats OR Images

// NOTE2:  ignore timestamp in header to get something to work first cut images

// Header is work in progress, likely to change with addition of datatype etc

	public ChannelMap process(DatagramPacket dp) {
		ChannelMap cm=new ChannelMap();
		int clen = 16;							// length of channel name field
//		int hdrlen = 8+16+clen;					// total header length (len fields, 2 doubles, cname)
		int hdrlen = 12 + 16 + 4 + clen;		// total header length (3 len fields, 2 doubles, type, cname)
		int dpoffset=dp.getOffset()+hdrlen;		// offset to start of data
		int dplen = (dp.getLength()-dpoffset);	// length per chan - header 
		byte[] data=new byte[dplen];
		byte[] dpdata = dp.getData();
		int i, idx, dtype;
		double dt, duration, itime, atime;
		String cname, ctype;
		boolean relativeTime=false;
		String mime = new String("application/octet-stream");

		if (someData == false) {
			someData = true;
//			writeMetaChan();
		}

		currentTime = System.currentTimeMillis() / 1000.;
		// Header Data Layout:
		// int4 len; char cname[16]
		// double count
		// double dt
		// int4 len; char ctype[4]
		// int4 len; string Data[]

		// first clen chars in packet are cname, then 2 doubles of timestamp, duration
		cname = new String(dpdata,4,clen);	// 4 byte length field offset
		int trim = cname.indexOf('\0');
		if(trim > 0) cname = cname.substring(0,trim);
//		if (cname.endsWith(".jpg")) isvideo = true;

		itime = arr2double(dpdata, 4+clen);
		if (itime < 1.e9) relativeTime = true;

		dt   = arr2double(dpdata, 12+clen);

		ctype = new String(dpdata, hdrlen-8, 4);	// channel type is last 4 bytes of header (prior to data len field)
		trim = ctype.indexOf('\0');
		if (trim > 0) ctype = ctype.substring(0, trim);

		duration = 0.;					// default

		if (ctype.equals("ARM")) {		// re-start (arm) source
			Sname = new String(cname);
			startSource();
			cname = new String("_MetaData");
			dtype = ChannelMap.TYPE_STRING;
		}
		else if (ctype.equals("DIS")) {		// stop/detach (disarm) source
			if (src != null) src.Detach();
			return (null);
		}
		else if (ctype.equals("STR"))
		{
			dtype = ChannelMap.TYPE_BYTEARRAY;		// was TYPE_STRING, but this isn't compatable with WebTurbine
			mime = new String("text/plain");
		}
		else if (ctype.equals("F32"))
		{
			dtype = ChannelMap.TYPE_FLOAT32;
			duration = dt * dplen / 4;
		}
		else if (ctype.equals("F64"))
		{
			dtype = ChannelMap.TYPE_FLOAT64;
			duration = dt * dplen / 8;
		}
		else if (ctype.equals("I16"))
		{
			dtype = ChannelMap.TYPE_INT16;
			duration = dt * dplen / 2;
		}
		else if (ctype.equals("I32"))
		{
			dtype = ChannelMap.TYPE_INT32;
			duration = dt * dplen / 4;
		}
		else
		{
			dtype = ChannelMap.TYPE_BYTEARRAY;
		}

		if (src == null) startSource();			// just to be sure

	// adjust times (after possible re-arm)
		if (relativeTime) {
		    if(duration > 0.) dt = duration;		// account for zero-duration images
		    atime = baseTime + itime * dt;
		} else 			atime = itime;

		double tinc = itime - lastiTime;
		lastiTime = itime;
		lastTime = atime;						// for next autoRestart compare

//		System.err.println("cname: "+cname+", dplen: "+dplen+", t: "+itime+", dt: "+dt+", ctype: "+ctype);
		if(Debug) System.err.println(Sname+"/"+cname+": "+dplen+" bytes @ "+itime+", "+atime+", ctype: "+ctype+", dt: "+dt+", tinc: "+tinc+", duration: "+duration);
		else if(dplen > 0) System.err.print(".");
		else {
				   System.err.print("x");
				   return cm;
		}

		if ((itime > 0.) && (tinc > 1.1))	// presumes integer time-indexing (may fail with Nchan>1)
			System.err.println("\nWarning, possible missed frame at T=" + itime + ", delta: "+tinc);

		try {
		    cm.PutTime(atime,duration);		// let it default for now?
//		    cm.PutTimeAuto("next");			// foo to force nominal working timestamps
		    System.arraycopy(dpdata,dpoffset,data,0,dplen);
		    idx=cm.Add(cname);
			cm.PutMime(idx, mime);			// help out WebTurbine
		    cm.PutData(idx,data,dtype);
		} catch (Exception e) { e.printStackTrace(); }
		return cm;
	}

//-------------------------------------------------------------------------------------------------------
// byteArray to numeric conversion functions

    	public static double arr2double (byte[] arr, int start) {
		int i = 0;
		int len = 8;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			//System.out.println(java.lang.Byte.toString(arr[i]) + " " + i);
			cnt++;
		}
		long accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 64; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return Double.longBitsToDouble(accum);
	}

	public static int arr2int (byte[] arr, int start) {
		int low = arr[start] & 0xff;
		int high = arr[start+1] & 0xff;
		return (int)( high << 8 | low );
	}

	
} //end class UDPproxy
