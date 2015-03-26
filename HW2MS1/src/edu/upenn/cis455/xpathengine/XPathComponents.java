package edu.upenn.cis455.xpathengine;

import java.util.List;

public class XPathComponents {
	
	public static class Step {
		public String elementName;
		public List<Predicate> predicates;
		public Step next;
	}
	
	public static class Predicate {
		public Step step;
		public int type;			//0-step, 1-text, 2-contains, 3-attr
		public String text;
		public String contains;
		public Attr attribute;
	}
	
	public static class Attr {
		public String attrName;
		public String attrValue;
	}
}
