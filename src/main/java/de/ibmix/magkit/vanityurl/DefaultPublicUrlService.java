package de.ibmix.magkit.vanityurl;

/*
 * #%L
 * magkit-vanity-url Magnolia Module
 * %%
 * Copyright (C) 2013 - 2014 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.module.site.Domain;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import java.util.Collection;

import static de.ibmix.magkit.vanityurl.VanityUrlService.DEF_SITE;
import static de.ibmix.magkit.vanityurl.VanityUrlService.PN_LINK;
import static de.ibmix.magkit.vanityurl.VanityUrlService.PN_SITE;
import static de.ibmix.magkit.vanityurl.VanityUrlService.PN_SUFFIX;
import static de.ibmix.magkit.vanityurl.VanityUrlService.PN_VANITY_URL;
import static de.ibmix.magkit.vanityurl.VanityUrlService.isExternalLink;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.replaceOnce;

/**
 * Default implementation for the {@link PublicUrlService}.
 *
 * @author frank.sommer
 * @since 16.10.14
 */
public class DefaultPublicUrlService implements PublicUrlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPublicUrlService.class);

    private String _targetContextPath = EMPTY;

    @Inject
    @Named(value = "magnolia.contextpath")
    private String _contextPath = EMPTY;

    private SiteManager _siteManager;
    private ServerConfiguration _serverConfiguration;

    @Override
    public String createVanityUrl(final Node node) {
        LOGGER.debug("Create vanity url for node {}", getPathIfPossible(node));
        // default base url is the default
        String baseUrl = _serverConfiguration.getDefaultBaseUrl();
        baseUrl = replaceContextPath(baseUrl);

        // check the site configuration and take the first domain
        String siteName = getString(node, PN_SITE, DEF_SITE);
        if (!DEF_SITE.equals(siteName)) {
            Site site = _siteManager.getSite(siteName);
            Collection<Domain> domains = site.getDomains();
            if (!domains.isEmpty()) {
                Domain firstDomain = domains.iterator().next();
                baseUrl = firstDomain.toString();
            }
        }

        return removeEnd(baseUrl, "/") + getString(node, PN_VANITY_URL, EMPTY);
    }

    /**
     * For public replacing the context path.
     *
     * @param link for replacing
     * @return link with replaced context path
     */
    private String replaceContextPath(String link) {
        String changedLink = link;
        if (isNotEmpty(_contextPath)) {
            changedLink = replaceOnce(changedLink, _contextPath, _targetContextPath);
        }
        return changedLink;
    }

    @Override
    public String createTargetUrl(final Node node) {
        LOGGER.debug("Create target url for node {}", getPathIfPossible(node));
        String url = EMPTY;
        if (node != null) {
            url = getString(node, PN_LINK, EMPTY);
            if (isNotEmpty(url)) {
                if (!isExternalLink(url)) {
                    url = getExternalLinkFromId(url);
                    url = replaceContextPath(url);
                }
                url += getString(node, PN_SUFFIX, EMPTY);
            }
        }
        return url;
    }

    @Inject
    public void setSiteManager(final SiteManager siteManager) {
        _siteManager = siteManager;
    }

    @Inject
    public void setServerConfiguration(final ServerConfiguration serverConfiguration) {
        _serverConfiguration = serverConfiguration;
    }

    public void setContextPath(final String contextPath) {
        _contextPath = contextPath;
    }

    public void setTargetContextPath(final String targetContextPath) {
        _targetContextPath = targetContextPath;
    }
}
