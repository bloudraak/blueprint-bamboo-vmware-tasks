package io.blueprints.bamboo.plugins;
 
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
 
public class ShutdownVirtualMachineTask implements TaskType
{
    @Override
    public TaskResult execute(final TaskContext taskContext) throws TaskException
    {
        final BuildLogger buildLogger = taskContext.getBuildLogger();
		final String server = taskContext.getConfigurationMap().get("server");
		final String username = taskContext.getConfigurationMap().get("username");
		final String password = taskContext.getConfigurationMap().get("password");
		final String name = taskContext.getConfigurationMap().get("name");

        buildLogger.addBuildLogEntry("Shutting down guest in virtual machine '" + name + "' on '" + server + "' using username '" + username + "'");

        return TaskResultBuilder.create(taskContext).success().build();
    }
}