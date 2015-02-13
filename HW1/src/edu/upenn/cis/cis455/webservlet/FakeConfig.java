package edu.upenn.cis.cis455.webservlet;

import javax.servlet.*;

import java.util.*;


public class FakeConfig implements ServletConfig {
	private String name;
	private FakeContext context;
	private HashMap<String,String> initParams;
	
	public FakeConfig(String name, FakeContext context) {		//init with context and servlet name
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
	}

	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);		//vector? for thread-safe?
		return atts.elements();
	}
	
	public ServletContext getServletContext() {			//ok
		return context;
	}
	
	public String getServletName() {		//ok
		return name;
	}

	void setInitParam(String name, String value) {		//set init param, may have multi pairs
		initParams.put(name, value);
	}
}
