package com.nucleus.core.xml.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;

public class FastXmlDoc implements Cloneable {
    /**
     *  Timestamp constant.
     */
    public static final String RFC_TIMESTAMP_FORMAT = "yyyy-MM-dd\\THH:mm:ssZ";

    /**
     * Internal Document instance.
     */
    private Document           doc;

    /**
     * The pointer to the root node in our document instance.
     */
    private Node               root;

    /**
     * Current FastXmlManipulator selection list.
     */
    private Node[]             list;

    /**
     * The current index in the selection list.
     */
    private int                index;

    /**
     * Whether the document is currently exclusive.
     * TODO: elaborate on the above.
     */
    private boolean            exclusive;

    /**
     * Quick access to an empty FastXmlManipulator.
     */
    public static FastXmlDoc   empty                = new FastXmlDoc();

    /**
     * Converts a NodeList instance into a Node array.
     * 
     * @param list The NodeList to convert.
     * @return Array of Node instances.
     */
    public static Node[] newListNode(NodeList list) {
        int length = (list == null) ? 0 : list.getLength();

        Node[] result = new Node[length];

        for (int i = 0 ; i < length ; i++) {
            result[i] = list.item(i);
        }

        return result;
    }

    /**
     * Recursively compares two XmlNode instances. Immediately returns false upon finding the first differnece between the two nodes.
     * 
     * @param left The left node.
     * @param right The right node.
     * @return Returns true if the two nodes match, false otherwise.
     */
    private static boolean compareNode(Node left, Node right) {

        // Compare node types
        if (left.getNodeType() != right.getNodeType()) {
            return false;
        }

        // Check if values need to be compared
        switch (left.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.TEXT_NODE:
                if (null!=left.getNodeValue() && null!=right.getNodeValue() && !left.getNodeValue().equals(right.getNodeValue())) {
                    return false;
                }
                break;
        }

        // Check if the names need to be compared
        switch (left.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.ELEMENT_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
                if (null!= left.getNamespaceURI() &&  null!= right.getNamespaceURI() && !left.getNamespaceURI().equals(right.getNamespaceURI())) {
                    return false;
                }
                if (null!=left.getNodeName() && null!=right.getNodeName() && !left.getNodeName().equals(right.getNodeName())) {
                    return false;
                }
        }

        // Check if attributes need to be compared
        switch (left.getNodeType()) {
            case Node.ELEMENT_NODE:
                NamedNodeMap leftAttr = left.getAttributes();
                NamedNodeMap rightAttr = right.getAttributes();

                if (leftAttr == null && rightAttr == null) {
                    break;
                }

                if (leftAttr == null || rightAttr == null) {
                    return false;
                }

                if (leftAttr.getLength() != rightAttr.getLength()) {
                    return false;
                }

                for (int i = 0, end = leftAttr.getLength() ; i < end ; i++) {
                    if (!compareNode(leftAttr.item(i), rightAttr.item(i))) {
                        return false;
                    }
                }
        }

        // Check if child nodes need to be compared
        switch (left.getNodeType()) {
            case Node.ELEMENT_NODE:
            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
                NodeList leftChildren = left.getChildNodes();
                NodeList rightChildren = right.getChildNodes();

                if (leftChildren == null && rightChildren == null) {
                    break;
                }

                if (leftChildren == null || rightChildren == null) {
                    return false;
                }

                if (leftChildren.getLength() != rightChildren.getLength()) {
                    return false;
                }

                for (int i = 0, end = leftChildren.getLength() ; i < end ; i++) {
                    if (!compareNode(leftChildren.item(i), rightChildren.item(i))) {
                        return false;
                    }
                }
        }

        return true;
    }

    /**
     * Helper function to create a new Document instance.
     * 
     * @return A new, empty Document instance.
     * @throws ParserConfigurationException 
     */
    public static Document getNewDocument() {
        Document doc = null;

        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new SystemException("could not create Document instance");
        }

