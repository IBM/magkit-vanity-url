package de.ibmix.magkit.vanityurl.setup;

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

import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

import static de.ibmix.magkit.vanityurl.VanityUrlModule.WORKSPACE;
import static info.magnolia.jcr.nodebuilder.Ops.addNode;
import static info.magnolia.jcr.nodebuilder.Ops.addProperty;
import static info.magnolia.jcr.nodebuilder.task.ErrorHandling.logging;
import static info.magnolia.jcr.util.NodeTypes.ContentNode;
import static info.magnolia.repository.RepositoryConstants.CONFIG;

/**
 * Module version handler of this magnolia module.
 *
 * @author frank.sommer
 */
public class VanityUrlModuleVersionHandler extends DefaultModuleVersionHandler {

    private final RemoveNodeTask _removeOldModuleConfig = new RemoveNodeTask("Remove old module config", "/modules/magnolia-vanity-url");

    public VanityUrlModuleVersionHandler() {
        register(DeltaBuilder.update("1.6.4", "Update for version 1.6.4").addTask(_removeOldModuleConfig));
    }

    private final Task _addUriRepositoryMapping = new NodeExistsDelegateTask("Check repository mapping", "Add uri to repository mapping for vanityUrls if missing.", CONFIG, "/server/URI2RepositoryMapping/mappings/" + WORKSPACE, null,
        new NodeBuilderTask("Add repository mapping", "", logging, CONFIG, "/server/URI2RepositoryMapping/mappings",
            addNode(WORKSPACE, ContentNode.NAME).then(
                addProperty("URIPrefix", (Object) ("/" + WORKSPACE)),
                addProperty("handlePrefix", (Object) ""),
                addProperty("repository", (Object) WORKSPACE)
            )
        )
    );

    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(_addUriRepositoryMapping);
        tasks.add(_removeOldModuleConfig);
        return tasks;
    }
}
