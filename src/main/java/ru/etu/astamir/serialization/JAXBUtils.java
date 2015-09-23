package ru.etu.astamir.serialization;

import com.google.common.base.Preconditions;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Artem Mon'ko
 */
public class JAXBUtils {


    public static Document jaxbTojdom(org.w3c.dom.Document document) {
        DOMBuilder jdb = new DOMBuilder();
        return jdb.build(document);
    }

	public static Element fromString(String string) throws JDOMException {
		Preconditions.checkNotNull(string);
		try {
			SAXBuilder builder = new SAXBuilder();
			return builder.build(new StringReader(string)).getRootElement();
		} catch (IOException e) {
			throw new JDOMException("Parse failed due to some I/O exception. That should not have happened since we're reading string.", e);
		}
	}

    public static org.w3c.dom.Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.newDocument();
    }
}

