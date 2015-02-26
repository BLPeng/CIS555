package edu.upenn.cis.cis455.webservlet;



import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Simple Hello servlet.
 */

public final class HelloServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
      throws IOException, ServletException {

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();        
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Test Servlet </title>");
        writer.println("</head>");
        writer.println("<body>");

        writer.println("just test ");
        writer.println("the Hello, World application.");

        writer.println("</body>");
        writer.println("</html>");
    }
} 