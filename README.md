# springboot-openshift-oauth-example
This example shows how to register a new Openshift OAuthClient and authenticate your Springboot 3 application over Openshift.

# Openshift OAuthClient
Openshift brings it's own ability to register new OAuthClients. This is very useful if you want to authenticate your application over Openshift without using some OIDC provider like Keycloak.

You can read more about registering OAuthClients in Openshift [here](https://docs.openshift.com/container-platform/4.13/authentication/configuring-oauth-clients.html).

# Springboot 3
We built a simple Springboot 3 application to test the Authentication process and want to share this with you. 
Of course the same you can to with Quarkus or any other framework.

Sadly the Openshift OAuthClient doesn't bring a userinfo endpoint so we have to use the Openshift user endpoint to get the user information and patch it into the authentication process.

# How to use this example
1. Clone this repository
2. Create a new Openshift project on which you want to give access rights to the user who will login
3. Adapt the openshift/OAuthClient.yaml and set the password and redirect Url to your needs
4. Login to your Openshift cluster in your terminal
5. Apply the OAuthClient.yaml to your Openshift project by running `oc apply -f openshift/OAuthClient.yaml`
6. Adapt the application.yaml with your OAuthClient information or set the Environment Variables in your Run Config (use EnvFile plugin in IntelliJ to load the local.env file)
7. The OPENSHIFT_OAUTH_URL you can get by running `oc get route oauth-openshift -n openshift-authentication -o json | jq .spec.host`
8. Run the application, open localhost:8080 in your browser and sign in to Openshift
9. Your username will be displayed and your are authenticated


# Example local.env file
Your .env File should look like this:
```
OPENSHIFT_API_URL=https://api.someopenshift-host.net:6443
OPENSHIFT_OAUTH_URL=https://oauth-openshift.apps.someopenshift-host.net
OPENSHIFT_OAUTH_CLIENT_SECRET=yourGeneratedPasswordFrom-OAuthClient.yaml
```