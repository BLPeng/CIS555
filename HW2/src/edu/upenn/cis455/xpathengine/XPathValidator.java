package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.upenn.cis455.xpathengine.XPathComponents.Attr;
import edu.upenn.cis455.xpathengine.XPathComponents.Predicate;
import edu.upenn.cis455.xpathengine.XPathComponents.Step;


/*XPath -> axis step
axis -> '/'
step -> nodename ([ test ])* (axis step)?
test -> step
     -> text() = "..."
     -> contains(text(), "...")
     -> @attname = "..."
*/
public class XPathValidator {
	
	private int index;
	private StringBuilder sb;
	private String inputXPath;
	private final String TEXT = "text";
	private final String CONTAINS = "contains";
	
	// init the validator for next xpath
	private void initValidator(String inputXPath) {
		sb = new StringBuilder();
		this.index = 0;
		this.sb.setLength(0);
		this.inputXPath = inputXPath;
	}
	public XPathValidator() {
	
		
	}
	public Step getStep(String xml) {
		if (xml == null) {
			return null;
		}
		initValidator(xml);
		return getNode();		// axis step
	}
	
	private Step getNode() {
		if (!matchNoneSpaceChar('/')) {
			return null;
		}
		Step step = getStep();
		if (this.index == this.inputXPath.length()) {		// exact match
			return step;
		} else {
			return null;
		}
	}
	/*
	 * "XML elements must follow these naming rules:
    	Element names are case-sensitive
    	Element names must start with a letter or underscore
    	Element names cannot start with the letters xml (or XML, or Xml, etc)
    	Element names can contain letters, digits, hyphens, underscores, and periods
    	Element names cannot contain spaces ???????
		Any name can be used, no words are reserved (except xml)."
	 */
	private Step getStep() {			// return null is invalid
		// to the end of expression	
		Step step = new Step();
		String name = getElementName();
		if (name == null) {
			return null;
		} 
		step.elementName = name;	
		step.predicates = getPrecidates();
		if (step.predicates == null) {
			return null;
		}
		if (peekNextNoneSpaceChar() == null || peekNextNoneSpaceChar() == ']') {		// teminate
			return step;
		}
		step.next = getNode();
		if (step.next == null) {
			return null;
		}
		return step;
	}
	
	private List<Predicate> getPrecidates() {
		List<Predicate> ret = new ArrayList<Predicate>();
		Stack<Character> st = new Stack<>();
		if (!peekNoneSpaceChar('[')) {
			return ret;
		}
		st.push(getChar());
		while (!st.empty()) {
			st.pop();
			skipSpaces();
			Predicate predicate = new Predicate();
			int curIndex = this.index;
			if ((predicate.step = getStep()) != null) {
				predicate.type = 0;
				if (!matchNoneSpaceChar(']'))	return null;
				if (peekNoneSpaceChar('[')) {
					st.push(getChar());
				}
				ret.add(predicate);
				continue;
			} 
			setIndexPosition(curIndex);
			if ((predicate.text = getText()) != null) {
				predicate.type = 1;
				if (!matchNoneSpaceChar(']'))	return null;
				if (peekNoneSpaceChar('[')) {
					st.push(getChar());
				}
				ret.add(predicate);
				continue;
			} 
			setIndexPosition(curIndex);
			if ((predicate.contains = getContains()) != null) {		
				predicate.type = 2;
				if (!matchNoneSpaceChar(']'))	return null;
				if (peekNoneSpaceChar('[')) {
					st.push(getChar());
				}
				ret.add(predicate);
				continue;
			} 
			setIndexPosition(curIndex);
			if ((predicate.attribute = getAttr()) != null) {
				predicate.type = 3;
				if (!matchNoneSpaceChar(']'))	return null;
				if (peekNoneSpaceChar('[')) {
					st.push(getChar());
				}
				ret.add(predicate);
				continue;
			} else {
				return null;
			}
			
		}
		return ret;
	}
	
	private boolean matchNoneSpaceChar(char expected) {
		if (peekNextNoneSpaceChar() == null || getChar() != expected) {
			return false;
		} else {
			return true;
		}
	}
	
	private boolean peekNoneSpaceChar(char expected) {
		if (peekNextNoneSpaceChar() == null || peekNextChar() != expected) {
			return false;
		} else {
			return true;
		}
	}
	
