package de.ibmix.magkit.vanityurl.app;

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

import de.ibmix.magkit.vanityurl.VanityUrlService;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.action.OpenLocationActionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;

import static info.magnolia.ui.api.location.Location.LOCATION_TYPE_APP;

/**
 * Preview action for vanity urls. Opens the website page or the external url in the configured app.
 *
 * @author frank.sommer
 * @since 06.05.14
 */
public class PreviewAction extends AbstractAction<OpenLocationActionDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewAction.class);

    private final LocationController _locationController;
    private final ValueContext<Node> _valueContext;
    private VanityUrlService _vanityUrlService;

    @Inject
    public PreviewAction(OpenLocationActionDefinition definition, ValueContext<Node> valueContext, LocationController locationController) {
        super(definition);
        _locationController = locationController;
        _valueContext = valueContext;
    }

    @Override
    public void execute() {
        LOGGER.debug("Execute preview action ...");
        if (_valueContext.getSingle().isPresent()) {
            String link = _vanityUrlService.createPreviewUrl(_valueContext.getSingle().get());
            Location location = new DefaultLocation(LOCATION_TYPE_APP, getDefinition().getAppName(), getDefinition().getSubAppId(), link);
            _locationController.goTo(location);
        }
    }

    @Inject
    public void setVanityUrlService(final VanityUrlService vanityUrlService) {
        _vanityUrlService = vanityUrlService;
    }
}
