/*
 * Copyright (c) 2013, Werner Strydom
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Werner Strydom nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL WERNER STRYDOM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package io.blueprints.bamboo.plugins;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.security.EncryptionService;
import com.opensymphony.xwork.TextProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VirtualMachineTaskConfigurator extends AbstractTaskConfigurator
{
    private TextProvider textProvider;
	public static final String HOST = "server";
    public static final String USERNAME = "username";
    public static final String PLAIN_PASSWORD = "password";
    public static final String PASSWORD = "encryptedPassword";
    public static final String CHANGE_PASSWORD = "change_password";
 	public static final String NAME = "name";

	private final EncryptionService encryptionService;

    public VirtualMachineTaskConfigurator(EncryptionService encryptionService)
    {
        this.encryptionService = encryptionService;
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
		config.put(HOST, params.getString(HOST));
		config.put(USERNAME, params.getString(USERNAME));
		config.put(NAME, params.getString(NAME));
		
		String passwordChange = params.getString(CHANGE_PASSWORD);
		if ("true".equals(passwordChange)) {
			final String password = params.getString(PLAIN_PASSWORD);
			config.put(PASSWORD, encryptionService.encrypt(password));
		}
		else if (previousTaskDefinition != null) {
			config.put(PASSWORD, previousTaskDefinition.getConfiguration().get(PASSWORD));
		}
		else {
			final String password = params.getString(PLAIN_PASSWORD);
			config.put(PASSWORD, encryptionService.encrypt(password));
		}

        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);

        context.put(HOST, taskDefinition.getConfiguration().get(HOST));
		context.put(USERNAME, taskDefinition.getConfiguration().get(USERNAME));
		context.put(PLAIN_PASSWORD, taskDefinition.getConfiguration().get(PASSWORD));
		context.put(NAME, taskDefinition.getConfiguration().get(NAME));
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);
        context.put(HOST, taskDefinition.getConfiguration().get(HOST));
		context.put(USERNAME, taskDefinition.getConfiguration().get(USERNAME));
		context.put(NAME, taskDefinition.getConfiguration().get(NAME));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);

		final String server = params.getString(HOST);
		if (StringUtils.isEmpty(server))
		{
		    errorCollection.addError(HOST, textProvider.getText("vm.server.error"));
		}

		final String username = params.getString(USERNAME);
		if (StringUtils.isEmpty(username))
		{
		    errorCollection.addError(USERNAME, textProvider.getText("vm.username.error"));
		}
		
		if ("true".equals(params.getString(CHANGE_PASSWORD)))
		{
			String password = params.getString(PLAIN_PASSWORD);
			if (StringUtils.isEmpty(password))
			{
				errorCollection.addError(PLAIN_PASSWORD, textProvider.getText("vm.password.error"));
			}
		}

		final String name = params.getString(NAME);
		if (StringUtils.isEmpty(name))
		{
		    errorCollection.addError(NAME, textProvider.getText("vm.name.error"));
		}
    }

    public void setTextProvider(final TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }
}