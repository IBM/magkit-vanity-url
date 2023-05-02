package com.aperto.magnolia.vanity;

/*
 * #%L
 * magnolia-vanity-url Magnolia Module
 * %%
 * Copyright (C) 2013 - 2014 Aperto AG
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


import info.magnolia.test.mock.jcr.MockNode;
import org.junit.Before;
import org.junit.Test;

import static com.aperto.magnolia.vanity.VanityUrlService.PN_LINK;
import static com.aperto.magnolia.vanity.VanityUrlService.PN_VANITY_URL;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test for simple public url service ({@link SimplePublicUrlService}).
 *
 * @author frank.sommer
 * @since 16.10.14
 */
public class SimplePublicUrlServiceTest {
    private SimplePublicUrlService _service;

    @Test
    public void testExternalTarget() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/aperto");
        mockNode.setProperty(PN_LINK, "http://www.aperto.de");

        assertThat(_service.createTargetUrl(mockNode), equalTo("http://www.aperto.de"));
        assertThat(_service.createVanityUrl(mockNode), equalTo("http://www.demo-project.com/context/aperto"));
    }

    @Test
    public void testInternalTarget() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/aperto");
        mockNode.setProperty(PN_LINK, "123-456-789");

        assertThat(_service.createTargetUrl(mockNode), equalTo("http://www.demo-project.com/context/internal/page.html"));
        assertThat(_service.createVanityUrl(mockNode), equalTo("http://www.demo-project.com/context/aperto"));
    }

    @Test
    public void testInternalTargetWithConfiguredPrefix() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/aperto");
        mockNode.setProperty(PN_LINK, "123-456-789");
        _service.setTargetServerPrefix("http://www.aperto.de");

        assertThat(_service.createTargetUrl(mockNode), equalTo("http://www.aperto.de/internal/page.html"));
        assertThat(_service.createVanityUrl(mockNode), equalTo("http://www.aperto.de/aperto"));
    }

    @Test
    public void testInternalTargetWithSlashEndingConfiguredPrefix() throws Exception {
        MockNode mockNode = new MockNode("node");
        mockNode.setProperty(PN_VANITY_URL, "/aperto");
        mockNode.setProperty(PN_LINK, "123-456-789");
        _service.setTargetServerPrefix("http://www.aperto.de/");

        assertThat(_service.createTargetUrl(mockNode), equalTo("http://www.aperto.de/internal/page.html"));
        assertThat(_service.createVanityUrl(mockNode), equalTo("http://www.aperto.de/aperto"));
    }

    @Before
    public void setUp() {
        _service = new SimplePublicUrlService() {
            @Override
            public String getExternalLinkFromId(final String nodeId) {
                return "/internal/page.html";
            }
        };
    }
}
