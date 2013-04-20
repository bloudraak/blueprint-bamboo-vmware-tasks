[@ww.textfield labelKey="vm.server" name="server" required='true'/]

[@ww.textfield labelKey="vm.username" name="username" required='true'/]

[#if context.get("password")?has_content]
    [@ww.checkbox labelKey="vm.password.change" toggle=true name="change_password" /]
    [@ui.bambooSection dependsOn="change_password" showOn=true]
        [@ww.password labelKey="vm.password" name="password" required="true" /]
    [/@ui.bambooSection]
[#else]
    [@ww.hidden name="change_password" value=true /]
    [@ww.password labelKey="vm.password" name="password" required="true" /]
[/#if]

[@ww.textfield labelKey="vm.name" name="name" required='true'/]