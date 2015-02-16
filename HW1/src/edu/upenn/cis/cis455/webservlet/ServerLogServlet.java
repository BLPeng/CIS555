package edu.upenn.cis.cis455.webservlet;



import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis.cis455.webserver.HttpServer;
import edu.upenn.cis.cis455.webserver.HttpServerUtils;


/**
 * Simple Hello servlet.
 */

public final class ServerLogServlet extends HttpServlet {


    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are producing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();        
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Server Error Log Page</title>");
        writer.println("</head>");
        writer.println("<body bgcolor=white>");
        writer.println("Server Error Logs: </br>");
        String content = HttpServerUtils.getFileContent(HttpServer.errorLog);
        writer.println(content);
        writer.println("</body>");
        writer.println("</html>");
    }
} 
