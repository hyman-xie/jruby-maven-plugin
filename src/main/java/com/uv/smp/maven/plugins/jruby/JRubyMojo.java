package com.uv.smp.maven.plugins.jruby;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.dependency.AbstractDependencyMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jruby.Main;

@Mojo(name = "jruby", requiresDependencyResolution = ResolutionScope.COMPILE,defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class JRubyMojo extends AbstractDependencyMojo {

	@Parameter(defaultValue = "org.nanoko.libs")
    protected String compassGroupId;
   
    @Parameter(defaultValue = "compass-gems")
    private String compassArtifactId;
    
    @Parameter(defaultValue = "0.12.2")
    protected String compassVersion;
   
    @Parameter(defaultValue = "jar")
    protected String compassType;
    
    @Parameter
    protected String compassClassifier;
    
	@Parameter(defaultValue = "${project.build.directory}")
    private File workingDirectory;

    @Parameter(defaultValue = "false")
    private Boolean useCompass=false;

    @Parameter
    private String parameter;

	@Parameter(defaultValue = "${project.build.directory}/compile.rb")
    protected File compileFile;
	
	@Parameter(defaultValue = "${project.build.directory}/compass")
	protected File compassDirectory;
	
	public static final String COMPASS_GEMS="gems";
	
	public static final String COMPASS_COMPILE="compile";
	
	public void doExecute() throws MojoExecutionException, MojoFailureException {
		try {
			
			if(workingDirectory==null){
				throw new MojoExecutionException("No working directory found! ");
			}

            String[] args=null;

            if(useCompass){
                if(compileFile==null || !compileFile.exists()){
                    throw new MojoExecutionException("No compile.rb file found! ");
                }
                if(compassDirectory==null){
                    throw new MojoExecutionException("No compass directory found! ");
                }

                if(!compassDirectory.exists()){
                    Artifact compassArtifact = resolveCompassArtifact();
                    if (compassArtifact != null) {
                        unpackCompass(compassArtifact, compassDirectory);
                    }
                }

                args=new String[4];
                args[0]=compileFile.getCanonicalPath();
                args[1]=compassDirectory.getCanonicalPath()+File.separator+COMPASS_GEMS;
                args[2]=COMPASS_COMPILE;
                args[3]=workingDirectory.getAbsolutePath();
            }else{
                if(parameter==null){
                     throw new MojoExecutionException("No parameter found! ");
                }
                args=new String[1];
                args[0]=parameter;
            }

            Main main = new Main();
            main.run(args);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}
	
	protected void unpackCompass(Artifact compassArtifact, File outputDirectory) throws MojoExecutionException {
    	System.setProperty("COMPASS_DIR",outputDirectory.getAbsolutePath());
        unpack(compassArtifact, outputDirectory);
    }

    protected Artifact resolveCompassArtifact() throws ArtifactNotFoundException, ArtifactResolutionException {
        Artifact artifact = getFactory().createArtifact(compassGroupId, compassArtifactId, compassVersion, Artifact.SCOPE_COMPILE,compassType);
        getResolver().resolve(artifact, getRemoteRepos(), getLocal());
        return artifact;
    }
	
}
