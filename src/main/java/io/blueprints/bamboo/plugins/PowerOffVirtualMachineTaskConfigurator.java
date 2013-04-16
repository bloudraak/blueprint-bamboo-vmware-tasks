package io.blueprints.bamboo.plugins;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.opensymphony.xwork.TextProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PowerOffVirtualMachineTaskConfigurator extends AbstractTaskConfigurator
{
    private TextProvider textProvider;

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
		config.put("server", params.getString("server"));
		config.put("username", params.getString("username"));
		config.put("password", params.getString("password"));
		config.put("name", params.getString("name"));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);

        context.put("server", "");
		context.put("username", "");
		context.put("password", "");
		context.put("name", "");
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);

        context.put("server", taskDefinition.getConfiguration().get("server"));
		context.put("username", taskDefinition.getConfiguration().get("username"));
		context.put("password", taskDefinition.getConfiguration().get("password"));
		context.put("name", taskDefinition.getConfiguration().get("name"));
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);
        context.put("server", taskDefinition.getConfiguration().get("server"));
		context.put("username", taskDefinition.getConfiguration().get("username"));
		context.put("password", "********");
		context.put("name", taskDefinition.getConfiguration().get("name"));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

		final String server = params.getString("server");
		if (StringUtils.isEmpty(server))
		{
		    errorCollection.addError("server", textProvider.getText("vm.server.error"));
		}

		final String username = params.getString("username");
		if (StringUtils.isEmpty(username))
		{
		    errorCollection.addError("username", textProvider.getText("vm.username.error"));
		}

		final String password = params.getString("password");
		if (StringUtils.isEmpty(password))
		{
		    errorCollection.addError("password", textProvider.getText("vm.password.error"));
		}

		final String name = params.getString("name");
		if (StringUtils.isEmpty(name))
		{
		    errorCollection.addError("name", textProvider.getText("vm.name.error"));
		}
    }

    public void setTextProvider(final TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }
}