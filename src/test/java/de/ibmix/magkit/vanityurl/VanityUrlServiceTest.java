package de.ibmix.magkit.vanityurl;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.junit5.Component;
import info.magnolia.test.junit5.MagnoliaTest;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;
import info.magnolia.test.mock.jcr.MockSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static info.magnolia.cms.beans.runtime.File.PROPERTY_CONTENTTYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
@MagnoliaTest
public class VanityUrlServiceTest {

    private static final String TEST_UUID = "123-4556-123";
    private static final String TEST_UUID_FORWARD = "123-4556-124";

    private VanityUrlService _service;

    @Test
    public void testRedirectWithNull() {
        assertEquals("", _service.createRedirectUrl(null, null));
    }

    @Test
    public void testTargetUrlWithEmptyNode() {
        MockNode mockNode = new MockNode("node");
        assertEquals("", _service.createRedirectUrl(mockNode, null));
        assertEquals("", _service.createPreviewUrl(mockNode));
    }

    @Test
    public void testTargetUrlInternalWithAnchor() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", TEST_UUID);
        mockNode.setProperty("linkSuffix", "#anchor1");

        assertEquals("redirect:/internal/page.html#anchor1", _service.createRedirectUrl(mockNode, null));
        assertEquals("/internal/page.html#anchor1", _service.createPreviewUrl(mockNode));
    }

    @Test
    public void testTargetUrlExternal() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", "http://www.ibmix.de");

        assertEquals("redirect:http://www.ibmix.de", _service.createRedirectUrl(mockNode, null));
        assertEquals("http://www.ibmix.de", _service.createPreviewUrl(mockNode));
    }

    @Test
    public void testForward() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", TEST_UUID_FORWARD);
        mockNode.setProperty("type", "forward");
        assertEquals("forward:/internal/forward/page.html", _service.createRedirectUrl(mockNode, null));
    }

    @Test
    public void testRedirectWithAnchor() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", TEST_UUID_FORWARD);
        mockNode.setProperty("type", "301");
        mockNode.setProperty("linkSuffix", "#anchor1");
        assertEquals("permanent:/internal/forward/page.html#anchor1", _service.createRedirectUrl(mockNode, null));
    }

    @Test
    public void testRedirectWithOriginSuffix() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", TEST_UUID_FORWARD);
        mockNode.setProperty("type", "301");
        assertEquals("permanent:/internal/forward/page.html?test=1", _service.createRedirectUrl(mockNode, "?test=1"));
    }

    @Test
    public void testForwardWithInvalidUrl() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty("link", "http://www.ibmix.de");
        mockNode.setProperty("type", "forward");
        assertEquals("", _service.createRedirectUrl(mockNode, null));
    }

    @Test
    public void testPublicUrl() {
        assertEquals("http://www.ibmix.de/page.html", _service.createPublicUrl(null));
    }

    @Test
    public void testVanityUrl() {
        assertEquals("http://www.ibmix.de/vanity", _service.createVanityUrl(null));
    }

    @Test
    public void testImageLinkWithNull() {
        assertEquals("", _service.createImageLink(null));
    }

    @Test
    public void testImageLinkWithMissingImage() {
        MockNode mockNode = new MockNode("node");
        assertEquals("", _service.createImageLink(mockNode));
    }

    @Test
    public void testImageLinkWithPngImage() {
        MockNode mockNode = new MockNode("node");
        MockNode image = new MockNode(VanityUrlService.NN_IMAGE);
        image.addProperty(new MockProperty(PROPERTY_CONTENTTYPE, PreviewImageConfig.ImageType.PNG.getMimeType(), image));
        mockNode.addNode(image);
        assertEquals("/node/qrCode.png", _service.createImageLink(mockNode));
    }

    @Test
    public void testImageLinkWithSvgImage() {
        MockNode mockNode = new MockNode("node");
        MockNode image = new MockNode(VanityUrlService.NN_IMAGE);
        mockNode.addNode(image);
        assertEquals("/node/qrCode.svg", _service.createImageLink(mockNode));
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

    @BeforeEach
    @Component(type = I18nContentSupport.class, implementation = Component.Mock.class)
    public void setUp() throws Exception {
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

        VanityUrlModule vanityUrlModule = new VanityUrlModule();
        PublicUrlService publicUrlService = mock(PublicUrlService.class);
        when(publicUrlService.createTargetUrl(any())).thenReturn("http://www.ibmix.de/page.html");
        when(publicUrlService.createVanityUrl(any())).thenReturn("http://www.ibmix.de/vanity");
        vanityUrlModule.setPublicUrlService(publicUrlService);
        _service.setVanityUrlModule(() -> vanityUrlModule);
    }
}
