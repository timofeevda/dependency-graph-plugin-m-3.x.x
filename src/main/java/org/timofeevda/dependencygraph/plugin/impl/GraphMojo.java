package org.timofeevda.dependencygraph.plugin.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.gexf._1.EdgeContent;
import net.gexf._1.EdgesContent;
import net.gexf._1.GexfContent;
import net.gexf._1.NodeContent;
import net.gexf._1.NodesContent;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.timofeevda.dependencygraph.plugin.utils.jaxb.gexf.GexfFileHandler;
import org.timofeevda.dependencygraph.plugin.AbstractBaseMojo;
import org.timofeevda.dependencygraph.plugin.utils.DependencyDescriptor;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;

import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.timofeevda.dependencygraph.plugin.utils.jaxb.gexf.GexfElementFactory;
import org.timofeevda.dependencygraph.plugin.utils.jaxb.ygraphml.GraphmlBuilder;
import org.timofeevda.dependencygraph.plugin.utils.jaxb.ygraphml.YGraphMLFileHandler;

/**
 * Goal for dependencies graph extraction
 *
 * @author timofeevda
 * @goal graph
 */
public class GraphMojo extends AbstractBaseMojo {

    private static final String GROUP_ID_SEPARATOR = ",";
    private static final String DEPENDENCIES_GRAPH_FILE = "dependencygraph.gexf";
    private static final String DEPENDENCIES_GRAPHML_FILE = "dependencygraph.graphml";

    /**
     * Flag denoting the type of dependencies traversal (transitive or not)
     *
     * @parameter default-value=true
     */
    protected boolean transitive;
    /**
     * Flag denoting if plugin should create separate dependency graph for each maven module
     *
     * @parameter default-value=false
     */
    protected boolean separateGraphs;
    /**
     * Label pattern for the graph nodes
     *
     * @parameter default-value="groupId:artifactId:version"
     */
    protected String labelPattern;
    /**
     * Property for dependencies filtering based on group id. Several groupIds can be specified using
     * comma separator
     *
     * @parameter default-value=""
     */
    protected String filterGroupId;

    private final Map<String, NodeContent> nodesMap = new HashMap<>();
    private final Map<EdgeDescriptor, EdgeContent> edgesMap = new HashMap<>();
    private final LinkedList<NodeContent> nodesStack = new LinkedList<>();
    private List<String> groupIdFilters = Collections.emptyList();

