package org.timofeevda.dependencygraph.plugin.utils.jaxb.gexf;

import net.gexf._1.EdgeContent;
import net.gexf._1.EdgesContent;
import net.gexf._1.GexfContent;
import net.gexf._1.GraphContent;
import net.gexf._1.NodeContent;
import net.gexf._1.NodesContent;
import net.gexf._1.ObjectFactory;

import java.util.Collection;

/**
 * Wrapps up Jaxb object factory
 *
 * @author timofeevda
 */
public class GexfElementFactory {

    static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public static NodeContent createNode(String id, String label) {
        NodeContent node = OBJECT_FACTORY.createNodeContent();
        node.setId(id);
        node.setLabel(label);
        return node;
    }

    public static EdgeContent createEdge(String id, String source, String target) {
        EdgeContent edge = OBJECT_FACTORY.createEdgeContent();
        edge.setId(id);
        edge.setSource(source);
        edge.setTarget(target);
        return edge;
    }

    public static GexfContent createGEXF(Collection<NodeContent> nodesCollection, Collection<EdgeContent> edgesCollection) {
        GexfContent gexf = OBJECT_FACTORY.createGexfContent();

        GraphContent graph = OBJECT_FACTORY.createGraphContent();

        NodesContent nodes = OBJECT_FACTORY.createNodesContent();
        nodes.getNode().addAll(nodesCollection);

        EdgesContent edges = OBJECT_FACTORY.createEdgesContent();
        edges.getEdge().addAll(edgesCollection);

        graph.getAttributesOrNodesOrEdges().add(nodes);
        graph.getAttributesOrNodesOrEdges().add(edges);

        gexf.setGraph(graph);

        return gexf;
    }
}
