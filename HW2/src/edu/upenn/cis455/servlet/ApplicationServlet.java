package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ApplicationServlet extends HttpServlet{
	
	protected boolean checkLogin(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Date createTime = new Date(session.getCreationTime());
	    Date lastAccessTime = new Date(session.getLastAccessedTime());
	    if (session.isNew() || lastAccessTime.getTime() + session.getMaxInactiveInterval() > System.currentTimeMillis()){
	        session.invalidate();
	    	return false;
	    } else {
	        return true;
	    }
	}
	
	protected void printWelcomePage(PrintWriter writer) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Welcome</title>");
        writer.println("</head>");
        writer.println("<body>");

        writer.println("</body>");
        writer.println("</html>");
        writer.close();
	}
}
