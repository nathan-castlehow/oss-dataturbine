import com.rbnb.sapi.*;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Client;

public class SinkProxy {

    Sink sink;
    String className = "<SinkClientManager>";
    String server = "localhost:3333";
    long lastTimeStamp;
    double durationSeconds = 20.0;
    
    public SinkProxy( String sinkName, String serverURL) {
	this.sink = new Sink();
	this.className = sinkName;
	this.server = serverURL;
    }
    
    public void OpenRBNBConnection () throws SAPIException{
	this.sink.OpenRBNBConnection();
    }
    
    public boolean VerifyConnection() {
	return this.sink.VerifyConnection();
    }
    
    public String GetClientName() {
	return this.sink.GetClientName();
    }

    public String GetServerName() {
	return this.sink.GetServerName();
    }
    
    
    public void RequestRegistrationAll() throws SAPIException{
	this.sink.RequestRegistration();
    }
    
    public void RequestRegistration (ChannelMap chm) throws SAPIException{
	this.sink.RequestRegistration(chm);
    }
    
    public void CloseRBNBConnection () {
	this.sink.CloseRBNBConnection();
    }
    
    public void Request(ChannelMap cm, double start, double duration, String reference) throws SAPIException {
	this.sink.Request(cm, start, duration, reference);
	
    }
    
    public void Subscribe(ChannelMap cm, double start, double duration, String reference)  throws SAPIException{
	this.sink.Subscribe(cm, start, duration, reference);
    }

    public ChannelMap Fetch(long blockTimeout) throws SAPIException{
	return this.sink.Fetch (blockTimeout);
    }

    public ChannelMap Fetch(long blockTimeout, ChannelMap cm) throws SAPIException {
	return this.sink.Fetch (blockTimeout, cm);
    }

    
    
    
}

