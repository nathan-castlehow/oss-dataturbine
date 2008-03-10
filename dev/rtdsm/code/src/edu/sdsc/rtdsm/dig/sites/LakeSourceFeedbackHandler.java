package edu.sdsc.rtdsm.dig.sites;

import java.util.Date;
import java.text.DateFormat;


import edu.sdsc.rtdsm.drivers.turbine.util.TurbineSrcConfig;
import edu.sdsc.rtdsm.framework.src.SrcConfig;
import edu.sdsc.rtdsm.framework.feedback.SrcFeedbackListener;
import edu.sdsc.rtdsm.framework.util.Debugger;

public class LakeSourceFeedbackHandler extends SrcFeedbackListener{

  TurbineSrcConfig srcConfig = null;
  LakeSource src = null;

  public LakeSourceFeedbackHandler(LakeSource src, SrcConfig srcConfig){
    if(!(srcConfig instanceof TurbineSrcConfig)) {

      throw new IllegalStateException("Only DataTurbine is supported in " +
          "the current version.");
    }
    this.srcConfig = (TurbineSrcConfig)srcConfig;
    this.src = src;
  }

  public void receiveFeedback(String feedbackMsg, Date time) {

    String timeStr = DateFormat.getDateTimeInstance().format(time);
    Debugger.debug(Debugger.TRACE, "FEEDBACK:" +  srcConfig.getName() + 
      "Time: " + timeStr + " Msg: " + feedbackMsg );
    src.appendFeedbackMsg(srcConfig.getName(),time, feedbackMsg);
    
  }
}
