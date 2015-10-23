package org.timofeevda.dependencygraph.plugin.utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;


/**
 * Utility dependency descriptor to represent Aether and Maven dependencies
 *
 * @author timofeevda
 */
@EqualsAndHashCode
public class DependencyDescriptor {

    @Getter
    @Setter
    protected String groupId;

    @Getter
    @Setter
    protected String artifactId;

    @Getter
    @Setter
    protected String classifier;

    @Getter
    @Setter
    protected String type;

    @Getter
    @Setter
    protected String version;

    @Getter
    @Setter
    protected String path;

    public DependencyDescriptor(String groupId, String artifactId, String classifier, String type, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.type = type;
        this.version = version;
    }

    public static DependencyDescriptor fromMavenDependency(org.apache.maven.model.Dependency dependency) {
        return new DependencyDescriptor(dependency.getGroupId(),
                dependency.getArtifactId(), dependency.getClassifier(),
                dependency.getType(), dependency.getVersion());
    }

    public Artifact getAetherArtifact() {
        return new DefaultArtifact(groupId, artifactId, classifier, type, version);
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DependencyDescriptor{" + "groupId=" + groupId + ", artifactId=" + artifactId + ", classifier=" + classifier + ", type=" + type + ", version=" + version + ", path=" + path + '}';
    }    

}
