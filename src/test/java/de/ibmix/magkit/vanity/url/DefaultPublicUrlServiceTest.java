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

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.test.mock.jcr.MockNode;
import org.junit.Before;
import org.junit.Test;

import static de.ibmix.magkit.vanity.url.VanityUrlService.PN_LINK;
import static de.ibmix.magkit.vanity.url.VanityUrlService.PN_VANITY_URL;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for default public url service ({@link DefaultPublicUrlService}).
 *
 * @author frank.sommer
 * @since 16.10.14
 */
public class DefaultPublicUrlServiceTest {
    private DefaultPublicUrlService _service;

    @Test
    public void testExternalTarget() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/aperto");
        mockNode.setProperty(PN_LINK, "http://www.aperto.de");

        assertThat(_service.createTargetUrl(mockNode), equalTo("http://www.aperto.de"));
        assertThat(_service.createVanityUrl(mockNode), equalTo("http://www.aperto.de/aperto"));
    }

    @Test
    public void testInternalTarget() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/aperto");
        mockNode.setProperty(PN_LINK, "123-456-789");

        assertThat(_service.createTargetUrl(mockNode), equalTo("http://www.aperto.de/context/page.html"));
        assertThat(_service.createVanityUrl(mockNode), equalTo("http://www.aperto.de/aperto"));
    }

    @Test
    public void testExternalTargetWithConfiguredTargetContextPath() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/aperto");
        mockNode.setProperty(PN_LINK, "http://www.aperto.de");
        _service.setTargetContextPath("/public");

        assertThat(_service.createTargetUrl(mockNode), equalTo("http://www.aperto.de"));
        assertThat(_service.createVanityUrl(mockNode), equalTo("http://www.aperto.de/public/aperto"));
    }

    @Test
    public void testInternalTargetWithConfiguredTargetContextPath() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/aperto");
        mockNode.setProperty(PN_LINK, "123-456-789");
        _service.setTargetContextPath("/public");

        assertThat(_service.createTargetUrl(mockNode), equalTo("http://www.aperto.de/context/page.html"));
        assertThat(_service.createVanityUrl(mockNode), equalTo("http://www.aperto.de/public/aperto"));
    }

    @Before
    public void setUp() {
        _service = new DefaultPublicUrlService() {
            @Override
            public String getExternalLinkFromId(final String nodeId) {
                return "http://www.aperto.de/context/page.html";
            }
        };

        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        when(serverConfiguration.getDefaultBaseUrl()).thenReturn("http://www.aperto.de/author");
        _service.setServerConfiguration(serverConfiguration);
        _service.setContextPath("/author");
    }
}
