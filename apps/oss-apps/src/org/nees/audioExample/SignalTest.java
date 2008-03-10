package org.nees.audioExample;

import java.util.Observer; 
import java.util.Observable; 
/** 
 * <p>SignalTest serves two purposes:</p> 
 * 
 * <ol> 
 * <li>It creates and sets up the signal handling (in the constructor) 
 * <li>It implements Observer and provides the update method that gets 
 *     called whenever a signal is received 
 * </ol> 
 * 
 * <p>Merely instantiating this object (and hanging onto a reference 
 * to it) is enough to enable the signal handling.</p> 
 **/ 
public class SignalTest 
  implements Observer 
{ 
  /** 
   * The software's entry point; exits after 10 seconds. 
   * @param args Any and all arguments are ignored and have no effect. 
   **/ 
  public static void main( final String[] args ) 
  { 
    new SignalTest().go(); 
  } 
  private void go() 
  { 
    try 
      { 
        final SignalHandler sh = new SignalHandler(); 
        sh.addObserver( this ); 
        sh.handleSignal( "HUP" ); 
        sh.handleSignal( "INT" );
        sh.handleSignal( "TERM" );
        System.out.println( "Sleeping for 30 seconds: hit me with signals!" ); 
        Thread.sleep( 30000 ); 
      } 
    catch( Throwable x ) 
      { 
        // SignalHandler failed to instantiate: maybe the classes do not exist, 
        // or the API has changed, or something else went wrong; actualy we get 
        // here on an InterruptedException from Thread.sleep, too, but that is 
        // probably quite rare and doesn't matter in a simple demo like this. 
        x.printStackTrace(); 
      } 
  } 
  /** 
   * Implementation of Observer, called by {@link SignalHandler} when 
   * a signal is received. 
   * @param o Our {@link SignalHandler} object 
   * @param arg The {@link sun.misc.Signal} that triggered the call 
   **/ 
  public void update( final Observable o, 
                      final Object arg ) 
  { 
    // use the same method that the Timer employs to trigger a 
    // rotation, which ensures that signal and timer don't screw 
    // each other up. 
    System.out.println( "Received signal: "+arg ); 
  } 
}

