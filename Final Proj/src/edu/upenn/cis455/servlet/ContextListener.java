package edu.upenn.cis455.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.upenn.cis455.storage.DBWrapper;

public class ContextListener implements ServletContextListener {

	@Override
    public void contextDestroyed(ServletContextEvent event) {
    	// Close database
    	DBWrapper.closeDBs();
    	System.out.println("Database closed");
    }

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
	//    DBWrapper.setupDirectory(context.getInitParameter("BDBstore"));
	}
	
}
