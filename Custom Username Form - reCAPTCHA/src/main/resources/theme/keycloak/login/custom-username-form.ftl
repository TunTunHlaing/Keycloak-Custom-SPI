<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
        Enter Username
    <#elseif section = "form">
        <form id="kc-username-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <label for="username" class="${properties.kcLabelClass!}">Username</label>
                <input type="text" id="username" name="username" class="${properties.kcInputClass!}" value="${(username!'')}" />
            </div>
            <#if messages?has_content>
                <div class="${properties.kcAlertClass!} ${properties.kcAlertErrorClass!}">
    ${kcSanitize(messages.asString())}
                </div>
            </#if>

            <#if recaptchaRequired??>
                <div class="form-group">
                    <div class="${properties.kcInputWrapperClass!}">
                        <div class="g-recaptcha" data-size="compact" data-sitekey="${recaptchaSiteKey}"></div>
                    </div>
                </div>
            </#if>

            <div class="${properties.kcFormGroupClass!}">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!}" type="submit" value="Continue" />
            </div>
        </form>
    </#if>
</@layout.registrationLayout>