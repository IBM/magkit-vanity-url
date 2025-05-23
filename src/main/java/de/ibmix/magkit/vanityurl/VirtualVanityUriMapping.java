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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.NullSite;
import info.magnolia.module.site.Site;
import info.magnolia.virtualuri.VirtualUriMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Virtual Uri Mapping of vanity URLs managed in the vanity url app.
 *
 * @author frank.sommer
 */
public class VirtualVanityUriMapping implements VirtualUriMapping {
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualVanityUriMapping.class);

    private Provider<VanityUrlModule> _vanityUrlModule;
    private Provider<VanityUrlService> _vanityUrlService;

    @Inject
    public void setVanityUrlModule(final Provider<VanityUrlModule> vanityUrlModule) {
        _vanityUrlModule = vanityUrlModule;
    }

    @Inject
    public void setVanityUrlService(final Provider<VanityUrlService> vanityUrlService) {
        _vanityUrlService = vanityUrlService;
    }

    @Override
    public Optional<Result> mapUri(final URI uri) {
        Optional<Result> result = Optional.empty();
        try {
            String vanityUrl = extractPath(uri);
            if (isVanityCandidate(vanityUrl)) {
                final String siteName = retrieveSite(vanityUrl);
                String toUri = getUriOfVanityUrl(siteName, vanityUrl, Optional.ofNullable(uri.getQuery()).map(value -> "?" + value).orElse(null));
                if (isNotBlank(toUri)) {
                    result = Optional.of(new Result(toUri, vanityUrl.length(), this));
                }
            }
        } catch (PatternSyntaxException e) {
            LOGGER.error("A vanity url exclude pattern is not set correctly.", e);
        }
        return result;
    }

    protected String extractPath(URI uri) {
        return uri.getPath();
    }

    protected boolean isVanityCandidate(String uri) {
        boolean contentUri = !isRootRequest(uri);
        if (contentUri) {
            Map<String, String> excludes = _vanityUrlModule.get().getExcludes();
            for (String exclude : excludes.values()) {
                if (isNotEmpty(exclude) && uri.matches(exclude)) {
                    contentUri = false;
                    break;
                }
            }
        }
        return contentUri;
    }

    private boolean isRootRequest(final String uri) {
        return uri.length() <= 1;
    }

    protected String getUriOfVanityUrl(String siteName, final String vanityUrl, String originSuffix) {
        Node node = null;

        try {
            // do it in the system context, so the anonymous need no read rights for using vanity urls
            node = MgnlContext.doInSystemContext(
                (MgnlContext.Op<Node, RepositoryException>) () -> _vanityUrlService.get().queryForVanityUrlNode(vanityUrl, siteName)
            );
        } catch (RepositoryException e) {
            LOGGER.warn("Error on querying for vanity url.", e);
        }

        return node == null ? EMPTY : createUrlForVanityNode(node, originSuffix);
    }

    /**
     * Override for alternative redirect url creation.
     *
     * @param node         vanity url node
     * @param originSuffix origin url suffix
     * @return redirect or forward url
     */
    protected String createUrlForVanityNode(final Node node, String originSuffix) {
        return _vanityUrlService.get().createRedirectUrl(node, originSuffix);
    }

    protected String retrieveSite(String vanityUrl) {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        Site site = aggregationState instanceof ExtendedAggregationState ? ((ExtendedAggregationState) aggregationState).getSite() : new NullSite();
        return site.getName();
    }

    @Override
    public boolean isValid() {
        return _vanityUrlService.get() != null;
    }

    public VanityUrlModule getVanityUrlModule() {
        return _vanityUrlModule.get();
    }

    public VanityUrlService getVanityUrlService() {
        return _vanityUrlService.get();
    }
}
