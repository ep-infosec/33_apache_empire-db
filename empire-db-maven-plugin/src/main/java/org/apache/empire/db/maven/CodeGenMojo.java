/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.empire.db.maven;

import java.io.File;

import org.apache.empire.db.codegen.CodeGenConfig;
import org.apache.empire.db.codegen.CodeGenerator;
import org.apache.empire.exceptions.InvalidPropertyException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Empire-DB Code generation by reading an existing database schema
 */
@Mojo( name = "codegen",
       aggregator = false,
       defaultPhase = LifecyclePhase.GENERATE_SOURCES,
       requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CodeGenMojo extends AbstractMojo {

    @Component
	private MavenProject project;
	
	/**
	 * Codegen configuration file, if the file is provided, only that file
	 * is used to configure code generation
	 */
    @Parameter(property = "empiredb.configFile")
	private File configFile;

	/**
	 * Location of the generated sources.
	 */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/java", property = "empiredb.generatedsources", required = true)
	private File targetDirectory;
	
	/**
	 * JDBC url
	 */
    @Parameter(property = "empiredb.jdbcURL")
	private String jdbcURL;
	
	/**
	 * JDBC Driver class
	 */
    @Parameter(property = "empiredb.jdbcClass")
	private String jdbcClass;
	
	/**
	 * JDBC Database user
	 */
    @Parameter(property = "empiredb.jdbcUser")
	private String jdbcUser;
	
	/**
	 * JDBC Database password
	 */
    @Parameter(property = "empiredb.jdbcPwd")
	private String jdbcPwd;
    
    /**
     * DBMSHandler class name
     */
    @Parameter(property = "empiredb.dbmsHandlerClass")
    private String dbmsHandlerClass;
	
	/**
	 * Code generator template directory, if not set the default templates
	 * are loaded from the classpath
	 */
    @Parameter(property = "empiredb.templateDirectory")
	private String templateDirectory;
	
	/**
	 * The package for the generated sources
	 */
    @Parameter(property = "empiredb.packageName")
	private String packageName;
    
    /**
     * The name of the generated Database class
     */
    @Parameter(property = "empiredb.dbClassName")
    private String dbClassName;

    @Parameter(property = "empiredb.nestTables")
    private boolean nestTables = false;
    
    @Parameter(property = "empiredb.nestViews")
    private boolean nestViews = false;
    
    @Parameter(property = "empiredb.createRecordProperties")
    private boolean createRecordProperties = false;
    
    @Parameter(property = "empiredb.preserverCharacterCase")
    private boolean preserverCharacterCase = false;
    
    @Parameter(property = "empiredb.preserveRelationNames")
    private boolean preserveRelationNames = false; 

	@Override
    public void execute() throws MojoExecutionException 
	{
		
		setupLogging();
		
		CodeGenConfig config = new CodeGenConfig();
		if(configFile != null)
		{
			getLog().info("Loading configuration file: " + configFile);
			config.init(configFile.getAbsolutePath());
			targetDirectory = new File(config.getTargetFolder());
		}
		else
		{
		    if (StringUtils.isEmpty(jdbcURL) ||
		        StringUtils.isEmpty(jdbcClass))
		    {   // Missing
		        throw new InvalidPropertyException("jdbcURL|jdbcClass", null);
		    }
		    
			config.setJdbcURL(jdbcURL);
			config.setJdbcClass(jdbcClass);
			config.setJdbcUser(jdbcUser);
			config.setJdbcPwd(jdbcPwd);
			config.setDbmsHandlerClass(dbmsHandlerClass);
			config.setTargetFolder(targetDirectory.getAbsolutePath());
			config.setTemplateFolder(templateDirectory);
			config.setPackageName(packageName);
			config.setDbClassName(dbClassName);
			config.setNestTables(nestTables);
			config.setNestViews(nestViews);
			config.setCreateRecordProperties(createRecordProperties);
			config.setPreserverCharacterCase(preserverCharacterCase);
			config.setPreserveRelationNames(preserveRelationNames);
		}
		
		//config.setExceptionsEnabled(true);
		
		getLog().info("Generating code for " + jdbcURL + " ...");
		
		// generate now
		CodeGenerator codeGen = new CodeGenerator();
		codeGen.generate(config);
		
		getLog().info("Code successfully generated in: " + targetDirectory);
		
		// we want the generate sources to be available in the project itself
		if (project != null && targetDirectory != null && targetDirectory.exists()) 
		{
            getLog().info("Adding Compile Source Folder: " + targetDirectory);
			project.addCompileSourceRoot(targetDirectory.getAbsolutePath());
		}
		else if (targetDirectory!=null)
		{
            getLog().warn("Target Source Folder does not exist: " + targetDirectory);
		}
	}

	private void setupLogging() 
	{
		Logger logger = Logger.getRootLogger();
		logger.addAppender(new MavenAppender(this));
	}
	
	/**
	 * Forwards Log4j logging to maven logging
	 * 
	 */
	private static final class MavenAppender extends AppenderSkeleton 
	{
		
		private final AbstractMojo mojo;
		
		public MavenAppender(final AbstractMojo mojo) 
		{
			this.mojo = mojo;
		}
		
		@Override
		public boolean requiresLayout() 
		{
			return false;
		}

		@Override
		public void close() 
		{
			// nothing to do here
		}

		@Override
		protected void append(LoggingEvent event) 
		{
			if(Level.INFO.equals(event.getLevel()))
			{
				mojo.getLog().info(String.valueOf(event.getMessage()));
			}
			else if(Level.ERROR.equals(event.getLevel()))
			{
				// TODO support throwables?
				mojo.getLog().error(String.valueOf(event.getMessage()));
			}
			else if(Level.WARN.equals(event.getLevel()))
			{
				mojo.getLog().warn(String.valueOf(event.getMessage()));
			}
		}
	}
}
