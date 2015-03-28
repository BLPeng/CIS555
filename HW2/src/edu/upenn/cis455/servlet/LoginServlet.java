package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class LoginServlet extends ApplicationServlet{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String user = request.getParameter("user");
        String pwd = request.getParameter("pwd");
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			user = URLDecoder.decode(user, "utf-8").trim();
			pwd = URLDecoder.decode(pwd, "utf-8").trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		if (checkLogin(request)) {
			printWelcomePage(writer);	
		} else {
			
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}     
		if (checkLogin(request)) {
			printWelcomePage(writer);	
		} else {
			printLoginPage(writer);
		}
	}
	
	private void printLoginPage(PrintWriter writer) {
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
		writer.close();	
	}
}
