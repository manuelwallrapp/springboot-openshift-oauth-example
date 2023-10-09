package ch.sbb.esta;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.OpenShiftClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenshiftOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Value("${openshift.api-url}")
    private String openshiftApiUrl;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        final List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("authority"));
        final Map<String, Object> attributes = getAttributes(oAuth2UserRequest);
        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    private Map<String, Object> getAttributes(OAuth2UserRequest oAuth2UserRequest) {
        return oAuth2UserRequest.getAdditionalParameters().isEmpty() ?
                readAdditionalParamsFromOpenshift(oAuth2UserRequest) :
                oAuth2UserRequest.getAdditionalParameters();
    }

    private Map<String, Object> readAdditionalParamsFromOpenshift(OAuth2UserRequest oAuth2UserRequest) {
        OAuth2AccessToken accessToken = oAuth2UserRequest.getAccessToken();
        try (KubernetesClient kubernetesClient = createKubeclient(accessToken)) {
            OpenShiftClient openShiftClient = kubernetesClient.adapt(OpenShiftClient.class);
            User user = openShiftClient.users().withName("~").get();
            return mapUserInfo(user);
        } catch (Exception e) {
            log.error("Error while reading user from openshift using fallback", e);
            return Map.of("email", "noreply@unknowndomain.com", "name", "unknown");
        }
    }

    private static Map<String, Object> mapUserInfo(User user) {
        return Map.of(
                "name", user.getMetadata().getName(),
                "email", user.getMetadata().getName() + "@sbb.ch",
                "fullname", user.getFullName(),
                "uid", user.getMetadata().getUid(),
                "groups", user.getGroups()
        );
    }

    private KubernetesClient createKubeclient(OAuth2AccessToken accessToken) {
        return new KubernetesClientBuilder().withConfig(createOpenshiftConfig(accessToken)).build();
    }

    private Config createOpenshiftConfig(OAuth2AccessToken accessToken) {
        return new ConfigBuilder().withMasterUrl(openshiftApiUrl).withOauthToken(accessToken.getTokenValue()).build();
    }
}