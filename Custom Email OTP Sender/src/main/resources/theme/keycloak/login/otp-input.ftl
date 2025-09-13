<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
  <form action="${url.loginAction}" method="post">
    <div class="form-group">
      <label for="otp">Enter OTP</label>
      <input type="text" id="otp" name="otp" class="form-control" autofocus />
    </div>
    <div class="form-group">
      <button type="submit" class="btn btn-primary">Verify</button>
    </div>
  </form>
</@layout.registrationLayout>
