package com.aperto.magnolia.vanity.app;

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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.datasource.DatasourceType;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.datasource.optionlist.OptionListDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;

import static com.aperto.magnolia.vanity.VanityUrlService.DEF_SITE;
import static info.magnolia.jcr.util.NodeTypes.ContentNode;
import static info.magnolia.jcr.util.NodeUtil.asList;
import static info.magnolia.repository.RepositoryConstants.CONFIG;

/**
 * Extends for site select options.
 *
 * @author frank.sommer
 * @since 05.05.14
 */
@DatasourceType("siteListDatasource")
public class SiteSelectFieldDefinition extends OptionListDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(SiteSelectFieldDefinition.class);
    private static final String SITE_LOCATION = "/modules/multisite/config/sites";

    public SiteSelectFieldDefinition() {
        setName("sitelist");
    }

    @Override
    public List<Option> getOptions() {
        List<Option> options = new ArrayList<>();

        final List<Node> nodes = getNodes();
        if (nodes.isEmpty()) {
            LOGGER.debug("No site nodes found.");
            options.add(createOptionDefinition(DEF_SITE));
        } else {
            LOGGER.debug("{} site nodes found.", nodes.size());
            for (Node node : nodes) {
                options.add(createOptionDefinition(NodeUtil.getName(node)));
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

    private List<Node> getNodes() {
        List<Node> nodes = new ArrayList<>();

        try {
            MgnlContext.doInSystemContext(new MgnlContext.RepositoryOp() {
                @Override
                public void doExec() throws RepositoryException {
                    Session jcrSession = MgnlContext.getJCRSession(CONFIG);
                    if (jcrSession.nodeExists(SITE_LOCATION)) {
                        Node siteBaseNode = jcrSession.getNode(SITE_LOCATION);
                        nodes.addAll(asList(NodeUtil.getNodes(siteBaseNode, ContentNode.NAME)));
                    }
                }
            });
        } catch (RepositoryException e) {
            LOGGER.error("Error getting site nodes.", e);
        }

        return nodes;
    }
}
