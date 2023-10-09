package de.ibmix.magkit.vanityurl;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

/**
 * Test the service class.
 *
 * @author frank.sommer
 * @since 28.05.14
 */
public class VanityUrlServiceTest {

    private static final String TEST_UUID = "123-4556-123";
    private static final String TEST_UUID_FORWARD = "123-4556-124";

    private VanityUrlService _service;

    @Test
    public void testRedirectWithNull() {
        assertThat(_service.createRedirectUrl(null), equalTo(""));
    }

    @Test
    public void testTargetUrlWithEmptyNode() {
        MockNode mockNode = new MockNode("node");
        assertThat(_service.createRedirectUrl(mockNode), equalTo(""));
        assertThat(_service.createPreviewUrl(mockNode), equalTo(""));
    }

    @Test
    public void testTargetUrlInternalWithAnchor() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", TEST_UUID);
        mockNode.setProperty("linkSuffix", "#anchor1");

        assertThat(_service.createRedirectUrl(mockNode), equalTo("redirect:/internal/page.html#anchor1"));
        assertThat(_service.createPreviewUrl(mockNode), equalTo("/internal/page.html#anchor1"));
    }

    @Test
    public void testTargetUrlExternal() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", "http://www.aperto.de");

        assertThat(_service.createRedirectUrl(mockNode), equalTo("redirect:http://www.aperto.de"));
        assertThat(_service.createPreviewUrl(mockNode), equalTo("http://www.aperto.de"));
    }

    @Test
    public void testForward() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", TEST_UUID_FORWARD);
        mockNode.setProperty("type", "forward");
        assertThat(_service.createRedirectUrl(mockNode), equalTo("forward:/internal/forward/page.html"));
    }

    @Test
    public void testForwardWithAnchor() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", TEST_UUID_FORWARD);
        mockNode.setProperty("type", "forward");
        mockNode.setProperty("linkSuffix", "#anchor1");
        assertThat(_service.createRedirectUrl(mockNode), equalTo("forward:/internal/forward/page.html#anchor1"));
    }

    @Test
    public void testForwardWithInvalidUrl() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", "http://www.aperto.de");
        mockNode.setProperty("type", "forward");
        assertThat(_service.createRedirectUrl(mockNode), equalTo(""));
    }

    @Test
    public void testPublicUrl() {
        assertThat(_service.createPublicUrl(null), equalTo("http://www.aperto.de/page.html"));
    }

    @Test
    public void testVanityUrl() {
        assertThat(_service.createVanityUrl(null), equalTo("http://www.aperto.de/vanity"));
    }

    @Test
    public void testImageLinkWithNull() {
        assertThat(_service.createImageLink(null), equalTo(""));
    }

    @Test
    public void testImageLinkWithMissingImage() {
        MockNode mockNode = new MockNode("node");
        assertThat(_service.createImageLink(mockNode), equalTo(""));
    }

    @Test
    public void testImageLinkWithImage() {
        MockNode mockNode = new MockNode("node");
        MockNode image = new MockNode(VanityUrlService.NN_IMAGE);
        mockNode.addNode(image);
        assertThat(_service.createImageLink(mockNode), equalTo("/node/qrCode.png"));
    }

    private MockNode createNode(MockSession session, String path) throws Exception {
        MockNode current = (MockNode) session.getRootNode();
        String[] parts = path.split("/");
        for (String part : parts) {
            MockNode child;
            if (current.hasNode(part)) {
                child = (MockNode) current.getNode(part);
            } else {
                child = new MockNode(part);
                current.addNode(child);
            }
            current = child;
        }
        return current;
    }

    @Before
    public void setUp() throws Exception {
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        MockWebContext webContext = new MockWebContext();
        MockSession session = new MockSession(RepositoryConstants.WEBSITE);
        createNode(session, "/internal/forward/page").setIdentifier(TEST_UUID_FORWARD);
        createNode(session, "/internal/page").setIdentifier(TEST_UUID);
        webContext.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(webContext);
        _service = new VanityUrlService() {

            @Override
            protected String getLinkFromNode(final Node node, final boolean isForward) {
                String link = "";
                if (node != null) {
                    try {
                        link = node.getPath() + ".html";
                    } catch (RepositoryException e) {
                        // should not happen
                    }
                }
                return link;
            }
        };

        Provider<VanityUrlModule> moduleProvider = mock(Provider.class);
        VanityUrlModule vanityUrlModule = new VanityUrlModule();
        PublicUrlService publicUrlService = mock(PublicUrlService.class);
        when(publicUrlService.createTargetUrl(any())).thenReturn("http://www.aperto.de/page.html");
        when(publicUrlService.createVanityUrl(any())).thenReturn("http://www.aperto.de/vanity");
        vanityUrlModule.setPublicUrlService(publicUrlService);
        when(moduleProvider.get()).thenReturn(vanityUrlModule);
        _service.setVanityUrlModule(moduleProvider);
    }
}
