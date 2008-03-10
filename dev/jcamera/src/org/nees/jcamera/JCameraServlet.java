package org.nees.jcamera;

import java.io.IOException;

import java.util.Enumeration;

import acme.serve.servlet.ServletException;
import acme.serve.servlet.ServletOutputStream;
import acme.serve.servlet.http.HttpServlet;
import acme.serve.servlet.http.HttpServletRequest;
import acme.serve.servlet.http.HttpServletResponse;

public class JCameraServlet extends HttpServlet {
    
    private static final String BASE = "/camera";
    private static final String INFO_REQUEST = "/info";
    private static final String IMAGE_REQUEST = "/image";
    private static final String USAGE_REQUEST = "/usage";
    public static final String[] PATTERNS = {
        BASE,
        BASE + "/",
        BASE + "/*"
    };
    
    // / Returns a string containing information about the author, version, and
    // copyright of the servlet.
    public String getServletInfo() {
        return "JCamera Servlet: a servlet that returns information or" +
                " an image from an attached digital camera";
    }

    // / Services a single request from the client.
    // @param req the servlet request
    // @param req the servlet response
    // @exception ServletException when an exception has occurred
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        long time = System.currentTimeMillis();
        String path = req.getServletPath();
        log("Called JCamera Servlet: " + path);

        if (!(req.getMethod().equalsIgnoreCase("get") || 
                req.getMethod().equalsIgnoreCase("post"))) {
            res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            return;
        }

        if (path == null || path.charAt(0) != '/') {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (path.indexOf("/../") != -1 || path.endsWith("/..")) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!path.startsWith(BASE)) { // not possible?
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;            
        }

        String request = path.substring(BASE.length());
        
        if (request == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        if (request.startsWith(INFO_REQUEST))
        {
            displayInfo(req,res);
            return;
        }
        
        if (request.startsWith(USAGE_REQUEST))
        {
            usageRequest(req,res);
            return;
        }
        
        if (request.startsWith(IMAGE_REQUEST))
        {
            request = request.substring(IMAGE_REQUEST.length());
            if (request.length() > 0 && request.startsWith("/")) // remove leading /
                request = request.substring(1);
//            System.out.println("Making image request with: " + request);
            processRequest(request,req,res);
//            long dtime = System.currentTimeMillis() - time;
//            System.out.println("Processing of image request took " + dtime + " milliseconds.");
            return;
        }
        
        displayBadCameraRequest(req,res,request);
        return;
    }
    
    private void processRequest(String command, HttpServletRequest req, HttpServletResponse res)
             throws IOException {
        
        String name = command;
        int pos = name.indexOf("/");
        if (pos > -1)
            name = name.substring(0,pos);
// if the rest of the command string is needed by a camera...
//        String rest;
//        if (name.length() < command.length())
//            rest = command.substring(name.length());
//        else
//            rest = "";
        
        Camera c = null;
        for (int i = 0; i < Camera.CAMERA_ARRAY.length; i++)
        {
            if (Camera.CAMERA_ARRAY[i].getTypeName().equals(name))
                c = Camera.CAMERA_ARRAY[i];
        }
        
        if (c != null)
        {
            try
            {
                Camera temp = (Camera)(c.getClass()).newInstance();
                // use commands in 'rest', see above, to set up this camera
                c = temp;
            }
            catch (Throwable ignore) {}
        }

        res.setStatus(HttpServletResponse.SC_OK);
        ServletOutputStream p = res.getOutputStream();
        if (c == null)
        {
            res.setContentType("text/html");
            p.println("<HTML><HEAD>");
            p.println("<TITLE>No Camera - no image</TITLE>");
            p.println("</HEAD><BODY>");
            p.println("No camera for camera type = " + name + "<br>");
            p.println("Access of camera/image with command = " + command + "<br>");
            p.println("</BODY></HTML>");
            p.flush();
            p.close();
            return;            
        }
        if (!c.hasImage())
        {
            res.setContentType("text/html");
            p.println("<HTML><HEAD>");
            p.println("<TITLE>Camera - no image</TITLE>");
            p.println("</HEAD><BODY>");
            p.println("No image for camera type = " + name + "<br>");
            p.println("Access of camera/image with command = " + command + "<br>");
            p.println("</BODY></HTML>");
            p.flush();
            p.close();
            return;            
        }
        byte [] image = c.getRecentImageBuffer();
        if (image == null)
        {
            res.setContentType("text/html");
            p.println("<HTML><HEAD>");
            p.println("<TITLE>Camera - failed to get image</TITLE>");
            p.println("</HEAD><BODY>");
            p.println("Failed to get image for camera type = " + name + "<br>");
            p.println("Access of camera/image with command = " + command + "<br>");
            p.println("</BODY></HTML>");
            p.flush();
            p.close();
            return;            
        }
        res.setContentType("image/jpeg");
        res.setContentLength(image.length);
        p.write(image);
        p.flush();
        p.close();
    }

