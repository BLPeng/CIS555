package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.PasswordHash;
import edu.upenn.cis455.storage.User;
import edu.upenn.cis455.storage.UserDA;


public class LoginServlet extends ApplicationServlet{
	@Override
	  public void init() throws ServletException {
	    super.init();
	    DBWrapper.setupDirectory("database");
	  }
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String username = request.getParameter("user");
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
			username = URLDecoder.decode(username, "utf-8").trim();
			pwd = URLDecoder.decode(pwd, "utf-8").trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		if (username == null || username.length() == 0 ) {
			printErrorPage(writer, getBanner(request), "empty username");
		} else if (pwd == null || pwd.length() == 0 ){
			printErrorPage(writer, getBanner(request), "password username");
		} else if (checkLogin(request)) {
			printWelcomePage(writer, getBanner(request));	
		} else {
			User user = UserDA.getEntry(username);
			String hash;
			try {
				hash = PasswordHash.hashPassword(pwd);
				if (user != null && user.getPassword().endsWith(hash)) {
					printWelcomePage(writer, getBanner(request));
					HttpSession session = request.getSession(true);
					session.setAttribute("user", user);
				} else {
					printLoginPage(writer, getBanner(request), "user no exist / password incorrect");
				}
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				printLoginPage(writer, getBanner(request), "password incorrect!");
				e.printStackTrace();
			}
			
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
			printWelcomePage(writer, getBanner(request));	
		} else {
			printLoginPage(writer, getBanner(request), null);
		}
	}
	
	private void printLoginPage(PrintWriter writer, String banner, String msg) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Login Page</title>");
        writer.println("</head>");
        writer.println("<body>");
        writer.println(banner+"<br/>");
        writer.println(msg+"<br/>");
        writer.println("Login Page! timout = 10 min<br/>");
        writer.println("<form method=\"post\">");
        writer.println("Username: <input type=\"text\" name=\"user\"><br>");
        writer.println("Password: <input type=\"password\" name=\"pwd\"><br>");
        writer.println("<input type=\"submit\" value=\"Login\">");
        writer.println("</form>");
        writer.println("</body>");
        writer.println("</html>");
		writer.close();	
	}
}