        return doc;
    }

    /**
     * Creates a new FastXmlManipulator instance with the given root tag.
     * 
     * @param tag Root tag of the new XML document.
     */
    public FastXmlDoc(String tag) {
        this(tag, null);
    }

    /**
     * Creates a new FastXmlManipulator instance with the given root tag and the given implicit namespace.
     * 
     * @param tag Root tag of the new XML document. Cannot be null.
     * @param ns Tag xml namespace.
     */
    public FastXmlDoc(String tag, String ns) {
        // Create the new document
        doc = getNewDocument();

        // Initialize the document
        if (ns != null && !ns.isEmpty()) {
            doc.appendChild(doc.createElementNS(ns, tag));
        } else {
            doc.appendChild(doc.createElement(tag));
        }

        // Set up the list
        list = new Node[] { doc.getDocumentElement() };
        index = 0;

        // Set the root node
        root = getCurrentNode();
    }

    /**
     * Creates a new FastXmlManipulator instance from an existing Document instance.
     * 
     * @param doc Document instance with a root element. Cannot be null.
     */
    public FastXmlDoc(Document doc) {
        // Make sure they passed a doc
        if (doc == null) {
            throw new IllegalArgumentException("doc");
        }

        // Make sure it has a root node
        if (doc.getDocumentElement() == null) {
            throw new IllegalArgumentException("Document instance does not have a root element.");
        }

        // Set up the new doc
        initialize(new Node[] { doc.getDocumentElement() }, 0, doc.getDocumentElement());
    }

    /**
     * Creates a new FastXmlManipulator instance from an existing, non-empty FastXmlManipulator instance.
     * 
     * @param doc Non-empty FastXmlManipulator instance. Cannot be null.
     */
    public FastXmlDoc(FastXmlDoc doc) {
        // Make sure the doc isn't empty or null
        if (doc == null || doc.isEmpty()) {
            throw new IllegalArgumentException("doc");
        }

        // Get the doc as a node
        Node node = doc.asNode();

        // Initialize the new doc
        initialize(new Node[] { node }, 0, node);
    }

    /**
     * Protected constructor for FastXmlManipulator inheritors for creating a new instance with the state of the old instance.
     * 
     * @param list List of nodes for the current cursor into the Document.
     * @param index Index into the list of nodes providing the cursor pointer.
     * @param root Root of the FastXmlManipulator instance.
     */
    private FastXmlDoc(Node[] list, int index, Node root) {
        initialize(list, index, root);
    }

    /**
     * Internal constructor that creates an empty, non-usable doc. 
     */
    private FastXmlDoc() {
    }

    /**
     * Protected initializer for FastXmlManipulator inheritors for setting the state of an existing instance.
     * 
     * @param list List of nodes for the current cursor into the Document.
     * @param index Index into the list of nodes providing the cursor pointer.
     * @param root Root of the FastXmlManipulator instance.
     */
    private void initialize(Node[] list, int index, Node root) {
        if (list == null) {
            throw new IllegalArgumentException("list is null");
        }

        if (list.length == 0) {
            throw new IllegalArgumentException("list has no nodes");
        }

        if (index < 0 || index >= list.length) {
            throw new IndexOutOfBoundsException("index is out of range");
        }

        this.list = list;
        this.index = index;
        this.root = (root != null) ? root : list[index];
        this.doc = this.root.getOwnerDocument();
    }

    /**
     * Returns the FastXmlManipulator at the given index of the root FastXmlManipulator or an empty instance if the index is out of bounds.
     * 
     * @param index Index of the child instance of the root FastXmlManipulator instance.
     * @return
     */
    public FastXmlDoc at(int index) {
        if (!isEmpty() && index >= 0 && index < root.getChildNodes().getLength()) {
            return new FastXmlDoc(newListNode(root.getChildNodes()), index, null);
        }

        return empty;
    }

    /**
     * Returns a new rooted FastXmlManipulator instance based on the supplied XPath. The selection starts at the first result.
     * 
     * @param path XPath 1.0 expression to select FastXmlManipulator instances in the current FastXmlManipulator instance.
     * @return
     */
    public FastXmlDoc at(String path) {
        // Simple lookup marker
        boolean simple = true;

        // OPTIMIZATION: check if xpath is just looking for a specific attribute or element
        for (int i = 0 ; i < path.length() ; i++) {
            char c = path.charAt(i);
            if (!((i == 0 && c == '@') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || (c == '-') || (c == '.'))) {
                simple = false;
                break;
            }
        }

        // If it's a simple lookup, do the lookup!
        if (simple) {
            // Get the current node
            Element element = (Element) getCurrentNode();

            // Process it if we have a node
            if (element != null) {
                // Check for attributes or elements
                if (path.charAt(0) == '@') {
                    // Get the attribute
                    Node child = element.getAttributeNode(path.substring(1));

                    // If we got a match, return it
                    if (child != null) {
                        return new FastXmlDoc(new Node[] { child }, 0, null);
                    }
                } else {
                    // Create a container for matching children elements
                    ArrayList<Node> children = new ArrayList<Node>();

                    // Iterate through all children looking for matches
                    for (Node child = element.getFirstChild() ; child != null ; child = child.getNextSibling()) {
                        // We got a match
                        if (child instanceof Element && child.getNodeName().equals(path)) {
                            children.add(child);
                        }
                    }

                    // Return a new doc with the matches if anything actually matched
                    if (children.size() > 0) {
                        return new FastXmlDoc(children.toArray(new Node[children.size()]), 0, null);
                    }
                }
            }

            // Nothing matched, return an empty doc
            return empty;
        }

        // Return a full-blow xpath search
        return atPath(path);
    }

    /**
     * Returns a new rooted FastXmlManipulator instance based on the supplied XPath. The selection starts at the given index position.
     * 
     * @param path XPath 1.0 expression to select FastXmlManipulator instances in the current FastXmlManipulator instance.
     * @return
     */
    public FastXmlDoc atPath(String path) {
        // Make sure we're given a path
        if (path == null) {
            throw new IllegalArgumentException("path");
        }

        // Do a lookup if the path isn't empty
        if (!path.isEmpty()) {
            ArrayList<Node> list = new ArrayList<Node>();

            // Create the xpath instance
            XPath xpath = XPathFactory.newInstance().newXPath();

            // Do the search
            NodeList results;

            try {
                results = (NodeList) xpath.evaluate(path, root, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                BaseLoggers.exceptionLogger.error("XPathExpressionException while evaluating:" + path);
                return empty;
            }

            // Process results
            for (int i = 0, end = results.getLength() ; i < end ; i++) {
                list.add(results.item(i));
            }

            // Return a new doc if we got results
            if (list.size() > 0) {
                return new FastXmlDoc(list.toArray(new Node[list.size()]), 0, null);
            }
        }

        return empty;
    }

    public FastXmlDoc at(Node node) {
        // Make sure we got a node
        if (node == null) {
            throw new IllegalArgumentException("node");
        }

        // Check for empty docs
        if (isEmpty()) {
            return empty;
        }

        // Make sure the node belongs to the document
        if (doc != node.getOwnerDocument()) {
            return empty;
        }

        return new FastXmlDoc(new Node[] { node }, 0, null);
    }

    /**
     * Returns the textual contents of the current node.
     * 
     * @return
     */
    public String getContents() {
        if (doc != null) {
            Node current = getCurrentNode();

            // Check for elements with only a text value
            if (current instanceof Element && current.hasChildNodes() && current.getChildNodes().getLength() == 1
                    && current.getChildNodes().item(0) instanceof Text) {
                return current.getChildNodes().item(0).getNodeValue();
            }

            // Check for text blocks or attributes
            if (current instanceof Text || current instanceof Attr) {
                return current.getNodeValue();
            }

            // Render the current xml tree
            return getInnerXml(current);
        }

        return "";
    }

    /**
     * Helper function to get the output of the inner xml of the current node.
     * Taken from: http://stackoverflow.com/questions/3300839/get-a-nodes-inner-xml-as-string-in-java-dom
     * 
     * @param node The Node instance to render.
     * @return The string representation of the inner xml of the current node.
     */
    private String getInnerXml(Node node) {
        DOMImplementationLS lsImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation()
                .getFeature("LS", "3.0");
        LSSerializer lsSerializer = lsImpl.createLSSerializer();
        NodeList childNodes = node.getChildNodes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < childNodes.getLength() ; i++) {
            sb.append(lsSerializer.writeToString(childNodes.item(i)));
        }
        return sb.toString();
    }

    /**
     * Returns an FastXmlManipulator selection that includes all child elements.
     * 
     * @return
     */
    public FastXmlDoc getElements() {
        if (doc != null) {
            return at("*");
        }
        return empty;
    }

    /**
     * Returns the first FastXmlManipulator instance of the selection.
     * 
     * @return
     */
    public FastXmlDoc getFirst() {
        if (!isEmpty() && list != null) {
            return new FastXmlDoc(list, 0, null);
        }

        return empty;
    }

    /**
     * Returns the next FastXmlManipulator instance in the selection.
     * 
     * @return
     */
    public FastXmlDoc getNext() {
        if (!isEmpty() && list != null) {
            if (index < list.length - 1) {
                return new FastXmlDoc(list, index + 1, null);
            }
        }

        return empty;
    }

    /**
     * Returns true if the FastXmlManipulator instance is empty.
     * 
     * @return
     */
    public boolean isEmpty() {
        return (doc == null);
    }

    /**
     * Returns true if the FastXmlManipulator instance is a text node.
     * 
     * @return
     */
    public boolean isText() {
        return (getCurrentNode() instanceof Text);
    }

    /**
     * Returns the element or attribute name of the current FastXmlManipulator instance.
     * 
     * @return
     */
    public String getName() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("The FastXmlManipulator is empty.");
        }
        return getCurrentNode().getNodeName();
    }

    /**
     * Returns the current node.
     * 
     * @return
     */
    public Node getCurrentNode() {
        if (doc != null) {
            return (list != null) ? list[index] : doc;
        }
        return null;
    }

    /**
     * Returns the element or attribute namespace URI of the current FastXmlManipulator instance.
     * 
     * @return
     */
    public String getNamespaceURI() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("The FastXmlManipulator is empty.");
        }
        return getCurrentNode().getNamespaceURI();
    }

    /**
     * Returns the element or attribute prefix of the current FastXmlManipulator instance.
     * 
     * @return
     */
    public String getPrefix() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("The FastXmlManipulator is empty.");
        }
        return getCurrentNode().getPrefix();
    }

    /**
     * Return the Node wrapped by the FastXmlManipulator instance. Use with caution and only if absolutely necessary.
     * 
     * @return
     */
    public Node asNode() {
        return getCurrentNode();
    }

    /**
     * Returns the value of the node for certain node types, or null.
     * 
     * @param node The node to get the value of.
     * @return
     */
    private String getNodeText(Node node) {
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.ATTRIBUTE_NODE:
                return node.getNodeValue();
        }

        return null;
    }

    /**
     * Returns the value of all contained text nodes including nested ones.
     * 
     * @return
     */
    public String asInnerText() {
        if (isEmpty()) {
            return null;
        }
        return getCurrentNode().getTextContent();
    }

    /**
     * Returns the value of all contained text nodes that are immediate children.
     * 
     * @return
     */
    public String asText() {
        // Check for an empty doc
        if (isEmpty()) {
            return null;
        }

        // Get the current node
        Node current = getCurrentNode();

        // Do stuff for elements
        if (current instanceof Element) {
            // Return an empty string if there are no children
            if (!current.hasChildNodes()) {
                return "";
            }

            // Get the child nodes
            NodeList children = current.getChildNodes();

            // Get the number of child nodes
            int count = children.getLength();

            // Check for exactly one text/cdata node
            if (count == 1) {
                return getNodeText(current.getChildNodes().item(0));
            }

            // Concatenate all consecutive text nodes
            StringBuilder result = new StringBuilder();
            for (int i = 0 ; i < count ; i++) {
                String text = getNodeText(children.item(i));
                if (text == null) {
                    break;
                }
                result.append(text);
            }

            return result.toString();
        }

        return getNodeText(current);
    }

    /**
     * Returns the contents as boolean or null if contents could not be converted.
     * 
     * @return
     */
    public boolean asBoolean() {
        return as(boolean.class);
    }

    /**
     * Returns the contents as byte or null if contents could not be converted.
     * 
     * @return
     */
    public byte asByte() {
        return as(byte.class);
    }

    /**
     * Returns the contents as signed byte or null if contents could not be converted.
     * 
     * @return
     */
    public short asShort() {
        return as(short.class);
    }

    /**
     * Returns the contents as integer or null if contents could not be converted.
     * 
     * @return
     */
    public int asInt() {
        return as(int.class);
    }

    /**
     * Returns the contents as long integer or null if contents could not be converted.
     * 
     * @return
     */
    public long asLong() {
        return as(long.class);
    }

    /**
     * Returns the contents as floating-point number or null if contents could not be converted.
     * 
     * @return
     */
    public float asFloat() {
        return as(float.class);
    }

    /**
     * Returns the contents as double floating-point number or null if contents could not be converted.
     * 
     * @return
     */
    public double asDouble() {
        return as(double.class);
    }

    /**
     * Returns the contents as date/time or null if contents could not be converted.
     * 
     * @return
     */
    public Date asDate() {
        if (isEmpty()) {
            return null;
        }
        try {
            return DateFormat.getInstance().parse(getContents());
        } catch (ParseException e) {
            BaseLoggers.exceptionLogger.error("ParseException while parsing:" + getContents());
            return null;
        }
    }

    /**
     * Generic type conversion for the asX() family of methods.
     * 
     * @param type The type to convert to.
     * @return
     */
    public <T> T as(Class<T> type) {
        if (isEmpty()) {
            return null;
        }
        return type.cast(getContents());
    }

    /**
     * Returns the parent FastXmlManipulator instance or Empty if none exists.
     * 
     * @return
     */
    public FastXmlDoc getParent() {
        // Do nothing if the doc is empty
        if (isEmpty()) {
            return empty;
        }

        // Get the current node
        Node current = getCurrentNode();

        // Parent marker
        Node parent = null;

        // Get the parent node
        if (current instanceof Attr) {
            parent = ((Attr) current).getOwnerElement();
        } else {
            parent = current.getParentNode();
        }

        if (parent == null || parent instanceof Document) {
            return empty;
        }

        return at(parent);
    }

    /**
     * Returns the value of the language attribute.  Returns null if none is set.
     * 
     * @return
     */
    public String getLanguage() {
        return at("@xml:lang").asText();
    }

    /**
     * Sets the language attribute.  Null values remove it.
     * 
     * @param language
     */
    public void setLanguage(String language) {
        if (language != null) {
            attr("xml:lang", language);
        }
    }

    /**
     * Returns the root's element.
     * 
     * @return
     */
    private Element getRootElement() {
        if (root instanceof Document) {
            return ((Document) root).getDocumentElement();
        }
        return (Element) root;
    }

    /**
     * Check the element/attribute name of the current FastXmlManipulator.
     * 
     * @param name Name to compare with.
     * @return True if the name matches.
     */
    public boolean hasName(String name) {
        return (!isEmpty() && getName().equals(name));
    }

    /**
     * Check the element/attribute name of the current FastXmlManipulator.
     * 
     * @param name Name to compare with.
     * @param namespaceUri XML namespace to compare with.
     * @return True if the name and namespace match.
     */
    public boolean hasName(String name, String namespaceUri) {
        return (!isEmpty() && getName().equals(name) && getNamespaceURI().equals(namespaceUri));
    }

    /**
     * Check if current FastXmlManipulator contains named attribute.
     * 
     * @param name Name to compare with.
     * @return True if the name matches an attribute.
     */
    public boolean hasAttr(String name) {
        return (!isEmpty() && getCurrentNode().getAttributes() != null && getCurrentNode().getAttributes()
                .getNamedItem(name) != null);
    }

    /**
     * Check if current FastXmlManipulator contains named attribute.
     * 
     * @param name Name to compare with.
     * @param namespaceUri XML namespace to compare with.
     * @return True if the name matches an attribute.
     */
    public boolean hasAttr(String name, String namespaceUri) {
        return (!isEmpty() && getCurrentNode().getAttributes() != null && getCurrentNode().getAttributes().getNamedItemNS(
                name, namespaceUri) != null);
    }

    /**
     * Returns a deep clone of the FastXmlManipulator instance starting at the root FastXmlManipulator instance.
     */
    public FastXmlDoc clone() throws CloneNotSupportedException {
        if (isEmpty()) {
            return this;
        }

        Document doc;

        if (root instanceof Document) {
            if (exclusive) {
                exclusive = false;
                return this;
            }
            doc = (Document) root.getParentNode().cloneNode(false);
        } else {
            doc = getNewDocument();
            Node node = doc.importNode(root, true);
            doc.appendChild(node);
        }
        return new FastXmlDoc(doc);
    }

    /**
     * Mark the current FastXmlManipulator instance as exclusive.  This will skip the next Clone() operation when it occurs.
     * 
     * @return
     */
    public FastXmlDoc setExclusive() {
        if (!isEmpty()) {
            if (!(root.getParentNode() instanceof Document)) {
                throw new IllegalStateException("only the root node can be marked exclusive");
            }
            exclusive = true;
        }
        return this;
    }

    /**
     * Returns a shallow copy of current FastXmlManipulator instance.
     * 
     * @return Shallow copy of the FastXmlManipulator instance.
     */
    public FastXmlDoc copy() {
        if (isEmpty()) {
            return this;
        }
        return new FastXmlDoc(list, index, root);
    }

    /**
     * Add an attribute to the FastXmlManipulator instance.
     * 
     * @param tag Attribute name.
     * @param value Value of the attribute.
     * @return
     */
    public FastXmlDoc attr(String tag, String value) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure they gave us a tag
        if (tag == null) {
            throw new IllegalArgumentException("tag");
        }

        // Make sure they gave us a value
        if (value == null) {
            return this;
        }

        // Create the node
        Node node = doc.createAttribute(tag);

        // Set the value
        node.setNodeValue(value);

        // Add it to the doc
        getCurrentNode().getAttributes().setNamedItemNS(node);

        return this;
    }

    /**
     * Adds an attribute to the FastXmlManipulator instance.
     * 
     * @param tag Attribute name.
     * @param value Value of the attribute.
     * @return
     */
    public FastXmlDoc attr(String tag, Object value) {
        return attr(tag, value.toString());
    }

    /**
     * Adds an attribute to the FastXmlManipulator instance.
     * 
     * @param tag Attribute name.
     * @param value Date value of the attribute.
     * @return
     */
    public FastXmlDoc attr(String tag, Date value) {
        SimpleDateFormat formatter = new SimpleDateFormat(RFC_TIMESTAMP_FORMAT);
        return attr(tag, formatter.format(value));
    }

    /**
     * Starts a new child element. (e.g.<foo>)
     * 
     * @param tag Element name.
     * @return
     */
    public FastXmlDoc start(String tag) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure the tag isn't null
        if (tag == null) {
            throw new IllegalArgumentException("tag");
        }

        // Create the new node
        Node node = doc.createElement(tag);

        // Add it to the current node
        getCurrentNode().appendChild(node);

        // Reset the list and index
        list = new Node[] { node };
        index = 0;

        return this;
    }

    /**
     * Starts a new child element. (e.g. <foo>).
     * 
     * @param tag Element name.
     * @param namespaceUri Element XML namespace.
     * @return
     */
    public FastXmlDoc start(String tag, String namespaceUri) {
        // Make sure the namespace was given
        if (namespaceUri == null) {
            return start(tag);
        }

        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure the tag isn't null
        if (tag == null) {
            throw new IllegalArgumentException("tag");
        }

        // Create the new node
        Node node = doc.createElementNS(tag, namespaceUri);

        // Add it to the current node
        getCurrentNode().appendChild(node);

        // Reset the list and index
        list = new Node[] { node };
        index = 0;

        return this;
    }

    /**
     * Adds a complete child element..
     * 
     * @param tag Element name.
     * @return
     */
    public FastXmlDoc elem(String tag) {
        return start(tag).end();
    }

    /**
     * Adds a complete child element.
     * 
     * @param tag Element name.
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc elem(String tag, Object value) {
        // Make sure value isn't null
        if (value == null) {
            return this;
        }
        return start(tag).value(value).end();
    }

    /**
     * Adds a text node.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc value(String value) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure they gave us a value
        if (value == null) {
            return this;
        }

        // Create the node
        Node node = doc.createTextNode(value);

        // Add the node
        getCurrentNode().appendChild(node);

        return this;
    }

    /**
     * Adds a text node.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc value(Date value) {
        SimpleDateFormat formatter = new SimpleDateFormat(RFC_TIMESTAMP_FORMAT);
        return value(formatter.format(value));
    }

    /**
     * Adds a text node.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc value(Object value) {
        return value(value.toString());
    }

    /**
     * Replaces the text node with a new text node.
     * 
     * @param value Replacement value.
     * @return
     */
    public FastXmlDoc replaceValue(String value) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure the value isn't null
        if (value == null) {
            throw new IllegalArgumentException("value");
        }

        // Get the current node
        Node current = getCurrentNode();

        // Handle elements
        if (current instanceof Element) {
            Node node = current.getFirstChild();
            while (node != null) {
                Node next = node.getNextSibling();
                if (node instanceof Text) {
                    current.removeChild(node);
                }
                node = next;
            }
            return value(value);
        }

        // Handle text nodes
        if (current instanceof Text) {
            current.setNodeValue(value);
            return this;
        }

        // Handle attributes
        if (current instanceof Attr) {
            current.setNodeValue(value);
            return this;
        }

        // Unknown type
        throw new IllegalStateException("doc has no value");
    }

    /**
     * Replaces the text node with a new text node.
     * 
     * @param value Replacement value.
     * @return
     */
    public FastXmlDoc replaceValue(Object value) {
        return replaceValue(value.toString());
    }

    /**
     * Inserts a text or attribute node at the given XPath expression, creating elements as needed.
     * 
     * @param xpath XPath expression.
     * @param value Value to insert.
     * @return
     */
    public FastXmlDoc insertValueAt(String xpath, String value) {
        // Regex Pattern
        Pattern pattern = Pattern.compile("(.+)\\[(\\d+)\\]");

        // Make sure we have a path
        if (xpath == null) {
            throw new IllegalArgumentException("xpath");
        }

        // Make sure we have a value
        if (value == null) {
            return this;
        }

        // Create our cursor
        FastXmlDoc cursor = copy();

        // Check if the path is empty
        if (xpath.length() > 0) {
            // Split up the path
            String[] path = xpath.replaceAll("^/*|/*$", "").split("/");

            // Iterate through each path piece
            for (int i = 0 ; i < path.length ; i++) {
                String token = path[i];
                int index = -1;

                // Check if we find an index
                Matcher match = pattern.matcher(token);
                if (match.find()) {
                    index = Integer.parseInt(match.group(2)) - 1;
                    token = match.group(1);
                }

                // Sanity check token
                if (token.length() == 0) {
                    throw new IllegalArgumentException("token");
                }

                // Check if this is the last token in the path
                if (i == path.length - 1) {
                    // Check if the last token is an attribute
                    if (token.charAt(0) == '@') {
                        cursor.attr(token.substring(1), value);
                        return this;
                    }

                    // Always create the last token (unless it's a text node)
                    if (token.charAt(0) != '#') {
                        cursor = cursor.start(token);
                    }
                } else if (index >= 0) {
                    // Add as many nodes as required
                    int count = index;
                    FastXmlDoc current = cursor.at(token);

                    while (--count >= 0) {
                        if (current.isEmpty()) {
                            current = cursor.start(token).copy();
                            cursor.end();
                        }
                        current = current.getNext();
                    }

                    if (current.isEmpty()) {
                        current = cursor.start(token).copy();
                        cursor.end();
                    }

                    // Select token at index position
                    cursor = current;
                } else {
                    FastXmlDoc current = cursor.at(token + "[last()]");

                    // Add one node
                    if (current.isEmpty()) {
                        current = cursor.start(token).copy();
                        cursor.end();
                    }
                    cursor = current;
                }
            }
        }

        // Add value to the current position
        if (value.length() > 0) {
            cursor.value(value);
        }

        return this;
    }

    /**
     * Adds a CDATA section.
     * 
     * @param value Contents of the CDATA section.
     * @return
     */
    public FastXmlDoc cDataSection(String value) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure the value isn't empty
        if (value == null) {
            return this;
        }

        // Create the node
        Node node = doc.createCDATASection(value);

        // Add it to the document
        getCurrentNode().appendChild(node);

        return this;
    }

    /**
     * Adds a comment node.
     * 
     * @param value Comment text.
     * @return
     */
    public FastXmlDoc comment(String value) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure the value isn't empty
        if (value == null) {
            return this;
        }

        // Create the node
        Node node = doc.createComment(value);

        // Add it to the document
        node.appendChild(node);

        return this;
    }

    /**
     * Adds a conditional XML comment node.
     * 
     * @param condition Condition expression.
     * @param contents Comment text.
     * @return
     */
    public FastXmlDoc conditionalComment(String condition, FastXmlDoc contents) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure the condition isn't empty
        if (condition == null || condition.isEmpty()) {
            throw new IllegalStateException("condition");
        }

        // Make sure the contents aren't empty
        if (contents == null || contents.isEmpty()) {
            return this;
        }

        // Create the node
        Node node = doc.createComment(String.format("[%s]>%s<![endif]", condition, contents.getContents()));

        // Add it to the document
        node.appendChild(node);

        return this;
    }

    /**
     * Ends a child element. (e.g. </foo>)
     * 
     * @return
     */
    public FastXmlDoc end() {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check if we're already at the root
        if (getCurrentNode() == root) {
            throw new IllegalStateException("doc is at root position");
        }

        // Get the parent node
        Node parent = getCurrentNode().getParentNode();

        if (parent == doc) {
            list = null;
            index = -1;
        } else {
            list = new Node[] { parent };
            index = 0;
        }

        return this;
    }

    /**
     * Returns the root FastXmlManipulator instance, which is either the XmlDocument element or the original node returned by an XPath expression.
     * 
     * @return
     */
    public FastXmlDoc getRoot() {
        return new FastXmlDoc(list, index, root);
    }

    /**
     * Ends child elements until the current node is the same as the marker node.
     * 
     * @param markerNode Xml node to use a marker of where the document was before the target start(string) was called.
     * @return
     */
    public FastXmlDoc end(Node markerNode) {
        // Make sure it's not empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        Node currentNode = asNode();
        while (currentNode != markerNode) {
            end();
            currentNode = asNode();
        }

        return this;
    }

    /**
     * Ends all child elements until the root FastXmlManipulator instance is reached.
     * 
     * @return
     */
    public FastXmlDoc endAll() {
        // Make sure it's not empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Close tags until we reach the root
        while (getCurrentNode() != root) {
            end();
        }

        return this;
    }

    /**
     * Returns the outer xml of a node.
     * 
     * @param node The node to get the outer xml of.
     * @param decl Whether to include the declaration.
     * @return
     */
    private String getOuterXml(Node node, boolean decl, boolean indent) {
        // Create the transformer
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Exception while creating new Transformer instance");
            return "";
        }

        // Force xml
        transformer.setOutputProperty("method", "xml");

        // Declaration
        if (decl == false) {
            transformer.setOutputProperty("omit-xml-declaration", "yes");
        }

        // Indention
        if (indent) {
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }

        // Create the string writer
        StringWriter writer = new StringWriter();

        // Transform
        try {
            transformer.transform(new DOMSource(node), new StreamResult(writer));
        } catch (TransformerException e) {
            BaseLoggers.exceptionLogger.error("TransformerException while transforming:" + node);
            return "";
        }

        // Return the string result
        return writer.toString();
    }

    /**
     * Outputs the document from the current root as a string.
     * 
     * @return
     */
    public String toString() {
        return toString(false);
    }

    /**
     * Outputs the document from the root as a string.
     * 
     * @param decl Whether to include the declaration.  Only works from the root of the document.
     * @return
     */
    public String toString(boolean decl) {
        // Check for an empty doc
        if (isEmpty()) {
            return "";
        }

        // Render the outer xml
        return getOuterXml(root, (decl == true && root.getParentNode() == doc), false);
    }

    /**
     * Returns the document as a string with indention.
     * 
     * @return
     */
    public String toPrettyString() {
        return getOuterXml(root, true, true);
    }

    /**
     * Removes this FastXmlManipulator instance from the containing document.
     * 
     * @return
     */
    public FastXmlDoc remove() {
        // Only do work if the doc isn't empty
        if (!isEmpty()) {
            // Get the current node
            Node current = getCurrentNode();

            // Handle the correct type
            if (current instanceof Attr) {
                // Recast the attribute
                Attr attribute = (Attr) current;

                // Remove the attribute
                if (attribute.getOwnerElement() != null) {
                    attribute.getOwnerElement().removeAttributeNode(attribute);
                }
            } else if (current.getParentNode() != null) {
                // Remove the node
                current.getParentNode().removeChild(current);
            }

            // Reset the list
            list = null;
        }

        return empty;
    }

    /**
     * Removes an attriute.
     * 
     * @param name Name of the attribute to remove.
     * @return
     */
    public FastXmlDoc removeAttr(String name) {
        // Nothing to do for empty docs
        if (isEmpty()) {
            return this;
        }

        // Make sure the name was given
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        // Remove the attribute
        getCurrentNode().getAttributes().removeNamedItem(name);

        return this;
    }

    /**
     * Removes all FastXmlManipulator instances in current selection.
     */
    public void removeAll() {
        for (FastXmlDoc doc : toList()) {
            doc.remove();
        }
    }

    /**
     * Removes all child nodes.
     * 
     * @return
     */
    public FastXmlDoc removeNodes() {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            return this;
        }

        // Get the current node
        Node current = getCurrentNode();

        // Remove each node
        while (current.getFirstChild() != null) {
            current.removeChild(current.getFirstChild());
        }

        return this;
    }

    /**
     * Replaces this FastXmlManipulator instance with another one.
     * 
     * @param doc Replacement FastXmlManipulator instance.
     * @return
     */
    public FastXmlDoc replace(FastXmlDoc doc) {
        // Make sure the doc's not empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for blank docs
        if (doc == null || doc.isEmpty()) {
            return remove();
        }

        // Get the top element node of the document to add
        Element docnode = doc.getRootElement();

        // Check if current document has a namespace
        String defaultNS = this.doc.getDocumentElement().getNamespaceURI();
        String nodeNS = docnode.getNamespaceURI();

        if (defaultNS != null && defaultNS.length() > 0 && (nodeNS == null || nodeNS.length() == 0)) {
            docnode.setAttribute("xmlns", defaultNS);
        }

        // Import the node
        Node node = this.doc.importNode(docnode, true);

        // Replace the node
        return replaceCurrentNode(node);
    }

    /**
     * Helper function to implement C#'s insertAfter().
     * 
     * @param insert New node to insert.  Note that importing this is the responsibility of the caller.
     * @param ref Node to insert the new node AFTER.
     * @return The node being inserted
     */
    private Node insertAfter(Node parent, Node insert, Node ref) {
        // Make sure the parent exists
        if (parent == null) {
            throw new IllegalStateException("doc is at root");
        }

        // Sanity check the ref node
        if (ref != null && ref.getParentNode() != parent) {
            throw new IllegalArgumentException("ref is not a child of parent");
        }

        // Rules:
        // 1. If ref is null, insert into the front of the list.
        // 2. If ref has a sibling after it, insert before the next sibling.
        // 3. Ref is the last null, just append the node.
        if (ref == null) {
            parent.insertBefore(insert, parent.getFirstChild());
        } else if (ref.getNextSibling() != null) {
            parent.insertBefore(insert, ref.getNextSibling());
        } else {
            parent.appendChild(insert);
        }

        return insert;
    }

    /**
     * Replaces this FastXmlManipulator instance with the child nodes of another one.
     * 
     * @param doc Container of replacement nodes.
     * @return
     */
    public FastXmlDoc replaceWithNodes(FastXmlDoc doc) {
        // Check for empty doc
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for empty passed doc
        if (doc == null || doc.isEmpty() || doc.asNode().getFirstChild() == null) {
            return remove();
        }

        // Get the needed pieces
        Node importNode = doc.getRootElement().getFirstChild();
        Node insertNode = getCurrentNode();
        Node parentNode = insertNode.getParentNode();

        // Do the inserting
        while (importNode != null) {
            insertNode = insertAfter(parentNode, this.doc.importNode(importNode, true), insertNode);
            importNode = importNode.getNextSibling();
        }

        // Remove the current node
        parentNode.removeChild(getCurrentNode());

        return this.at(insertNode);
    }

    /**
     * Replaces this FastXmlManipulator instance with a text node.
     * 
     * @param value Value to replace with.
     * @return
     */
    public FastXmlDoc replace(String value) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // If the value's null, remove it
        if (value == null) {
            return remove();
        }

        // Replace the node
        return replaceCurrentNode(doc.createTextNode(value));
    }

    /**
     * Replaces this FastXmlManipulator instance with a text node.
     * 
     * @param value Value to replace with.
     * @return
     */
    public FastXmlDoc replace(Object value) {
        return replace(value.toString());
    }

    /**
     * Replaces this FastXmlManipulator instance with a text node.
     * 
     * @param value Value to replace with.
     * @return
     */
    public FastXmlDoc replace(Date value) {
        SimpleDateFormat formatter = new SimpleDateFormat(RFC_TIMESTAMP_FORMAT);
        return replace(formatter.format(value));
    }

    /**
     * Change the name of the current element node.
     * 
     * @param name New element name.
     * @return
     */
    public FastXmlDoc rename(String name) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Make sure the name was given
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        // If the names match, quit
        if (name.equals(getCurrentNode().getNodeName())) {
            return this;
        }

        // Get the current node
        Node current = getCurrentNode();

        // Create the new node
        Element node = current.getOwnerDocument().createElement(name);

        // Move over the attributes
        NamedNodeMap list = current.getAttributes();
        for (int i = 0 ; i < list.getLength() ; i++) {
            Attr item = (Attr) list.item(i);
            node.setAttribute(item.getNodeName(), item.getNodeValue());
        }

        // Move over child nodes
        for (Node child : newListNode(current.getChildNodes())) {
            node.appendChild(child);
        }

        // Replace the current node with the new node
        return replaceCurrentNode(node);
    }

    /**
     * Prepend child nodes from another FastXmlManipulator instance.
     * 
     * @param doc Container of prepended nodes.
     * @return
     */
    public FastXmlDoc addNodesInFront(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for empty doc
        if (doc == null) {
            return this;
        }

        // Make sure the root of the document is an element
        if (!(root instanceof Element)) {
            throw new IllegalStateException("doc is not an element");
        }

        // Do work if the new doc has content
        if (!doc.isEmpty()) {
            // Insertion marker
            Node insertNode = null;

            // Get child nodes
            NodeList nodes = doc.getRootElement().getChildNodes();

            // Add the new nodes
            for (int i = 0 ; i < nodes.getLength() ; i++) {
                insertNode = insertAfter(getCurrentNode(), this.doc.importNode(nodes.item(i), true), insertNode);
            }
        }

        return this;
    }

    /**
     * Replaces the current node with a new given node.
     * 
     * @param newNode New node.
     * @return
     */
    private FastXmlDoc replaceCurrentNode(Node newNode) {
        // Get the current node
        Node current = getCurrentNode();

        // Get the parent node
        Node parent = current.getParentNode();

        // Replace the nodes
        parent.replaceChild(newNode, current);

        // Reset the list
        list[index] = newNode;

        // If the root changed, update that as well
        if (current == root) {
            root = newNode;
            doc = root.getOwnerDocument();
        }

        return this;
    }

    /**
     * Converts the FastXmlManipulator selection into a list of FastXmlManipulator instances.
     * 
     * @return
     */
    public ArrayList<FastXmlDoc> toList() {
        // Return an empty list if the doc is empty
        if (isEmpty() || list == null) {
            return new ArrayList<FastXmlDoc>();
        }

        // Create the result array
        ArrayList<FastXmlDoc> result = new ArrayList<FastXmlDoc>(list.length);

        // Add each doc
        for (int i = 0 ; i < list.length ; i++) {
            result.add(new FastXmlDoc(list, i, null));
        }

        return result;
    }

    /**
     * Add child nodes from another FastXmlManipulator instance before this one.
     * 
     * @param doc Container of prepended nodes.
     * @return
     */
    public FastXmlDoc addNodesBefore(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for null doc
        if (doc == null) {
            return this;
        }

        // Make sure the root's an element
        if (!(root instanceof Element)) {
            throw new IllegalStateException("doc is not an element");
        }

        // Do work if the doc isn't empty
        if (!doc.isEmpty()) {
            // Get the parent node
            Node parent = getCurrentNode().getParentNode();

            // Check if the parent was null (i.e. we're the root element)
            if (parent == null) {
                throw new IllegalStateException("doc is top node");
            }

            // Create our marker node
            Node marker = parent.insertBefore(this.doc.createTextNode("#"), getCurrentNode());

            // Insert marker
            Node insertNode = marker;

            // Add each node
            for (Node node : newListNode(doc.getRootElement().getChildNodes())) {
                insertNode = insertAfter(parent, this.doc.importNode(node, true), insertNode);
            }

            // Remove the marker node
            parent.removeChild(marker);
        }

        return this;
    }

    /**
     * Add child nodes from another FastXmlManipulator instance after this one.
     * 
     * @param doc Container of prepended nodes.
     * @return
     */
    public FastXmlDoc addNodesAfter(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for null doc
        if (doc == null) {
            return this;
        }

        // Make sure the root's an element
        if (!(root instanceof Element)) {
            throw new IllegalStateException("doc is not an element");
        }

        // Do work if the doc isn't empty
        if (!doc.isEmpty()) {
            // Get the parent node
            Node parent = getCurrentNode().getParentNode();
            if (parent == null) {
                throw new IllegalStateException("doc is top node");
            }

            // Create our marker node
            Node insertNode = getCurrentNode();

            // Add each node
            for (Node node : newListNode(doc.getRootElement().getChildNodes())) {
                insertNode = insertAfter(parent, this.doc.importNode(node, true), insertNode);
            }
        }

        return this;
    }

    /**
     * Adds child nodes from another FastXmlManipulator instance.
     * 
     * @param doc Container of added nodes.
     * @return
     */
    public FastXmlDoc addNodes(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for null doc
        if (doc == null) {
            return this;
        }

        // Do work if the doc isn't empty
        if (!doc.isEmpty()) {
            // Add each node
            for (Node node : newListNode(doc.getRootElement().getChildNodes())) {
                getCurrentNode().appendChild(this.doc.importNode(node, true));
            }
        }

        return this;
    }

    /**
     * Adds an FastXmlManipulator instance.
     * 
     * @param doc FastXmlManipulator instance to add.
     * @return
     */
    public FastXmlDoc add(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for null doc
        if (doc == null) {
            return this;
        }

        // Do work if the doc isn't empty
        if (!doc.isEmpty()) {
            getCurrentNode().appendChild(this.doc.importNode(doc.root, true));
        }

        return this;
    }

    /**
     * Adds a value after this FastXmlManipulator instance.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc addAfter(String value) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for null value
        if (value == null || value.isEmpty()) {
            return this;
        }

        // Get the parent node
        Node parent = getCurrentNode().getParentNode();
        if (parent == null) {
            throw new IllegalStateException("doc is top node");
        }

        // Insert the node
        insertAfter(parent, doc.createTextNode(value), getCurrentNode());

        return this;
    }

    /**
     * Adds a value after this FastXmlManipulator instance.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc addAfter(Date value) {
        SimpleDateFormat formatter = new SimpleDateFormat(RFC_TIMESTAMP_FORMAT);
        return addAfter(formatter.format(value));
    }

    /**
     * Adds a value after this FastXmlManipulator instance.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc addAfter(Object value) {
        return addAfter(value.toString());
    }

    /**
     * Adds an FastXmlManipulator instance after this one.
     * 
     * @param doc FastXmlManipulator instance to add.
     * @return
     */
    public FastXmlDoc addAfter(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Get the parent node
        Node parent = getCurrentNode().getParentNode();
        if (parent == null) {
            throw new IllegalStateException("doc is top node");
        }

        // Do work if the doc isn't empty
        if (doc != null && !doc.isEmpty()) {
            insertAfter(parent, this.doc.importNode(doc.root, true), getCurrentNode());
        }

        return this;
    }

    /**
     * Adds a value before this FastXmlManipulator instance.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc addBefore(String value) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for null doc
        if (value == null || value.isEmpty()) {
            return this;
        }

        // Get the parent
        Node parent = getCurrentNode().getParentNode();

        // Make sure parent exists
        if (parent == null) {
            throw new IllegalStateException("doc is top node");
        }

        // Insert the node
        parent.insertBefore(doc.createTextNode(value), getCurrentNode());

        return this;
    }

    /**
     * Adds a value before this FastXmlManipulator instance.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc addBefore(Date value) {
        SimpleDateFormat formatter = new SimpleDateFormat(RFC_TIMESTAMP_FORMAT);
        return addBefore(formatter.format(value));
    }

    /**
     * Adds a value before this FastXmlManipulator instance.
     * 
     * @param value Value to add.
     * @return
     */
    public FastXmlDoc addBefore(Object value) {
        return addBefore(value.toString());
    }

    /**
     * Adds an FastXmlManipulator instance before this one.
     * 
     * @param doc FastXmlManipulator instance to add.
     * @return
     */
    public FastXmlDoc addBefore(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        // Check for null doc
        if (doc == null) {
            return this;
        }

        // Get the parent node
        Node parent = getCurrentNode().getParentNode();

        // Make sure parent exists
        if (parent == null) {
            throw new IllegalStateException("doc is top node");
        }

        // Insert the node
        parent.insertBefore(this.doc.importNode(doc.root, true), getCurrentNode());

        return this;
    }

    /**
     * Adds all FastXmlManipulator instances in selection to this one.
     * 
     * @param doc FastXmlManipulator selection.
     * @return
     */
    public FastXmlDoc addAll(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        if (doc != null) {
            for (FastXmlDoc item : doc.toList()) {
                add(item);
            }
        }

        return this;
    }

    /**
     * Adds all FastXmlManipulator instances in selection before this one.
     * 
     * @param doc FastXmlManipulator selection.
     * @return
     */
    public FastXmlDoc addAllBefore(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        if (doc != null) {
            for (FastXmlDoc item : doc.toList()) {
                addBefore(item);
            }
        }

        return this;
    }

    /**
     * Adds all FastXmlManipulator instances in selection after this one.
     * 
     * @param doc FastXmlManipulator selection.
     * @return
     */
    public FastXmlDoc addAllAfter(FastXmlDoc doc) {
        // Make sure the doc isn't empty
        if (isEmpty()) {
            throw new IllegalStateException("doc is empty");
        }

        if (doc == null) {
            return this;
        }

        // Reverse the list of nodes
        ArrayList<FastXmlDoc> list = new ArrayList<FastXmlDoc>();
        for (FastXmlDoc item : doc.toList()) {
            list.add(item);
        }
        Collections.reverse(list);

        // Add reversed list
        for (FastXmlDoc item : list) {
            addAfter(item);
        }

        return this;
    }

    /**
     * Returns the number of items in the FastXmlManipulator selection.
     * 
     * @return
     */
    public int length() {
        if (isEmpty()) {
            return 0;
        }

        if (list == null) {
            return 1;
        }

        return list.length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((root == null) ? 0 : root.hashCode());
        return result;
    }

    /**
     * Override comparison operators.
     * 
     * @param other Object to compare to this.
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof FastXmlDoc)) {
            return false;
        }
        return compareNode(root, ((FastXmlDoc) other).root);
    }

    /**
     * Loads the passed xml into a new FastXmlManipulator instance.
     * 
     * @param xml Xml string to parse.
     * @return The loaded FastXmlManipulator, or null on failure.
     */
    public static FastXmlDoc load(String xml) {
        // Check for empty xml input
        if (xml == null || xml.isEmpty()) {
            return empty;
        }

        // Create the document builder factory
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        // Create the document builder
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            BaseLoggers.exceptionLogger.error("ParserConfigurationException while creating new instance of DocumentBuilder");
            return empty;
        }

        // Clean up the input string
        xml = xml.trim().replaceFirst("^([\\W]+)<", "<");

        // Parse the input xml
        Document doc = null;
        try {
            doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            throw new InvalidDataException("Unparsable XML: " + xml);
        }

        // Load into a new doc
        return new FastXmlDoc(doc);
    }

    /**
     * Creates a single selection from multiple FastXmlManipulators.
     * 
     * @param documents List of FastXmlManipulators.
     * @return Combined FastXmlManipulators.
     */
    public static FastXmlDoc createSelection(FastXmlDoc[] documents) {
        if (documents == null) {
            throw new IllegalArgumentException("documents are null");
        }

        ArrayList<Node> list = new ArrayList<Node>();
        for (FastXmlDoc doc : documents) {
            if (!doc.isEmpty()) {
                list.add(doc.asNode());
            }
        }

        if (list.isEmpty()) {
            return empty;
        }

        return new FastXmlDoc(list.toArray(new Node[list.size()]), 0, null);
    }

    /**
     * Creates a single selection from multiple FastXmlManipulators.
     * 
     * @return empty, since this takes no params.
     */
    public static FastXmlDoc createSelection() {
        return empty;
    }
}
