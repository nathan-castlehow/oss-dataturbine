/*

Copyright (c) 2004, Chris Adamson
See http://www.oreillynet.com/articles/author/1045

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/
package org.nees.iSight;

import quicktime.QTException;
import quicktime.QTSession;

public class QTSessionCheck {

    private Thread shutdownHook;
    private static QTSessionCheck instance;
    private QTSessionCheck() throws QTException {
        super();
        // init
        QTSession.open();
        // create shutdown handler
        shutdownHook = new Thread() {
                public void run() {
                    QTSession.exitMovies();
                    QTSession.close();
                }
            };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
    
    private static QTSessionCheck getInstance() throws QTException {
        if (instance == null)
            instance = new QTSessionCheck();
        return instance;
    }
    
    public static void check() throws QTException {
        // gets instance.  if a new one needs to be created,
        // it calls QTSession.open() and creates a shutdown hook
        // to call QTSession.close()
        getInstance();
    }

    public static void main (String[] args) {
        try {
            QTSessionCheck.check();
            System.exit(0);
        } catch (QTException qte) {
            qte.printStackTrace();
        }
    }

}
