import hudson.util.*
import hudson.tasks.*
import hudson.maven.*
import hudson.model.*
import hudson.tasks.*

String script = """#!/bin/bash +x
if [ "\$WARNING" = true ] ; then
  echo "STARTING DEPLOY"
else
  echo "DEPLOY BLOCKED"
  exit 1
fi"""

Jenkins.instance.getAllItems(Job.class)
	.findAll{ it instanceof FreeStyleProject || it instanceof MavenModuleSet }
    .each{
        // edit here!
        if (it.fullName.startsWith("foo") && it.name.contains("bar")) {
            println it.fullName + " --- " + it.class

            prop = it.getProperty(ParametersDefinitionProperty.class)

            if (!prop.getParameterDefinition("WARNING")) {
                println "TASK - BooleanParameterDefinition 'WARNING' not exists, adding it!"
                def new_parameters = new ArrayList<>()

                new_parameters.add(new BooleanParameterDefinition("WARNING", false, "You will make a distribution in the production environment, please select the checkbox if you want continue!"))
                new_parameters.addAll(prop.getParameterDefinitions())

                ParametersDefinitionProperty paramsDef = new ParametersDefinitionProperty(new_parameters)

                it.addProperty(paramsDef)
            } else {
                println "TASK - BooleanParameterDefinition 'WARNING' already exists, nothing to do!"
            }

            DescribableList<Builder,Descriptor<Builder>> builders = new DescribableList<Builder,Descriptor<Builder>>()
            builders.add(new Shell(script))

            if(it instanceof FreeStyleProject) {

                if (it.getBuilders().first().getClass() == hudson.tasks.Shell && !it.getBuilders().first().getContents() == script) {
                    println "TASK - hudson.tasks.Shell 'Script' not exists, adding it!"

                    builders.addAll(it.getBuildersList())
                    it.getBuildersList().clear()
                    it.getBuildersList().addAll(builders)
                } else {
                    println "TASK - hudson.tasks.Shell 'Script' already exists, nothing to do!"
                }

            } else if (it instanceof MavenModuleSet) {

                if (it.getBuilders().first().getClass() == hudson.tasks.Shell && !it.getPrebuilders().first().getContents() == script) {
                    println "TASK - hudson.tasks.Shell 'Script' not exists, adding it!"

                    builders.addAll(it.getPrebuilders())
                    it.getPrebuilders().clear()
                    it.getPrebuilders().addAll(builders)
                } else {
                    println "TASK - hudson.tasks.Shell 'Script' already exists, nothing to do!"
                }
            }
            // autosave depends by Jenkins version, in the case uncomment the following:
            //it.save
        }
    }