    private long maxNodeId;
    private long maxEdgeId;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            setupBuildParameters();
            createWorkingDirectory();
            createDependencyGraph();
        } catch (JAXBException | IOException ex) {
            throw new MojoFailureException(ex.getMessage());
        }
        getLog().info(" Project's dependencies extraction is complete.");
    }

    private void setupBuildParameters() {
        transitive = Boolean.parseBoolean(
                System.getProperty("transitive", Boolean.toString(transitive)));

        separateGraphs = Boolean.parseBoolean(
                System.getProperty("separateGraphs", Boolean.toString(separateGraphs)));

        labelPattern = System.getProperty("labelPattern", labelPattern);

        groupIdFilters = Arrays.stream(System.getProperty("filterGroupId", "").split(GROUP_ID_SEPARATOR))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.<String>toList());
    }

    private Optional<GexfContent> getIntermediateGraphContent(File graphFile) throws JAXBException, FileNotFoundException {
        return graphFile.exists() ? Optional.of(GexfFileHandler.read(graphFile)) : Optional.empty();
    }

    /**
     * Creates and dumps dependencies graph to the target directory using GEXF file format
     *
     * @throws FileNotFoundException
     * @throws JAXBException
     */
    private void createDependencyGraph() throws FileNotFoundException, JAXBException {
        File graphFile = resolveGraphFileLocation(DEPENDENCIES_GRAPH_FILE);

        Optional<GexfContent> intermediateGraphContent = graphFile.exists() ?
                Optional.of(GexfFileHandler.read(graphFile)) : Optional.empty();

        intermediateGraphContent.ifPresent(this::populateGraphMaps);

        // put current project as node in graph
        NodeContent projectNode = createNode(mavenProject.getGroupId(),
                mavenProject.getArtifactId(), mavenProject.getVersion());
        nodesMap.put(projectNode.getLabel(), projectNode);
        nodesStack.push(projectNode);

        resolveDependencies();

        GexfContent gexfContent = intermediateGraphContent.map(this::updateGraphContent)
                .orElse(GexfElementFactory.createGEXF(nodesMap.values(), edgesMap.values()));

        GexfFileHandler.write(gexfContent, graphFile);
        YGraphMLFileHandler.write(GraphmlBuilder.toGraphml(gexfContent), resolveGraphFileLocation(DEPENDENCIES_GRAPHML_FILE));
    }

    private GexfContent updateGraphContent(GexfContent gexfContent) {
        // clear intermediate GEXF content and populate it with altered graph content
        Optional<NodesContent> nodesContent = findNodesContent(gexfContent);
        nodesContent.map(NodesContent::getNode).ifPresent(nodes -> {
            nodes.clear();
            nodes.addAll(nodesMap.values());
        });

        Optional<EdgesContent> edgesContent = findEdgesContent(gexfContent);
        edgesContent.map(EdgesContent::getEdge).ifPresent(edges -> {
            edges.clear();
            edges.addAll(edgesMap.values());
        });
        return gexfContent;
    }

    private File resolveGraphFileLocation(String fileName) {
        File depsGraphFile;
        if (this.separateGraphs) {
            depsGraphFile = Paths.get(target, mavenProject.getGroupId()
                    + "." + mavenProject.getArtifactId()
                    + ":" + mavenProject.getVersion() + ".gexf").toFile();
        } else {
            depsGraphFile = Paths.get(target, fileName).toFile();
        }
        return depsGraphFile;
    }


    private void populateGraphMaps(GexfContent gexf) {
        populateNodesMap(gexf);
        PopulateEdgesMap(gexf);

    }

    private void PopulateEdgesMap(GexfContent gexf) {
        Optional<EdgesContent> edgesContent = findEdgesContent(gexf);
        edgesContent.map(content -> content.getEdge().stream())
                .ifPresent(edges -> edges.forEach(this::putInEdgesMap));
    }

    private Optional<EdgesContent> findEdgesContent(GexfContent gexf) {
        return gexf.getGraph().getAttributesOrNodesOrEdges().stream()
                .filter(EdgesContent.class::isInstance)
                .map(EdgesContent.class::cast)
                .findFirst();
    }

    private void populateNodesMap(GexfContent gexf) {
        Optional<NodesContent> nodesContent = findNodesContent(gexf);

        nodesContent.map(content -> content.getNode().stream())
                .ifPresent(nodes -> nodes.forEach(this::putInNodesMap));
    }

    private Optional<NodesContent> findNodesContent(GexfContent gexf) {
        return gexf.getGraph().getAttributesOrNodesOrEdges().stream()
                .filter(NodesContent.class::isInstance)
                .map(NodesContent.class::cast)
                .findFirst();
    }

    private void putInNodesMap(NodeContent node) {
        nodesMap.put(node.getLabel(), node);
        long nodeId = Long.parseLong(node.getId());
        maxNodeId = Math.max(maxNodeId, nodeId);
    }

    private void putInEdgesMap(EdgeContent edge) {
        edgesMap.put(new EdgeDescriptor(edge.getSource(), edge.getTarget()), edge);
        long edgeId = Long.parseLong(edge.getId());
        maxEdgeId = Math.max(maxEdgeId, edgeId);
    }

    /**
     * Resolve dependencies and write them out as nodes and edges on graph
     */
    private void resolveDependencies() {
        mavenProject.getDependencies().stream()
                .map(org.apache.maven.model.Dependency.class::cast)
                .filter(dependency -> dependency.getScope() == null
                        || dependency.getScope().equals("compile")
                        || dependency.getScope().equals("runtime"))
                .map(DependencyDescriptor::fromMavenDependency)
                .forEach(this::resolveDependency);
    }

    private void resolveDependency(DependencyDescriptor dependencyDescriptor) {
        Artifact artifact = dependencyDescriptor.getAetherArtifact();
        Dependency dep = new Dependency(artifact, dependencyDescriptor.getType());
        CollectRequest req = new CollectRequest(dep, remoteRepositories);
        DependencyTreeVisitor rdv = new DependencyTreeVisitor();
        try {
            repositorySystem.collectDependencies(repositorySystemSession, req);
            DependencyNode node = repositorySystem.collectDependencies(repositorySystemSession, req).getRoot();
            repositorySystem.resolveDependencies(repositorySystemSession, new DependencyRequest(node, null));
            node.accept(rdv);
        } catch (DependencyResolutionException | DependencyCollectionException ex) {
            getLog().error(ex);
        }
    }

    /**
     * Represents key for the edge in the edges map
     */
    @EqualsAndHashCode
    private class EdgeDescriptor {

        @Getter
        @Setter
        private final String source;

        @Getter
        @Setter
        private final String target;

        public EdgeDescriptor(String source, String target) {
            this.source = source;
            this.target = target;
        }

    }

    private class DependencyTreeVisitor implements DependencyVisitor {

        @Override
        public boolean visitEnter(DependencyNode dn) {
            Dependency dependency = dn.getDependency();

            if (!filterOut(dependency.getArtifact().getGroupId())) {
                return transitive;
            }
            if (dependency.getScope().equals("compile")
                    || dependency.getScope().equals("runtime")
                    || dependency.getScope().equals("jar")
                    || dependency.getScope().equals("war")
                    || dependency.getScope().equals("pom")) {
                try {

                    Artifact depArtifact = dn.getDependency().getArtifact();
                    NodeContent dependencyNode
                            = createNode(depArtifact.getGroupId(),
                            depArtifact.getArtifactId(), depArtifact.getVersion());
                    NodeContent nodeToStack;
                    if (!nodesMap.containsKey(dependencyNode.getLabel())) {
                        nodesMap.put(dependencyNode.getLabel(), dependencyNode);
                        nodeToStack = dependencyNode;
                    } else {
                        nodeToStack = nodesMap.get(dependencyNode.getLabel());
                    }

                    EdgeContent edge = generateEdge(nodesStack.peek().getId() + "", nodeToStack.getId());
                    EdgeDescriptor edgeDescriptor
                            = new EdgeDescriptor(edge.getSource(), edge.getTarget());
                    if (!edgesMap.containsKey(edgeDescriptor)) {
                        edgesMap.put(edgeDescriptor, edge);
                    }
                    nodesStack.push(nodeToStack);

                } catch (Exception e) {
                    getLog().error(e);
                }
            }
            return transitive;
        }

        @Override
        public boolean visitLeave(DependencyNode dn) {
            Dependency dependency = dn.getDependency();

            if (!filterOut(dependency.getArtifact().getGroupId())) {
                return transitive;
            }

            if (dependency.getScope().equals("compile")
                    || dependency.getScope().equals("runtime")
                    || dependency.getScope().equals("jar")
                    || dependency.getScope().equals("war")
                    || dependency.getScope().equals("pom")) {
                try {
                    nodesStack.pop();
                } catch (Exception e) {
                    getLog().error(e);
                }
            }
            return transitive;
        }

        private boolean filterOut(String groupId) {
            return groupIdFilters.isEmpty() ? true : groupIdFilters.stream().filter(group -> groupId.contains(group)).findAny().isPresent();
        }
    }

    private EdgeContent generateEdge(String sourceId, String targetId) {
        return GexfElementFactory.createEdge(++maxEdgeId + "", sourceId, targetId);
    }

    private NodeContent createNode(String groupId,
                                   String artifactId, String version) {
        return GexfElementFactory.createNode(++maxNodeId + "",
                generateLabel(groupId, artifactId, version));
    }

    /**
     * Generate label based on label pattern value
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return
     */
    private String generateLabel(String groupId,
                                 String artifactId, String version) {
        return labelPattern.replace("groupId", groupId).replace("artifactId", artifactId).replace("version", version);
    }
}
