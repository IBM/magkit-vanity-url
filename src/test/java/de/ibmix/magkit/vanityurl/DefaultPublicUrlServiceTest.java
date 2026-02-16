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
import info.magnolia.test.mock.jcr.MockNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.vanityurl.VanityUrlService.PN_LINK;
import static de.ibmix.magkit.vanityurl.VanityUrlService.PN_VANITY_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for default public url service ({@link DefaultPublicUrlService}).
 *
 * @author frank.sommer
 * @since 16.10.14
 */
class DefaultPublicUrlServiceTest {
    private DefaultPublicUrlService _service;

    @Test
    void testExternalTarget() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/ibmix");
        mockNode.setProperty(PN_LINK, "http://www.ibmix.de");

        assertEquals("http://www.ibmix.de", _service.createTargetUrl(mockNode));
        assertEquals("http://www.ibmix.de/ibmix", _service.createVanityUrl(mockNode));
    }

    @Test
    void testInternalTarget() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/ibmix");
        mockNode.setProperty(PN_LINK, "123-456-789");

        assertEquals("http://www.ibmix.de/context/page.html", _service.createTargetUrl(mockNode));
        assertEquals("http://www.ibmix.de/ibmix", _service.createVanityUrl(mockNode));
    }

    @Test
    void testExternalTargetWithConfiguredTargetContextPath() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/ibmix");
        mockNode.setProperty(PN_LINK, "http://www.ibmix.de");
        _service.setTargetContextPath("/public");

        assertEquals("http://www.ibmix.de", _service.createTargetUrl(mockNode));
        assertEquals("http://www.ibmix.de/public/ibmix", _service.createVanityUrl(mockNode));
    }

    @Test
    void testInternalTargetWithConfiguredTargetContextPath() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/ibmix");
        mockNode.setProperty(PN_LINK, "123-456-789");
        _service.setTargetContextPath("/public");

        assertEquals("http://www.ibmix.de/context/page.html", _service.createTargetUrl(mockNode));
        assertEquals("http://www.ibmix.de/public/ibmix", _service.createVanityUrl(mockNode));
    }

    @BeforeEach
    void setUp() {
        _service = new DefaultPublicUrlService() {
            @Override
            public String getExternalLinkFromId(final String nodeId) {
                return "http://www.ibmix.de/context/page.html";
            }
        };

        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        when(serverConfiguration.getDefaultBaseUrl()).thenReturn("http://www.ibmix.de/author");
        _service.setServerConfiguration(serverConfiguration);
        _service.setContextPath("/author");
    }
}
