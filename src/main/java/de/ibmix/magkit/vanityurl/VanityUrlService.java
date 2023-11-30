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

import de.ibmix.magkit.vanityurl.app.VanityUrlSaveFormAction;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.link.LinkUtil;
import org.apache.jackrabbit.value.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.Collections;
import java.util.List;

import static info.magnolia.cms.util.RequestDispatchUtil.FORWARD_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.PERMANENT_PREFIX;
import static info.magnolia.cms.util.RequestDispatchUtil.REDIRECT_PREFIX;
import static info.magnolia.jcr.util.NodeUtil.asIterable;
import static info.magnolia.jcr.util.NodeUtil.asList;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static info.magnolia.link.LinkUtil.DEFAULT_EXTENSION;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static javax.jcr.query.Query.JCR_SQL2;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.substringAfter;

/**
 * Query service for vanity url nodes in vanity url workspace.
 *
 * @author frank.sommer
 * @since 28.05.14
 */
public class VanityUrlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VanityUrlService.class);

    private static final String QUERY = "select * from [" + VanityUrlModule.NT_VANITY + "] where vanityUrl = $vanityUrl and site = $site";
    public static final String NN_IMAGE = "qrCode";
    public static final String DEF_SITE = "default";
    public static final String PN_SITE = "site";
    public static final String PN_VANITY_URL = "vanityUrl";
    public static final String PN_LINK = "link";
    public static final String PN_SUFFIX = "linkSuffix";
    public static final String PN_TYPE = "type";

    @Inject
    @Named(value = "magnolia.contextpath")
    private String _contextPath = "";

    private Provider<VanityUrlModule> _vanityUrlModule;

    /**
     * Creates the redirect url for uri mapping.
     * Without context path, because of Magnolia's {@link info.magnolia.cms.util.RequestDispatchUtil}.
     *
     * @param node         vanity url node
     * @param originSuffix origin url suffix
     * @return redirect url
     */
    protected String createRedirectUrl(final Node node, String originSuffix) {
        return createRedirectUrl(node, false, originSuffix);
    }

    /**
     * Creates the redirect url for uri mapping.
     * Without context path, because of Magnolia's {@link info.magnolia.cms.util.RequestDispatchUtil}.
     *
     * @param node         vanity url node
     * @param asExternal   external link flag
     * @param originSuffix origin url suffix
     * @return redirect url
     */
    protected String createRedirectUrl(final Node node, final boolean asExternal, final String originSuffix) {
        String result;
        String type = getString(node, PN_TYPE, EMPTY);
        String prefix;
        if ("forward".equals(type)) {
            result = createForwardLink(node);
            prefix = FORWARD_PREFIX;
        } else {
            result = createTargetLink(node, asExternal, originSuffix);
            prefix = "301".equals(type) ? PERMANENT_PREFIX : REDIRECT_PREFIX;
        }
        if (isNotEmpty(result)) {
            result = prefix + result;
        }
        return result;
    }

    /**
     * Creates the public url for displaying as target link in app view.
     *
     * @param node vanity url node
     * @return public url
     */
    public String createPublicUrl(final Node node) {
        PublicUrlService publicUrlService = _vanityUrlModule.get().getPublicUrlService();
        return publicUrlService.createTargetUrl(node);
    }

    /**
     * Creates the vanity url for public instance, stored in qr code.
     *
     * @param node vanity url node
     * @return vanity url
     */
    public String createVanityUrl(final Node node) {
        PublicUrlService publicUrlService = _vanityUrlModule.get().getPublicUrlService();
        return publicUrlService.createVanityUrl(node);
    }

    /**
     * Creates the preview url for app preview.
     * Without contextPath, because of Magnolia's app framework.
     *
     * @param node vanity url node
     * @return preview url
     */
    public String createPreviewUrl(final Node node) {
        return createTargetLink(node, false, null);
    }

    private String createForwardLink(final Node node) {
        // nearly the same functionality as in createTargetLink. the only difference
        // is the clearing of the url if an external url had been configured
        return createTargetLink(node, true, false, null);
    }

    private String createTargetLink(final Node node, final boolean asExternal, String originSuffix) {
        return createTargetLink(node, false, asExternal, originSuffix);
    }

    private String createTargetLink(final Node node, final boolean isForward, final boolean asExternal, String originSuffix) {
        String url = EMPTY;
        if (node != null) {
            String linkValue = getString(node, PN_LINK, EMPTY);
            if (isNotEmpty(linkValue)) {
                if (isExternalLink(linkValue)) {
                    // we won't allow external links in a forward
                    if (!isForward) {
                        url = linkValue;
                    }
                } else {
                    Node nodeFromId = getNodeFromId(linkValue);
                    url = createTargetLinkForPage(nodeFromId, isForward, asExternal);
                }
            }

            if (isNotEmpty(url) && !isForward) {
                url += defaultString(getString(node, PN_SUFFIX, originSuffix));
            }
        }
        return url;
    }

    private String createTargetLinkForPage(Node pageNode, boolean isForward, boolean asExternal) {
        String url = EMPTY;
        if (pageNode != null) {
            if (asExternal) {
                url = LinkUtil.createExternalLink(pageNode);
            } else {
                url = getLinkFromNode(pageNode, isForward);
                if (isNotBlank(url) && url.contains(_contextPath)) {
                    url = substringAfter(url, _contextPath);
                }
            }
        }
        return url;
    }

    /**
     * Create the link to the qr image without context path.
     *
     * @param node vanity url node
     * @return link to qr image
     */
    public String createImageLink(final Node node) {
        String link = EMPTY;
        try {
            if (node != null && node.hasNode(NN_IMAGE)) {
                link = getLinkFromNode(node.getNode(NN_IMAGE), false);
                link = removeStart(defaultString(link), _contextPath);
                link = replace(link, "." + DEFAULT_EXTENSION, VanityUrlSaveFormAction.IMAGE_EXTENSION);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error creating link to image property.", e);
        }
        return link;
    }

    /**
     * Query for a vanity url node.
     *
     * @param vanityUrl vanity url from request
     * @param siteName  site name from aggegation state
     * @return first vanity url node of result or null, if nothing found
     */
    public Node queryForVanityUrlNode(final String vanityUrl, final String siteName) {
        Node node = null;

        List<Node> nodes = queryForVanityUrlNodes(vanityUrl, siteName);
        if (!nodes.isEmpty()) {
            node = nodes.get(0);
        }

        return node;
    }

    /**
     * Query for a vanity url nodes.
     *
     * @param vanityUrl vanity url from request
     * @param siteName  site name from aggegation state
     * @return vanity url nodes or empty list, if nothing found
     */
    public List<Node> queryForVanityUrlNodes(final String vanityUrl, final String siteName) {
        List<Node> nodes = Collections.emptyList();

        try {
            Session jcrSession = MgnlContext.getJCRSession(VanityUrlModule.WORKSPACE);
            QueryManager queryManager = jcrSession.getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(QUERY, JCR_SQL2);
            query.bindValue(PN_VANITY_URL, new StringValue(vanityUrl));
            query.bindValue(PN_SITE, new StringValue(siteName));
            QueryResult queryResult = query.execute();
            nodes = asList(asIterable(queryResult.getNodes()));
        } catch (RepositoryException e) {
            LOGGER.error("Error message.", e);
        }

        return nodes;
    }

    /**
     * Override for testing.
     */
    protected String getLinkFromNode(final Node node, boolean isForward) {
        return isForward ? NodeUtil.getPathIfPossible(node) : LinkUtil.createLink(node);
    }

    @Inject
    public void setVanityUrlModule(final Provider<VanityUrlModule> vanityUrlModule) {
        _vanityUrlModule = vanityUrlModule;
    }

    protected static Node getNodeFromId(final String nodeId) {
        Node node = null;
        try {
            Session jcrSession = MgnlContext.getJCRSession(WEBSITE);
            node = jcrSession.getNodeByIdentifier(nodeId);
        } catch (RepositoryException e) {
            LOGGER.info("Error getting node for {}.", nodeId);
            LOGGER.debug("Error getting node for {}.", nodeId, e);
        }
        return node;
    }

    static boolean isExternalLink(String linkValue) {
        return startsWithIgnoreCase(linkValue, "https://") || startsWithIgnoreCase(linkValue, "http://");
    }
}
