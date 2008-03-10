package edu.sdsc.rtdsm.stubs;

import java.util.Date;
import java.text.DateFormat;


import edu.sdsc.rtdsm.framework.feedback.SrcFeedbackListener;
import edu.sdsc.rtdsm.framework.util.Debugger;

public class FeedbackListenerStub extends SrcFeedbackListener{

  public void receiveFeedback(String feedbackMsg, Date time) {

    String timeStr = DateFormat.getDateTimeInstance().format(time);
    Debugger.debug(Debugger.TRACE, "Received some feedback at the end " + 
      "source. Time: " + timeStr + " Msg: " + feedbackMsg );
  }
}
