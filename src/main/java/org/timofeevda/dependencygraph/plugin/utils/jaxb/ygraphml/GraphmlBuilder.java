package org.timofeevda.dependencygraph.plugin.utils.jaxb.ygraphml;


import com.sun.javafx.geom.Edge;
import com.yworks.xml.graphml.AlignmentType;
import com.yworks.xml.graphml.AutoSizePolicyType;
import com.yworks.xml.graphml.NodeLabelType;
import com.yworks.xml.graphml.ShapeNodeType;
import net.gexf._1.EdgeContent;
import net.gexf._1.EdgesContent;
import net.gexf._1.GexfContent;
import net.gexf._1.NodeContent;
import net.gexf._1.NodesContent;
import org.graphdrawing.graphml.xmlns.DataType;
import org.graphdrawing.graphml.xmlns.EdgeType;
import org.graphdrawing.graphml.xmlns.GraphEdgedefaultType;
import org.graphdrawing.graphml.xmlns.GraphType;
import org.graphdrawing.graphml.xmlns.GraphmlType;
import org.graphdrawing.graphml.xmlns.KeyForType;
import org.graphdrawing.graphml.xmlns.KeyType;
import org.graphdrawing.graphml.xmlns.NodeType;
import org.graphdrawing.graphml.xmlns.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.Optional;

/**
 * @author dtimofeev
 * @version 06.10.2015
 */
public class GraphmlBuilder {

    public static GraphmlType toGraphml(GexfContent gexfContent) {
        ObjectFactory graphmlFactory = new ObjectFactory();
        com.yworks.xml.graphml.ObjectFactory yGraphmlFactory = new com.yworks.xml.graphml.ObjectFactory();

        GraphmlType graphmlType = graphmlFactory.createGraphmlType();

        createNodeGraphicsKey(graphmlFactory, graphmlType);

        GraphType graphType = createGraphElement(graphmlFactory, graphmlType);

        Optional<NodesContent> nodesContent = gexfContent.getGraph().getAttributesOrNodesOrEdges().stream()
                .filter(NodesContent.class::isInstance)
                .map(NodesContent.class::cast)
                .findFirst();

        nodesContent.ifPresent(nodes -> nodes.getNode().stream()
                .filter(NodeContent.class::isInstance)
                .map(NodeContent.class::cast)
                .forEach(node -> GraphmlBuilder.createNodeElement(node, graphType, graphmlFactory, yGraphmlFactory)));

        Optional<EdgesContent> edgesContent = gexfContent.getGraph().getAttributesOrNodesOrEdges().stream()
                .filter(EdgesContent.class::isInstance)
                .map(EdgesContent.class::cast)
                .findFirst();

        edgesContent.ifPresent(edges -> edges.getEdge().stream()
                .filter(EdgeContent.class::isInstance)
                .map(EdgeContent.class::cast)
                .forEach(edge -> GraphmlBuilder.createEdgeElement(edge, graphType, graphmlFactory)));

        return graphmlType;
    }

    private static void createNodeGraphicsKey(ObjectFactory graphmlFactory, GraphmlType graphmlType) {
        KeyType keyType = graphmlFactory.createKeyType();
        keyType.setId("k1");
        keyType.setFor(KeyForType.NODE);
        keyType.setYfilesType("nodegraphics");
        graphmlType.getKey().add(keyType);
    }

    private static GraphType createGraphElement(ObjectFactory graphmlFactory, GraphmlType graphmlType) {
        GraphType graphType = graphmlFactory.createGraphType();
        graphType.setEdgedefault(GraphEdgedefaultType.DIRECTED);
        graphType.setId("G");
        graphmlType.getGraphOrData().add(graphType);
        return graphType;
    }

    private static void createNodeElement(NodeContent nodeContent, GraphType graphType, ObjectFactory graphmlFactory,
                                          com.yworks.xml.graphml.ObjectFactory yGraphmlFactory) {
        String id = nodeContent.getId();
        String label = nodeContent.getLabel();
        NodeType nodeType = graphmlFactory.createNodeType();
        nodeType.setId(id);

        DataType dataType = graphmlFactory.createDataType();
        dataType.setKey("k1");

        ShapeNodeType shapeNodeType = yGraphmlFactory.createShapeNodeType();
        NodeLabelType nodeLabelType = yGraphmlFactory.createNodeLabelType();
        nodeLabelType.setAlignment(AlignmentType.CENTER);
        nodeLabelType.setAutoSizePolicy(AutoSizePolicyType.CONTENT);
        nodeLabelType.setVisible(true);
        nodeLabelType.getContent().add(label);
        shapeNodeType.getNodeLabel().add(nodeLabelType);

        dataType.getContent().add(yGraphmlFactory.createShapeNode(shapeNodeType));
        nodeType.getDataOrPortOrGraph().add(dataType);

        graphType.getDataOrNodeOrEdge().add(nodeType);
    }

    private static void createEdgeElement(EdgeContent edgeContent, GraphType graphType, ObjectFactory graphmlFactory) {
        String source = edgeContent.getSource();
        String target = edgeContent.getTarget();

        EdgeType edgeType = graphmlFactory.createEdgeType();
        edgeType.setSource(source);
        edgeType.setTarget(target);

        graphType.getDataOrNodeOrEdge().add(edgeType);
    }

    public static void main(String... args) throws JAXBException, FileNotFoundException {
        ObjectFactory graphmlFactory = new ObjectFactory();
        com.yworks.xml.graphml.ObjectFactory yGraphmlFactory = new com.yworks.xml.graphml.ObjectFactory();
        GraphmlType graphmlType = graphmlFactory.createGraphmlType();

        KeyType keyType = graphmlFactory.createKeyType();
        keyType.setId("k1");
        keyType.setFor(KeyForType.NODE);
        keyType.setYfilesType("nodegraphics");

        GraphType graphType = graphmlFactory.createGraphType();
        graphType.setEdgedefault(GraphEdgedefaultType.DIRECTED);
        graphType.setId("G");

        NodeType nodeType = graphmlFactory.createNodeType();
        nodeType.setId("n1");
        graphType.getDataOrNodeOrEdge().add(nodeType);
        DataType dataType = graphmlFactory.createDataType();
        dataType.setKey("k1");

        ShapeNodeType shapeNodeType = yGraphmlFactory.createShapeNodeType();
        NodeLabelType nodeLabelType = yGraphmlFactory.createNodeLabelType();
        nodeLabelType.setAlignment(AlignmentType.CENTER);
        nodeLabelType.setAutoSizePolicy(AutoSizePolicyType.CONTENT);
        nodeLabelType.setVisible(true);
        nodeLabelType.getContent().add("Opa");
        shapeNodeType.getNodeLabel().add(nodeLabelType);

        dataType.getContent().add(yGraphmlFactory.createShapeNode(shapeNodeType));
        nodeType.getDataOrPortOrGraph().add(dataType);

        graphmlType.getKey().add(keyType);
        graphmlType.getGraphOrData().add(graphType);

        JAXBContext jc = JAXBContext.newInstance(GraphmlType.class, ShapeNodeType.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(graphmlFactory.createGraphml(graphmlType), new FileOutputStream("grp.graphml"));


    }
}
