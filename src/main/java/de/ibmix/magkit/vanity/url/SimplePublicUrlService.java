package de.ibmix.magkit.vanity.url;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;

import static de.ibmix.magkit.vanity.url.VanityUrlService.PN_LINK;
import static de.ibmix.magkit.vanity.url.VanityUrlService.PN_SUFFIX;
import static de.ibmix.magkit.vanity.url.VanityUrlService.PN_VANITY_URL;
import static de.ibmix.magkit.vanity.url.VanityUrlService.isExternalLink;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.replaceOnce;

/**
 * Alternative simple implementation for the {@link PublicUrlService}.
 * Uses just the configured server prefix for external link creation.
 *
 * @author frank.sommer
 * @since 16.10.14
 */
public class SimplePublicUrlService implements PublicUrlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePublicUrlService.class);

    @Inject
    @Named(value = "magnolia.contextpath")
    private String _contextPath = EMPTY;

    private String _targetServerPrefix = "http://www.demo-project.com/context";

    @Override
    public String createVanityUrl(final Node node) {
        LOGGER.debug("Create vanity url for node {}", getPathIfPossible(node));
        return normalizePrefix() + getString(node, PN_VANITY_URL, EMPTY);
    }

    @Override
    public String createTargetUrl(final Node node) {
        LOGGER.debug("Create target url for node {}", getPathIfPossible(node));
        String url = EMPTY;
        if (node != null) {
            url = getString(node, PN_LINK, EMPTY);
            if (isNotEmpty(url)) {
                if (!isExternalLink(url)) {
                    url = normalizePrefix() + removeContextPath(getExternalLinkFromId(url));
                }
                url += getString(node, PN_SUFFIX, EMPTY);
            }
        }
        return url;
    }

    /**
     * For public removing the context path.
     *
     * @param link to check
     * @return link with removed context path
     */
    private String removeContextPath(String link) {
        String changedLink = link;
        if (isNotEmpty(_contextPath)) {
            changedLink = replaceOnce(changedLink, _contextPath, EMPTY);
        }
        return changedLink;
    }

    private String normalizePrefix() {
        return removeEnd(_targetServerPrefix, "/");
    }

    public void setTargetServerPrefix(final String targetServerPrefix) {
        _targetServerPrefix = targetServerPrefix;
    }
}
