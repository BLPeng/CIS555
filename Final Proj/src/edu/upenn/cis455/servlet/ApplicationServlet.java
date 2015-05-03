package edu.upenn.cis455.servlet;

import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.User;

public class ApplicationServlet extends HttpServlet{
	public User user;
	public String username;
	protected boolean checkLogin(HttpServletRequest request) {
		HttpSession session = request.getSession();
//		user = (User) session.getAttribute("user");
		username = (String) session.getAttribute("user");
		Date createTime = new Date(session.getCreationTime());
	    Date lastAccessTime = new Date(session.getLastAccessedTime());
/*	    if (session.isNew() || user == null || user.getUserName() == null || user.getUserName().length() == 0){   
	    	return false;
	    } */
	    if (session.isNew() || username == null || username.length() == 0){   
	    	return false;
	    }/*else if (lastAccessTime.getTime() + session.getMaxInactiveInterval() > System.currentTimeMillis()) {
	    	session.invalidate();
	    	return false;
	    }*/else {
	        return true;
	    }
	}
	
	protected String getBanner(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		if (checkLogin(request)) {
			sb.append("<a href=\"/servlet/logout\">");
			sb.append("<button>Logout</button></a>");
		} else {
			sb.append("<a href=\"/servlet/login\">");
			sb.append("<button>Login</button></a>");
		}
		sb.append("<a href=\"/servlet/register\">");
		sb.append("<button>Register</button></a>");
		sb.append("<a href=\"/servlet/channel\">");
		sb.append("<button>Channel</button></a>");
		return sb.toString();
	}
	
	protected void printErrorPage(PrintWriter writer, String banner, String error) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Welcome</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println(banner+"<br/>");
        writer.println(error);
        writer.println("</body>");
        writer.println("</html>");
        writer.close();
	}
	
	protected void printWelcomePage(PrintWriter writer, String banner) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Welcome</title>");
        writer.println("</head>");
        writer.println("<body>");
        if (user != null) {
        	writer.println("User:" + user.getUserName());
        }
        writer.println(banner+"<br/>");
        writer.println("</body>");
        writer.println("</html>");
        writer.close();
	}
}
