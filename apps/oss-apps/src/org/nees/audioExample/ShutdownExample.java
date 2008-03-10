package org.nees.audioExample;

public class ShutdownExample {

    public static void main(String[] args) {
        (new ShutdownExample()).exec();
    }

    private void exec()
    {
        System.out.println("Running shutdown test... 30 sec wait.");
        // Add a shutdownHook to the JVM
        setup();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setup()
    {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown();
            }
          });        
    }

    private void shutdown()
    {
        System.out.println("Running Shutdown Hook");
    }
}