    private void displayInfo(HttpServletRequest req, HttpServletResponse res)
         throws IOException, ServletException
     {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
        ServletOutputStream p = res.getOutputStream();
        p.println("<HTML><HEAD>");
        p.println("<TITLE>Info Display Output</TITLE>");
        p.println("</HEAD><BODY>");
        p.println("<HR>");
        p.println("<H2>Test JCamera Servlet Content</H2>");
        p.println("<br>");
        p.println("Camera Types are: <ul>");
        for (int i = 0; i < Camera.CAMERA_ARRAY.length; i++)
        {
            Camera c = Camera.CAMERA_ARRAY[i];
            String name = c.getTypeName();
            String description = c.getTypeDescription();
            p.print("<li>");
            if (c.available())
                p.print("<a href=\"/camera/image/"+ name +"\">" + name + "</a>: " + description);
            else
                p.print(name + "(<b>not available</b>): " + description);
            p.println("</li>");
        }   
        p.println("</ul>");
        p.println("<HR>");
        p.println("<H2>Test JCamera Servlet Content</H2>");
        p.println("<br>");
        p.println("<PRE>");
        p.println("getContentLength(): " + req.getContentLength());
        p.println("getContentType(): " + req.getContentType());
        p.println("getProtocol(): " + req.getProtocol());
        p.println("getScheme(): " + req.getScheme());
        p.println("getServerName(): " + req.getServerName());
        p.println("getServerPort(): " + req.getServerPort());
        p.println("getRemoteAddr(): " + req.getRemoteAddr());
        p.println("getRemoteHost(): " + req.getRemoteHost());
        p.println("getMethod(): " + req.getMethod());
        p.println("getRequestURI(): " + req.getRequestURI());
        p.println("getServletPath(): " + req.getServletPath());
        p.println("getPathInfo(): " + req.getPathInfo());
        p.println("getPathTranslated(): " + req.getPathTranslated());
        p.println("getQueryString(): " + req.getQueryString());
        p.println("getRemoteUser(): " + req.getRemoteUser());
        p.println("getAuthType(): " + req.getAuthType());
        p.println("");
        p.println("Parameters:");
        Enumeration en;
        en = req.getParameterNames();
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            p.println("    " + name + " = " + req.getParameter(name));
        }
        p.println("");
        p.println("Headers:");
        en = req.getHeaderNames();
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            p.println("    " + name + ": " + req.getHeader(name));
        }
        p.println("</PRE>");
        p.println("<HR>");
        p.println("</BODY></HTML>");
        p.flush();
        p.close();
    }

    private void displayBadCameraRequest(HttpServletRequest req, 
            HttpServletResponse res, String request) throws IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
        ServletOutputStream p = res.getOutputStream();
        p.println("<HTML><HEAD>");
        p.println("<TITLE>Camera - bad request</TITLE>");
        p.println("</HEAD><BODY>");
        p.println("Bad request to camera: " + request);
        p.println("Use 'camera/info' to obtain information and 'camera/usage' " +
                "for useful forms.");
        p.println("</BODY></HTML>");
        return;
    }
    
    private void usageRequest(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/html");
        ServletOutputStream p = res.getOutputStream();
        p.println("<HTML><HEAD>");
        p.println("<TITLE>Camera - usage</TITLE>");
        p.println("</HEAD><BODY>");
        p.println("Use <ul>" +
                "<li>'camera/info' to obtain information,</li>" +
                "<li>'camera/image' to get a 'dummy' camera image, and</li>" +
                "<li>'camera/usage' to see this page.</li>" +
                "</ul>");
        p.println("</BODY></HTML>");
        return;
    }


}
