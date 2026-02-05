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
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.site.NullSite;
import info.magnolia.test.junit5.Component;
import info.magnolia.test.junit5.MagnoliaTest;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.virtualuri.VirtualUriMapping;
import jakarta.inject.Provider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for mapping.
 *
 * @author frank.sommer
 * @since 28.05.14
 */
@MagnoliaTest
public class VirtualVanityUriMappingTest {

    private VirtualVanityUriMapping _uriMapping;

    @Test
    public void testRootRequest() throws Exception {
        Optional<VirtualUriMapping.Result> mappingResult = _uriMapping.mapUri(new URI("/"));
        assertTrue(mappingResult.isEmpty());
    }

    @Test
    public void testPageRequest() throws Exception {
        Optional<VirtualUriMapping.Result> mappingResult = _uriMapping.mapUri(new URI("/home.html"));
        assertFalse(mappingResult.isPresent());
    }

    @Test
    public void testVanityUrlWithoutTarget() throws Exception {
        Optional<VirtualUriMapping.Result> mappingResult = _uriMapping.mapUri(new URI("/home"));
        assertTrue(mappingResult.isEmpty());
    }

    @Test
    public void testVanityUrlWithTarget() throws Exception {
        Optional<VirtualUriMapping.Result> mappingResult = _uriMapping.mapUri(new URI("/xmas"));
        assertNotNull(mappingResult);
        assertTrue(mappingResult.isPresent());
        assertEquals("redirect:/internal/page.html", mappingResult.get().getToUri());
    }

    @BeforeEach
    @Component(type = SystemContext.class, implementation = Component.Mock.class)
    public void setUp() {
        _uriMapping = new VirtualVanityUriMapping();

        Provider<VanityUrlModule> moduleProvider = mock(Provider.class);
        VanityUrlModule module = new VanityUrlModule();
        Map<String, String> excludes = new HashMap<>();
        excludes.put("pages", ".*\\..*");
        module.setExcludes(excludes);
        when(moduleProvider.get()).thenReturn(module);
        _uriMapping.setVanityUrlModule(moduleProvider);

        Provider<VanityUrlService> serviceProvider = mock(Provider.class);
        VanityUrlService vanityUrlService = mock(VanityUrlService.class);
        when(vanityUrlService.queryForVanityUrlNode("/home", NullSite.SITE_NAME)).thenReturn(null);

        MockNode mockNode = new MockNode("xmas");
        when(vanityUrlService.queryForVanityUrlNode("/xmas", NullSite.SITE_NAME)).thenReturn(mockNode);

        when(vanityUrlService.createRedirectUrl(mockNode, null)).thenReturn("redirect:/internal/page.html");
        when(serviceProvider.get()).thenReturn(vanityUrlService);
        _uriMapping.setVanityUrlService(serviceProvider);

        initWebContext();
    }

    private void initWebContext() {
        WebContext webContext = mock(WebContext.class);
        AggregationState aggregationState = mock(AggregationState.class);
        when(webContext.getAggregationState()).thenReturn(aggregationState);
        MgnlContext.setInstance(webContext);
    }

    @AfterEach
    public void tearDown() {
        MgnlContext.setInstance(null);
    }
}
