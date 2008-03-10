package acme.serve;

import java.io.IOException;

import acme.serve.servlet.ServletException;
import acme.serve.servlet.http.HttpServlet;
import acme.serve.servlet.http.HttpServletRequest;
import acme.serve.servlet.http.HttpServletResponse;



public class EmptyServlet extends HttpServlet {

    /// Returns a string containing information about the author, version, and
    // copyright of the servlet.
    public String getServletInfo()
    {
    return "This is the servlet that is reached when no other servelt will do.";
    }

    /// Services a single request from the client.
    // @param req the servlet request
    // @param req the servlet response
    // @exception ServletException when an exception has occurred
    public void service( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        log("Empty Servlet: " + req.getServletPath());
        res.sendError( HttpServletResponse.SC_BAD_REQUEST );
        return;
    }
}