	private Attr getAttr() {
		if (!matchNoneSpaceChar('@')) {			//check "@"
			return null;
		}
		skipSpaces();
		StringBuilder sb = new StringBuilder();
		Attr attribute = new Attr();
		while (peekNextChar() != null && peekNextChar() != '=' && peekNextChar() != ' ' && isValidChar(peekNextChar(), false)) {	//check start "@attname"
			sb.append(getChar());
		}
		attribute.attrName = sb.toString();
		if (attribute.attrName.length() == 0) {
			return null;
		}
		if (!matchNoneSpaceChar('=')) {			//check "="
			return null;
		}
		if (!matchNoneSpaceChar('\"')) {			//check "\""
			return null;
		}
		sb.setLength(0);
		while (peekNextChar() != '\"' && peekNextChar() != null ) {
			sb.append(getChar());
		}
		if (matchNoneSpaceChar('\"')) {			// check "\""
			attribute.attrValue = sb.toString();
			return attribute;
		} else {
			return null;
		}
	}
	
	private String getText() {
		skipSpaces();
		StringBuilder sb = new StringBuilder();
		while (peekNextChar() != null && peekNextChar() != '(' && isValidChar(peekNextChar(), false)) {	//check start "text()"
			sb.append(getChar());
		}
		if (!sb.toString().startsWith(TEXT)) {		
			return null;
		}
		if (!matchNoneSpaceChar('(')) {			//check "("
			return null;
		}
		if (!matchNoneSpaceChar(')')) {			//check ")"
			return null;
		}
		if (!matchNoneSpaceChar('=')) {			//check "="
			return null;
		}
		if (!matchNoneSpaceChar('\"')) {			//check "\""
			return null;
		}
		sb.setLength(0);
		while (peekNextChar() != '\"' && peekNextChar() != null ) {
			sb.append(getChar());
		}
		if (matchNoneSpaceChar('\"')) {			// check "\""
			return sb.toString();
		} else {
			return null;
		}
	}
	
	private String getContains() {
		skipSpaces();
		StringBuilder sb = new StringBuilder();
		while (peekNextChar() != null && peekNextChar() != '(' && isValidChar(peekNextChar(), false)) {	//check start "contains()"
			sb.append(getChar());
		}
		if (!sb.toString().startsWith(CONTAINS)) {		
			return null;
		}
		if (!matchNoneSpaceChar('(')) {			//check "("
			return null;
		}
		skipSpaces();
		sb.setLength(0);
		while (peekNextChar() != null && peekNextChar() != '(' && isValidChar(peekNextChar(), false)) {	//check start "text()"
			sb.append(getChar());
		}
		if (!sb.toString().startsWith(TEXT)) {		
			return null;
		}
		if (!matchNoneSpaceChar('(')) {			//check "("
			return null;
		}
		if (!matchNoneSpaceChar(')')) {			//check ")"
			return null;
		}
		if (!matchNoneSpaceChar(',')) {			//check ","
			return null;
		}
		if (!matchNoneSpaceChar('\"')) {			//check "\""
			return null;
		}
		sb.setLength(0);
		while (peekNextChar() != '\"' && peekNextChar() != null ) {
			sb.append(getChar());
		}
		if (!matchNoneSpaceChar('\"')) {			// check "\""
			return null;
		} 
		if (!matchNoneSpaceChar( ')')) {		// check ")"
			return null;
		}
		return sb.toString();
	}
	
	
	// get the name of element
	private String getElementName() {
		Character c = peekNextNoneSpaceChar();
		//invalid start char
		if (c == null || !Character.isLetter(c) && c != '_') {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		while (isValidChar(peekNextChar(), false)) {
			sb.append(getChar());
		}
		skipSpaces();
		if (isSeparator(peekNextChar())) {		// do not consume separator
			if (sb.length() >= 3 && "xml".equals(sb.substring(0, 3).toLowerCase())) {
				return null;
			}
			return sb.toString();
		} else {
			return null;
		}
	}
	
	// check if a character is valid
	private boolean isValidChar(Character c, boolean space) {
		if (c == null) {
			return false;
		}
		if (Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_' || c == '.'
				|| (space && c == ' ')) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isSeparator(Character c) {

		if (c == null || c == '/' || c == ' ' || c == '[' || c == ']') {
			return true;
		} else {
			return false;
		}
	}
	
	private Character peekNextNoneSpaceChar() {
		skipSpaces();
		return peekNextChar();
	}
	
	// not forward index pointer
	private Character peekNextChar() {
		if (this.index >= this.inputXPath.length()) {
			return null;
		}
		char c = inputXPath.charAt(index);
		return c;
	}
	
	// get next char
	private Character getChar() {
		if (this.index >= this.inputXPath.length()) {
			return null;
		}
		char c = inputXPath.charAt(index++);
		return c;
	}
	
	// skip spaces
	private void skipSpaces() {
		while (peekNextChar() != null && peekNextChar() == ' ') {
			getChar();
		}
	}
	
	// set index position
	private void setIndexPosition(int position) {
		this.index = position;
	}
	
}
