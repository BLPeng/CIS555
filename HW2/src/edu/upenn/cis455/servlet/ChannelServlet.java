package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.DBWrapper;


public class ChannelServlet extends ApplicationServlet{
	@Override
	public void init() throws ServletException {
	    super.init();
	    DBWrapper.setupDirectory("database");
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
			printChannelsPage(writer, true);	
		} else {
			printChannelsPage(writer, false);
		}
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String xpath = request.getParameter("xpaths");
		String url = request.getParameter("url");
		String name = request.getParameter("name");
		String op = request.getParameter("operation");
		response.setContentType("text/html");
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if (!checkLogin(request)) {
			printChannelsPage(writer, false);
			return;
		}
    	if ("delete".equals(op)) {
    		name = request.getParameter("name"); 		
			ChannelDA.deleteEntry(name);
    	} else if ("display".equals(op)) { 
    		name = request.getParameter("name"); 		
			
    	} else {
    		try {
    			xpath = URLDecoder.decode(xpath, "utf-8").trim();
    			url = URLDecoder.decode(url, "utf-8").trim();
    			name = URLDecoder.decode(name, "utf-8").trim();
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    			return;
    		}
    		if (xpath.isEmpty()) {
    			printErrorPage(writer, "Empty xpath <br/>");
    		} else if (url.isEmpty()) {
    			printErrorPage(writer, "Error: Empty URL <br/>");
    		} else if (name.isEmpty()) { 
    			printErrorPage(writer, "Error: Empty Name <br/>");
    		} else if (ChannelDA.getEntry(name) != null) { 
    			printErrorPage(writer, "Name exists <br/>");
    		} else {
    			if (!url.toLowerCase().startsWith("http://")) {
    				url = "http://" + url;
    			}
    			String[] xpaths = xpath.split(";");
    			for (int i = 0; i < xpaths.length; i++) {
    				xpaths[i] = xpaths[i].trim();
    			}
    			Channel channel = new Channel(name, user.getUserName(), url, new Date(), xpaths);
    			ChannelDA.putEntry(channel);
    		}
    	}	
		printChannelsPage(writer, true);
	}
	
	
	private void printChannelsPage(PrintWriter writer, boolean login) {
		writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Login Page</title>");
        writer.println("</head>");
        writer.println("<body>");
        if (login) {
        	writer.println("Create new channel<br/>");
            writer.println("<form method=\"post\">");
            writer.println("Channel name:<br/>");
            writer.println("<input type=\"text\" name=\"name\" size=\"100\" ><br/>");
            writer.println("XPaths: separate by semicolons<br/>");
            writer.println("<input type=\"text\" name=\"xpaths\" size=\"100\" ><br/>");
            writer.println("URL:<br/>");
            writer.println("<input type=\"text\" name=\"url\" size=\"100\"><br/>");
            writer.println("<input type=\"submit\" value=\"New\">");
            writer.println("</form>");
        }
        writer.println(getChannels());
        writer.println("</body>");
        writer.println("</html>");
		writer.close();	
	}
	
	private String getHiddenForm(String user, String name) {
		StringBuilder sb = new StringBuilder();
		if (this.user.getUserName().equals(user)) {
			sb.append("<form method=\"post\">");
			sb.append("<input type=\"hidden\" name=\"operation\" value=\"display\"/>");
			sb.append("<input type=\"hidden\" name=\"name\" value=\"" + name + "\" ><br/>");
			sb.append("<input type=\"submit\" value=\"Delete\">");
			sb.append("</form>");
		}
		return sb.toString();
	}
	
	
	private String getHiddenForm1(String name) {
		StringBuilder sb = new StringBuilder();
		if (true) {
			sb.append("<form method=\"post\">");
			sb.append("<input type=\"hidden\" name=\"operation\" value=\"display\"/>");
			sb.append("<input type=\"hidden\" name=\"name\" value=\"" + name + "\" ><br/>");
			sb.append("<input type=\"submit\" value=\"Display\">");
			sb.append("</form>");
		}
		return sb.toString();
	}
	private String getChannels() {
		List<Channel> channels = ChannelDA.getEntries();
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		sb.append("<tr><th>Name</th><th>XPaths</th><th>URL</th><th>Created_at</th><th> </th><th> </th></tr>");
		for (int i = 0; i < channels.size(); i++) {
			StringBuilder tmp = new StringBuilder();
			for (String xpath : channels.get(i).getXpaths()) {
				tmp.append(xpath + System.lineSeparator());
			}
			sb.append("<tr><td>" + channels.get(i).getName() + "</td><td>" + tmp.toString() + "</td><td>" + channels.get(i).getUrl() + "</td><td>" + channels.get(i).getCreatedAt() + "</td>"
					+ "<td>" + getHiddenForm(channels.get(i).getUserName(), channels.get(i).getName()) + "</td>"
							+ "<td>" + getHiddenForm1(channels.get(i).getName()) + "</td></tr>");
		}
		sb.append("</table><br/><br/>");
		return sb.toString();
	}
}
