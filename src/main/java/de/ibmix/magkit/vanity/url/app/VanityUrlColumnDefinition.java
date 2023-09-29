package de.ibmix.magkit.vanity.url.app;

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


import com.machinezoo.noexception.Exceptions;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.contentapp.column.jcr.JcrTitleColumnDefinition;
import info.magnolia.ui.contentapp.configuration.column.ColumnType;

import javax.jcr.Item;
import javax.jcr.Node;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Column definition for vanity url column.
 *
 * @author frank.sommer
 * @since 1.6.0
 */
@ColumnType("vanityUrlColumn")
public class VanityUrlColumnDefinition extends JcrTitleColumnDefinition {

    public VanityUrlColumnDefinition() {
        setValueProvider(ValueProvider.class);
    }

    /**
     * Value provider.
     *
     * @author frank.sommer
     * @since 1.6.0
     */
    public static class ValueProvider extends JcrTitleValueProvider {
        public ValueProvider(VanityUrlColumnDefinition definition) {
            super(definition);
        }

        public String apply(Item item) {
            return Exceptions.wrap().get(() -> {
                Node node = (Node) item;
                if (!NodeUtil.isNodeType(node, NodeTypes.Folder.NAME) && !NodeUtil.hasMixin(node, NodeTypes.Deleted.NAME)) {
                    String vanityUrl = PropertyUtil.getString(node, getDefinition().getName());
                    if (isBlank(vanityUrl)) {
                        vanityUrl = item.getName();
                    }

                    return "<span class=\"v-table-icon-element " + getIcon(item) + "\" ></span>" + vanityUrl;
                } else {
                    return super.apply(item);
                }
            });
        }

        public VanityUrlColumnDefinition getDefinition() {
            return (VanityUrlColumnDefinition) super.getDefinition();
        }
    }
}
