package edu.upenn.cis.cis455.webservlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {
	//preset user account
    private final String user = "test";
    private final String password = "test";
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	/*String user = request.getParameter("user");
        String pwd = request.getParameter("pwd");*/
    	HttpSession session = request.getSession();
    	long expire = session.getLastAccessedTime() + session.getMaxInactiveInterval() * 1000;
    	long cur = System.currentTimeMillis();
        if(expire > cur && !session.isNew()){
            session.setMaxInactiveInterval(10*60);
/*            Cookie cookie = new Cookie("user", user);
            cookie.setMaxAge(10*60);
            response.addCookie(cookie);
            cookie = new Cookie("sid", session.getId());
            cookie.setMaxAge(10*60);
            response.addCookie(cookie);*/
            welcomePage(response);   
        }else{
        	loginPage(response);  
        }
    }
    
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = request.getParameter("user");
        String pwd = request.getParameter("pwd");
         
        if(user.equals(user) && pwd.equals(pwd)){
            HttpSession session = request.getSession();
            session.setMaxInactiveInterval(10*60);
            Cookie cookie = new Cookie("user", user);
            cookie.setMaxAge(10*60);
            response.addCookie(cookie);
            cookie = new Cookie("sid", session.getId());
            cookie.setMaxAge(10*60);
            response.addCookie(cookie);
            welcomePage(response);   
        }else{
        	loginPage(response);  
        }
 
    }
    private void loginPage(HttpServletResponse response) throws IOException {
    	response.setContentType("text/html");
        PrintWriter writer = response.getWriter();        
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Login Page</title>");
        writer.println("</head>");
        writer.println("<body>");

        writer.println("Login Page! timout = 10 min");
        writer.println("<form action=\"login\" method=\"post\">");
        writer.println("Username: <input type=\"text\" name=\"user\"><br>");
        writer.println("Password: <input type=\"password\" name=\"pwd\"><br>");
        writer.println("<input type=\"submit\" value=\"Login\">");
        writer.println("</form>");
        writer.println("</body>");
        writer.println("</html>");
    }
    private void welcomePage(HttpServletResponse response) throws IOException {
    	response.setContentType("text/html");
        PrintWriter writer = response.getWriter();        
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Welcome login</title>");
        writer.println("</head>");
        writer.println("<body>");

        writer.println("Welcone! timout = 10 min");
        writer.println("Hello World.");

        writer.println("</body>");
        writer.println("</html>");
    }
 
}