<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
        <div class="tabs">
            <button type="button" class="tab-button <#if (inputType!"email") == "email">active</#if>" onclick="switchTab('email')">Email</button>
            <button type="button" class="tab-button <#if (inputType!"email") == "phone">active</#if>" onclick="switchTab('phone')">Phone</button>
        </div>
    <#elseif section = "form">
        <form id="kc-username-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <input type="hidden" id="inputType" name="inputType" value="${(inputType!"email")}">
            <div class="${properties.kcFormGroupClass!}">
                <label for="inputValue" class="${properties.kcLabelClass!}"><#if (inputType!"email") == "phone">Phone Number<#else>Email</#if></label>
                <input type="<#if (inputType!"email") == "phone">tel<#else>email</#if>" id="inputValue" name="inputValue" class="${properties.kcInputClass!}" value="${(inputValue!"")}" />
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
                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!}" type="submit" value="Send OTP" />
            </div>
        </form>
        <script>
            function switchTab(type) {
                document.getElementById('inputType').value = type;
                const formGroup = document.querySelector('.${properties.kcFormGroupClass!?js_string}');
                const label = formGroup.querySelector('label');
                const input = formGroup.querySelector('input[name="inputValue"]');
                label.textContent = type === 'phone' ? 'Phone Number' : 'Email';
                input.type = type === 'phone' ? 'tel' : 'email';
                input.value = '';
                document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));
                const selector = '.tab-button[onclick="switchTab(\'' + type + '\')"]';
                document.querySelector(selector).classList.add('active');
            }
        </script>
        <style>
            .tabs {
                display: flex;
                justify-content: center;
                margin-bottom: 20px;
            }
            .tab-button {
                padding: 10px 20px;
                margin: 0 5px;
                border: 1px solid #ccc;
                background: #f9f9f9;
                cursor: pointer;
            }
            .tab-button.active {
                background: #007bff;
                color: white;
                border-color: #007bff;
            }
        </style>
    </#if>
</@layout.registrationLayout>