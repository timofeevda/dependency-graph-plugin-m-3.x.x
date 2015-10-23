# Dependecy Graph Maven Plugin

Plugin for generating dependencies graph using GEXF file format which can be used in graph visualization tools like Gephi.

### Goals overview

The Dependency Graph plugin has one goal:

* dependencygraph:graph builds GEXF file containing Maven project's dependency graph

### Optional parameters

| Parameter     | Type          | Description  |
| ------------- | ------------- | ------------ |
| transitive    | boolean       | The type of dependencies traversal (transitive or not). Default value is: true                                         |
| separateGraphs| String        | Create separate dependency graph for each Maven module. Default value is: false                                        |
| labelPattern  | String        | Label pattern for the graph nodes. Default value is: "groupId:artifactId:version"                                      |
| filterGroupId | String        | Dependency filtering based on group id. Several group ids can be specified using comma separator. Default value is: "" |


### Usage

1. if you don't have plugin entry in <build> section:
   mvn org.timofeevda:dependencygraph-maven-plugin:graph
2. otherwise:
   mvn dependencygraph:graph

This goal will generate dependencygraph.gexf file in target folder. 

```
mvn clean org.timofeevda:dependencygraph-maven-plugin:graph -DfilterGroupId=org.sonatype.aether,org.apache.maven -DlabelPattern=groupId:artifactId
```
