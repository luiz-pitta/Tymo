package io.development.tymo.model_server;

import java.io.Serializable;

public class AppInfoServer implements Serializable {
    private static final long serialVersionUID = 53L;

    private String name, email, site, site_url, use_terms_url, privacy_policy_url;

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setSiteUrl(String site_url) {
        this.site_url = site_url;
    }

    public void setUseTermsUrl(String use_terms_url) {
        this.use_terms_url = use_terms_url;
    }

    public void setPrivacyPoliceUrl(String privacy_policy_url) {
        this.privacy_policy_url = privacy_policy_url;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSite() {
        return site;
    }

    public String getSiteUrl() {
        return site_url;
    }

    public String getUseTermsUrl() {
        return use_terms_url;
    }

    public String getPrivacyPoliceUrl() {
        return privacy_policy_url;
    }

}
