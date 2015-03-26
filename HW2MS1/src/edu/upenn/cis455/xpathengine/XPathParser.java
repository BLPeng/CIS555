package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.upenn.cis455.xpathengine.XPathComponents.Predicate;
import edu.upenn.cis455.xpathengine.XPathComponents.Step;

public class XPathParser {
	
	private Document dom;
	private Step xpath;
	private List<Element> ret;
	
	public XPathParser(Document dom, Step xpath) {
		this.dom = dom;
		this.xpath = xpath;
	}
	
	public void setDocumentAndXPath(Document dom, Step xpathStep) {
		this.dom = dom;
		this.xpath = xpathStep;
	}
	
	public List<Element> evaluate() {
		List<Element> ret = new ArrayList<Element>();
		if (dom == null || xpath == null) {
			return ret;
		}
		Element rootElement = dom.getDocumentElement();
		matchStep(ret, rootElement, xpath, false);
		return ret;
	}
	
	private boolean matchNextStep(List<Element> ret, Element parent, NodeList elements, Step xpathStep, boolean isPredicate) {
		if (xpathStep == null) {
			if (!isPredicate)
				ret.add(parent);	//end node
			return true;	
			} else {	
				if (elements.getLength() == 0) {
					return false;
			}
		}
		//for this assignment, only consider step -> nodename ([ test ])* (axis step)?
		boolean ifMatch = false;
		for (int i = 0; i < elements.getLength(); i++) {
			if (elements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) elements.item(i);
                ifMatch = ifMatch | matchStep(ret, el, xpathStep, isPredicate);
			}
		}
		return ifMatch;
	}
	
	private boolean matchStep(List<Element> ret, Element element, Step xpathStep, boolean isPredicate) {
		if (xpathStep == null) {
			return true;
		}
		if (element == null) {
			return false;
		}
		String name = element.getNodeName();
//		System.out.print(name);
		if (name.equals(xpathStep.elementName)) {		//match name
			boolean pred = matchPredicate(ret, element, xpathStep.predicates);	//match predicates
			if (!pred)	return false;
			NodeList elements = element.getChildNodes();
			return matchNextStep(ret, element, elements, xpathStep.next, isPredicate);
		} else {
			return false;
		}

	}
	// arguments should not be null, check before pass in
	private boolean matchAttr(Element element, Predicate predicate) {
		String value = element.getAttribute(predicate.attribute.attrName);
		if (value == null) {
			return false;
		}
		return value.equals(predicate.attribute.attrValue);
	}
	
	private boolean matchPredicate(List<Element> ret, Element element, List<Predicate> predicates) {
		if (predicates.size() == 0) {
			return true;
		}
		boolean allTrue = true;
		for (Predicate predicate : predicates) {
			switch (predicate.type) {
			case 0:
				NodeList elements = element.getChildNodes();
				allTrue &= matchNextStep(ret, element, elements, predicate.step, true);
				break;
			case 1:
				allTrue &= matchText(element, predicate);
				break;
			case 2:
				allTrue &= matchContainsText(element, predicate);
				break;
			case 3:
				allTrue &= matchAttr(element, predicate);
				break;
			}
		}
		return allTrue;
	}
	// arguments should not be null, check before pass in
	private boolean matchText(Element element, Predicate predicate) {
		String value = element.getFirstChild().getNodeValue();
	//	String value = element.getElementsByTagName(name).item(0).getTextContent();
		if (value == null) {
			return false;
		}
		return value.equals(predicate.text);
	}
	// arguments should not be null, check before pass in
	private boolean matchContainsText(Element element, Predicate predicate) {
		String value = element.getFirstChild().getNodeValue();
	//	String value = element.getElementsByTagName(element.getTagName()).item(0).getTextContent();
		if (value == null) {
			return false;
		}
		return value.contains(predicate.contains);
	}
}
