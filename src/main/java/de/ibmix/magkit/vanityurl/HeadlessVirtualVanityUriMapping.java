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

import info.magnolia.cms.beans.config.URI2RepositoryMapping;
import info.magnolia.module.site.SiteManager;

import javax.inject.Inject;
import javax.jcr.Node;
import java.net.URI;
import java.util.Map;

import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringAfter;

/**
 * Virtual Uri Mapping of vanity URLs managed in the vanity url app.
 *
 * @author frank.sommer
 */
public class HeadlessVirtualVanityUriMapping extends VirtualVanityUriMapping {

    private SiteManager _siteManager;

    @Override
    protected String extractPath(URI uri) {
        String path = EMPTY;

        final String headlessEndpoint = getVanityUrlModule().getHeadlessEndpoint();
        if (isNotBlank(headlessEndpoint)) {
            path = substringAfter(uri.getPath(), headlessEndpoint);
        }

        return path;
    }

    @Override
    protected String retrieveSite(String vanityUrl) {
        return _siteManager.getAssignedSite("", vanityUrl).getName();
    }

    @Override
    protected String getUriOfVanityUrl(String siteName, String vanityUrl) {
        final Map<String, URI2RepositoryMapping> mappings = _siteManager.getSite(siteName).getMappings();
        String mappedUri = mappings.values().stream().filter(repositoryMapping -> WEBSITE.equals(repositoryMapping.getRepository())).findFirst().map(mapping -> vanityUrl.replace(mapping.getHandlePrefix(), mapping.getURIPrefix())).orElse(EMPTY);
        return super.getUriOfVanityUrl(siteName, mappedUri);
    }

    @Override
    protected String createUrlForVanityNode(Node node) {
        return getVanityUrlService().createRedirectUrl(node, true);
    }

    @Inject
    public void setSiteManager(SiteManager siteManager) {
        _siteManager = siteManager;
    }

}
