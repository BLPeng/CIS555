package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.User;

public class ApplicationServlet extends HttpServlet{
	public User user;
	protected boolean checkLogin(HttpServletRequest request) {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		Date createTime = new Date(session.getCreationTime());
	    Date lastAccessTime = new Date(session.getLastAccessedTime());
	    if (session.isNew() || user == null || user.getUserName() == null || user.getUserName().length() == 0){   
	    	return false;
	    } /*else if (lastAccessTime.getTime() + session.getMaxInactiveInterval() > System.currentTimeMillis()) {
	    	session.invalidate();
	    	return false;
	    }*/else {
	        return true;
	    }
	}
	
	protected void printErrorPage(PrintWriter writer, String error) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Welcome</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println(error);
        writer.println("</body>");
        writer.println("</html>");
        writer.close();
	}
	
	protected void printWelcomePage(PrintWriter writer) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Welcome</title>");
        writer.println("</head>");
        writer.println("<body>");
        if (user != null) {
        	writer.println("User:" + user.getUserName());
        }
        writer.println("<a href=\"logout\">");
        writer.println("<button>logout</button></a>");
        writer.println("</body>");
        writer.println("</html>");
        writer.close();
	}
}
