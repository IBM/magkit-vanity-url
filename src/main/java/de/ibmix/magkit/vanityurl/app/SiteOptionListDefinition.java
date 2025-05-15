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

import info.magnolia.module.site.NullSite;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.datasource.DatasourceType;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.ibmix.magkit.vanityurl.VanityUrlService.DEF_SITE;

/**
 * Data source implementation for site options.
 *
 * @author frank.sommer
 * @since 05.05.14
 */
@DatasourceType("siteListDatasource")
public class SiteOptionListDefinition extends OptionListDefinition {

    private static final String MULTI_SITE_FALLBACK = "fallback";

    public SiteOptionListDefinition() {
        setName("sitelist");
    }

    @Override
    public List<Option> getOptions() {
        return getSites().stream().map(Site::getName).sorted((siteName1, siteName2) -> {
            int compareValue;
            if (siteName1.equals(MULTI_SITE_FALLBACK)) {
                compareValue = -1;
            } else if (siteName2.equals(MULTI_SITE_FALLBACK)) {
                compareValue = 1;
            } else {
                compareValue = siteName1.compareTo(siteName2);
            }
            return compareValue;
        }).map(this::createOptionDefinition).collect(Collectors.toList());
    }

    private Option createOptionDefinition(final String name) {
        String optionName = name.equals(NullSite.SITE_NAME) ? DEF_SITE : name;
        final Option def = new Option();
        def.setName(optionName);
        def.setLabel(optionName);
        def.setValue(optionName);
        return def;
    }

    private Collection<Site> getSites() {
        return Components.getComponent(SiteManager.class).getSites();
    }
}
