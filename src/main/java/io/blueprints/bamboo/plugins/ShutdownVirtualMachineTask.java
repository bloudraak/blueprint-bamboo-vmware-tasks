package io.blueprints.bamboo.plugins;

import java.net.URL;
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
		try {
			buildLogger.addBuildLogEntry("Shutting down guest in virtual machine '" + name + "' on '" + server + "' using username '" + username + "'");
			VMwareVirtualMachine virtualMachine = new VMwareVirtualMachine(new URL(server), name, username, password);
			try {
				virtualMachine.shutdown();
				buildLogger.addBuildLogEntry("Succesfully shutdown guest in virtual machine '" + name + "' on '" + server + "' using username '" + username + "'");
			}
			finally {
				virtualMachine.disconnect();
			}
		}
		catch(Exception exception) {
			throw new TaskException("Failed to shutdown guest in virtual machine '" + name + "' on '" + server + "' using username '" + username + "'", exception);
		}
        return TaskResultBuilder.create(taskContext).success().build();
    }
}