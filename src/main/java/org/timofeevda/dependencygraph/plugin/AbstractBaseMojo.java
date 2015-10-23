package org.timofeevda.dependencygraph.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Abstract mojo for all plugin's goals
 *
 * @author timofeevda
 */
public abstract class AbstractBaseMojo extends AbstractMojo {
    /**
     * Output directory for the intermediate files
     *
     * @parameter default-value="target"
     */
    protected String target;
    /**
     * Reference to maven project created from pom.xml file
     * @parameter default-value="${project}"
     */
    protected MavenProject mavenProject;   
     /**
     * Aether repository system
     *
     * @component
     */
    protected RepositorySystem repositorySystem;
    /**
     * Current repository/network configuration of Maven
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    protected RepositorySystemSession repositorySystemSession;
    /**
     * Project's remote repositories to use for the resolution of artifact and their dependencies
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    protected List<RemoteRepository> remoteRepositories;

    /**
     * Create output directory if it doesn't exist
     *
     * @throws IOException
     */
    protected void createWorkingDirectory() throws IOException {
        File outputDirectory = new File(target);
        if(!outputDirectory.exists()) {
            boolean res = outputDirectory.mkdirs();
            if(!res) {
                throw new IOException("Can't create intermediate distribution folder: " + outputDirectory.getAbsolutePath());
            }
        }
    }
    

}
