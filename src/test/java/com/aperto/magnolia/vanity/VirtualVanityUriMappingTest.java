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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.virtualuri.VirtualUriMapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for mapping.
 *
 * @author frank.sommer
 * @since 28.05.14
 */
public class VirtualVanityUriMappingTest {

    private VirtualVanityUriMapping _uriMapping;

    @Test
    public void testRootRequest() throws Exception {
        Optional<VirtualUriMapping.Result> mappingResult = _uriMapping.mapUri(new URI("/"));
        assertThat(mappingResult, is(Optional.empty()));
    }

    @Test
    public void testPageRequest() throws Exception {
        Optional<VirtualUriMapping.Result> mappingResult = _uriMapping.mapUri(new URI("/home.html"));
        assertThat(mappingResult.isPresent(), is(false));
    }

    @Test
    public void testVanityUrlWithoutTarget() throws Exception {
        Optional<VirtualUriMapping.Result> mappingResult = _uriMapping.mapUri(new URI("/home"));
        assertThat(mappingResult, is(Optional.empty()));
    }

    @Test
    public void testVanityUrlWithTarget() throws Exception {
        Optional<VirtualUriMapping.Result> mappingResult = _uriMapping.mapUri(new URI("/xmas"));
        assertThat(mappingResult, notNullValue());
        assertThat(mappingResult.isPresent() ? mappingResult.get().getToUri() : "", equalTo("redirect:/internal/page.html"));
    }

    @Before
    public void setUp() throws Exception {
        _uriMapping = new VirtualVanityUriMapping();

        Provider moduleProvider = mock(Provider.class);
        VanityUrlModule module = new VanityUrlModule();
        Map<String, String> excludes = new HashMap<>();
        excludes.put("pages", ".*\\..*");
        module.setExcludes(excludes);
        when(moduleProvider.get()).thenReturn(module);
        _uriMapping.setVanityUrlModule(moduleProvider);

        Provider serviceProvider = mock(Provider.class);
        VanityUrlService vanityUrlService = mock(VanityUrlService.class);
        when(vanityUrlService.queryForVanityUrlNode("/home", "default")).thenReturn(null);

        MockNode mockNode = new MockNode("xmas");
        when(vanityUrlService.queryForVanityUrlNode("/xmas", "default")).thenReturn(mockNode);

        when(vanityUrlService.createRedirectUrl(mockNode)).thenReturn("redirect:/internal/page.html");
        when(serviceProvider.get()).thenReturn(vanityUrlService);
        _uriMapping.setVanityUrlService(serviceProvider);

        Provider registryProvider = mock(Provider.class);
        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
        when(registryProvider.get()).thenReturn(moduleRegistry);
        _uriMapping.setModuleRegistry(registryProvider);

        initWebContext();
        initComponentProvider();
    }

    private void initComponentProvider() {
        SystemContext systemContext = mock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);
    }

    private void initWebContext() {
        WebContext webContext = mock(WebContext.class);
        AggregationState aggregationState = mock(AggregationState.class);
        when(webContext.getAggregationState()).thenReturn(aggregationState);
        MgnlContext.setInstance(webContext);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }
}
