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

import com.vaadin.server.StreamResource;
import com.vaadin.ui.UI;
import de.ibmix.magkit.vanityurl.PreviewImageConfig;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.vaadin.server.AttachmentStreamResource;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.InputStream;

import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static com.vaadin.util.EncodeUtil.rfc5987Encode;
import static de.ibmix.magkit.vanityurl.VanityUrlService.NN_IMAGE;
import static info.magnolia.cms.beans.runtime.File.PROPERTY_CONTENTTYPE;
import static info.magnolia.cms.beans.runtime.File.PROPERTY_FILENAME;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.jackrabbit.JcrConstants.JCR_DATA;

/**
 * Action to download QR code image for a vanity url.
 *
 * @author IBM iX
 */
public class QrCodeDownloadAction extends AbstractAction<ConfiguredActionDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QrCodeDownloadAction.class);

    private final ValueContext<Node> _valueContext;

    @Inject
    public QrCodeDownloadAction(ConfiguredActionDefinition definition, ValueContext<Node> valueContext) {
        super(definition);
        _valueContext = valueContext;
    }

    @Override
    public void execute() {
        LOGGER.debug("Execute qr code download action ...");
        if (_valueContext.getSingle().isPresent()) {
            StreamResource streamResource = getStreamResource(_valueContext.getSingle().get());
            if (streamResource != null) {
                UI.getCurrent().getPage().open(streamResource, null, false);
            }
        }
    }

    private StreamResource getStreamResource(final Node node) {
        AttachmentStreamResource resource = null;
        try {
            if (node != null && node.hasNode(NN_IMAGE)) {
                Node binaryNode = node.getNode(NN_IMAGE);
                Binary binary = binaryNode.getProperty(JCR_DATA).getBinary();
                InputStream inputStream = binary.getStream();
                final String mimeType = defaultIfEmpty(PropertyUtil.getString(binaryNode, PROPERTY_CONTENTTYPE), PreviewImageConfig.ImageType.SVG.getMimeType());
                String fileName = getFileName(binaryNode, mimeType);
                resource = new AttachmentStreamResource((StreamResource.StreamSource) () -> inputStream, fileName);

                // A negative value will disable caching of this stream.
                resource.setCacheTime(-1);
                String encodedFilename = rfc5987Encode(fileName);
                resource.getStream().setParameter(CONTENT_DISPOSITION, String.format("attachment;filename=\"%s\"; filename*=utf-8''%s", encodedFilename, encodedFilename));
                resource.setMIMEType(mimeType);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error creating download of qr image.", e);
        }
        return resource;
    }

    private String getFileName(Node binaryNode, String mimeType) {
        String fileName = PropertyUtil.getString(binaryNode, PROPERTY_FILENAME, "no-name");
        String extension = PreviewImageConfig.ImageType.getExtensionByMimeType(mimeType);
        return fileName.endsWith(extension) ? fileName : fileName + extension;
    }
}
