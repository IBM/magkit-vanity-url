package de.ibmix.magkit.vanityurl.app;

/*
 * #%L
 * magkit-vanity-url Magnolia Module
 * %%
 * Copyright (C) 2013 - 2021 IBM iX
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


import de.ibmix.magkit.vanityurl.VanityUrlService;
import com.machinezoo.noexception.Exceptions;
import com.vaadin.data.ValueProvider;

import jakarta.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;

/**
 * Vanity url link column value provider.
 *
 * @author frank.sommer
 * @since 1.6.0
 */
public class VanityLinkProvider implements ValueProvider<Item, String> {
    private final VanityUrlService _vanityUrlService;

    @Inject
    public VanityLinkProvider(VanityUrlService vanityUrlService) {
        _vanityUrlService = vanityUrlService;
    }

    @Override
    public String apply(Item item) {
        return Exceptions.wrap().get(() -> _vanityUrlService.createPublicUrl((Node) item));
    }
}
