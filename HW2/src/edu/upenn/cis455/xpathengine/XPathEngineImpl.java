package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import edu.upenn.cis455.xpathengine.XPathComponents.Step;

public class XPathEngineImpl implements XPathEngine {
  
	private String[] xpaths = null;
	private List<Step> xpathSteps = null;
	private XPathValidator validator;
	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
		validator = new XPathValidator();
	}
	
	public void setXPaths(String[] s) {
		this.xpaths = s;
		if (this.xpaths == null) {
			return;
		}
		xpathSteps = new ArrayList<>(xpaths.length);
		for (int i = 0; i < s.length; i++) {
			xpathSteps.add(validator.getStep(xpaths[i]));
		}
	}

	public boolean isValid(int i) {
		if (xpathSteps == null) {
			return false;
		}
		if (xpathSteps.get(i) == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean[] evaluate(Document d) { 
	  /* TODO: Check whether the document matches the XPath expressions */
		return null; 
	}
	
	public static void main(String[ ] args)
	{
		XPathEngineImpl xpathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine(); 
		 String[] xpaths0 = {"/catalog/cd[@title=\"Empire Burlesque\"][@artist=\"Bob Dylan\"]"};
	      
	      xpathEngine.setXPaths(xpaths0);
	      for (int i = 0; i < xpaths0.length; i++) {
	    	  System.out.println(xpathEngine.isValid(i));
	      }
	      
		/*
	      System.out.println("----------------------------------------------------------");
	      String[] xpaths = {null, "", "/catalog]","/catalog[","/catalog/cd[@title=Empire Burlesque\"]",
	    		  "/catalog/cd[@title=\"Empire Burlesque\"][artist=\"Bob Dylan\"]",
	    		  "/catalog/cd[@title=Empire Burlesque\"]]",
	    		  "/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]]",
	    		  "/catalog/cd[[@title=Empire Burlesque\"]",
	    		  "/catalog/cd[@year=\"1988\"][[@price=\"9.90\"]/country[text()=\"UK\"]",
	    		  "/catalog/!badelem",
	    		  "/@frenchbread/unicorns",
	    		  "/abc/123bad",
	    		  "/hello world",
	    		  "/check(these)chars",
	    		  "/xmlillegal",
	    		  "/XMLillegal",
	    		  "/abc/ab[@,illegalattribute=\"hello\"]",
	    		  "/abc/ab[@<illegalattribute=\"hello\"]",
	    		  "/abc/ab[text()=\"abc\"  pqr]",
	    		  "/abc/ab[@attname\"=\"abc\"]",
	    		  "/abc/ab[@=\"hello\"]"};
	      xpathEngine = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();
	      xpathEngine.setXPaths(xpaths);
	      for (int i = 0; i < xpaths.length; i++) {
	    	  System.out.println(xpathEngine.isValid(i));
	      }*/
	      System.out.println("----------------------------------------------------------");
	      String[] xpaths1 = {"/catalog","/catalog[text]","/catalog/cd[@title=\"Empire Burlesque\"]",
	    		  "/catalog/cd[@title=\"Empire Burlesque\"][@artist=\"Bob Dylan\"]",
	    		  "/catalog/cd[@title=\"Empire Burlesque\"]",
	    		  "/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]",
	    		  "/catalog/cd[@title=\"Empire Burlesque\"]",
	    		  "/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]",
	    		  "/catalog/badelem",
	    		  "/frenchbread/unicorns",
	    		  "/abc/bad",
	    		  "/hello.world",
	    		  "/check-these_chars",
	    		  "/illegal",
	    		  "/illegal",
	    		  "/abc/ab[@illegalattribute=\"hello\"]",
	    		  "/abc/ab[@illegalattribute=\"hello\"]",
	    		  "/abc/ab[text()=\"abc\"]",
	    		  "/abc/ab[@attname=\"abc\"]",
	    		  "/abc/ab[@d=\"hello\"]"};
	      xpathEngine.setXPaths(xpaths1);
	      for (int i = 0; i < xpaths1.length; i++) {
	    	  System.out.println(xpathEngine.isValid(i));
	      }
	      System.out.print(true);
	}
        
}
