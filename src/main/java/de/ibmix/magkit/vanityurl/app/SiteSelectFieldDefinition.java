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

import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.datasource.DatasourceType;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.ibmix.magkit.vanityurl.VanityUrlService.DEF_SITE;

/**
 * Extends for site select options.
 *
 * @author frank.sommer
 * @since 05.05.14
 */
@DatasourceType("siteListDatasource")
public class SiteSelectFieldDefinition extends OptionListDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(SiteSelectFieldDefinition.class);

    public SiteSelectFieldDefinition() {
        setName("sitelist");
    }

    @Override
    public List<Option> getOptions() {
        List<Option> options = new ArrayList<>();

        final Collection<Site> sites = getSites();
        if (sites.isEmpty()) {
            LOGGER.debug("No site nodes found.");
            options.add(createOptionDefinition(DEF_SITE));
        } else {
            LOGGER.debug("{} site nodes found.", sites.size());
            for (Site site : sites) {
                options.add(createOptionDefinition(site.getName()));
            }
        }
        return options;
    }

    private Option createOptionDefinition(final String name) {
        final Option def = new Option();
        def.setName(name);
        def.setLabel(name);
        def.setValue(name);
        return def;
    }

    private Collection<Site> getSites() {
        return Components.getComponent(SiteManager.class).getSites();
    }
}
