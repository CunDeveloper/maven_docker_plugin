package docker.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * deploy war file to docker container
 */
@Mojo( name = "docker", defaultPhase = LifecyclePhase.DEPLOY)
public class DockerMojo extends AbstractMojo
{
	private static final String JAR = "jar";
	private static final String WAR = "war";
	private static final String DEPLOY_SUCCESS = "DEPLOY SUCCESS";
	private static final String DEPLOY_FAILURE = "DEPLOY FAILURE";
	 
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    private File buildDirectory;
 
    @Parameter(defaultValue = "${env.DOCKER_CONTAINTER_PATH}")
    private String destPath;
    
    private File destFile;
    
    public void execute() throws MojoExecutionException
    {
    	if(null==destPath || destPath.equals("")){
    		resultLog(DEPLOY_FAILURE);
    		getLog().error("===environment varibales is not setted.");
    	}else{
    		exeDeploy();
    	}
    }
    
    private void exeDeploy() throws MojoExecutionException {
    	
    	 if (!buildDirectory.exists())
         {
         	buildDirectory.mkdirs();
         }
         getLog().info("===docker container path "+destPath);
         String replaceDestPath = destPath.replace("/", "\\");
         destFile = new File(replaceDestPath);
         if(!destFile.exists()){
         	destFile.mkdirs();
         }
         
         try
         {
             getLog().info("===start copy war file to "+destFile.getAbsolutePath());
             for (File subFile:buildDirectory.listFiles()){
             	String subFileName = subFile.getName();
             	if (subFileName.endsWith(JAR) || subFileName.endsWith(WAR)){
             		File haha = new File(destFile,subFile.getName());
             		compyVarToDockerContainer(subFile,haha);
             		getLog().info(subFile.getAbsolutePath()+"="+subFile.getName());
             		 break;
             	}
             }
             resultLog(DEPLOY_SUCCESS);
         }
         catch (IOException e)
         {
        	 resultLog(DEPLOY_FAILURE);
         	 getLog().error("===error info "+e.getMessage());
             throw new MojoExecutionException( "===error copy war ", e);
         }
    }
    
	private static void compyVarToDockerContainer(File source,File dest) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally{
			input.close();
			output.close();
		}
    }
	
	private  void resultLog(String result){
		getLog().info("===copy war file to: " + destPath);
        getLog().info("++++++++++++++++++++++++++++++++");
        getLog().info(result);
        getLog().info("++++++++++++++++++++++++++++++++");
	}
    
}
