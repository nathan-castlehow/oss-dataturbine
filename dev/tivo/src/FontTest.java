/*
 * FontTest.java
 * Created on Aug 2, 2005
 * 
 * Copied and modified from the Java Sun Tutorial.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

public class FontTest extends Canvas {

    private static final int STYLE = Font.BOLD;
    // possible settings are:
    //      Font.PLAIN, Font.BOLD, Font.ITALIC, 
    //      or Font.BOLD + Font.ITALIC
    
    private static final String NAME = "Monospaced";
    // possible names generic are: Default, Dialog, 
    //      DialogInput, Monospaced, Serif, or SansSerif
    // it is also possilbe to use specifis font names...
    
    private static final int SIZE = 8; // in points

    private static final String TEXT = "24 Point Times Bold";

    public FontTest() {
        setBackground(Color.white);
    }

    public void paint(Graphics g) {
        Graphics2D g2;
        g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);

        Dimension theSize= getSize();
        int x = theSize.width/30;
        int y = theSize.height/2 - 40;

        FontRenderContext frc1 = g2.getFontRenderContext();        
        Font f1 = new Font("Default", Font.PLAIN, 12);
        String s1 = new String("Normal -- Default");
        TextLayout tl1 = new TextLayout(s1, f1, frc1);
        
        g2.setColor(Color.BLACK);
        tl1.draw(g2, x, y);
        
        y += 30;
        FontRenderContext frc2 = g2.getFontRenderContext();        
        Font f2 = new Font(NAME, STYLE, SIZE);
        String s = new String(TEXT);
        TextLayout tl2 = new TextLayout(s, f2, frc2);

        g2.setColor(Color.black);
        tl2.draw(g2, x, y);
    }

    public static void main(String s[]) {
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowClosed(WindowEvent e) {System.exit(0);}
        };

        Frame f = new Frame("Font Test");
        f.addWindowListener(l);
        f.add("Center", new FontTest());
        f.pack();
        f.setSize(new Dimension(400, 300));
        f.show();
    }
}

