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

import com.machinezoo.noexception.Exceptions;
import com.vaadin.ui.Notification;
import de.ibmix.magkit.vanityurl.PreviewImageConfig;
import de.ibmix.magkit.vanityurl.VanityUrlModule;
import de.ibmix.magkit.vanityurl.VanityUrlService;
import info.magnolia.cms.core.FileSystemHelper;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeTypes.Resource;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.ContentBrowserSubApp;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.detail.action.SaveDetailSubAppAction;
import info.magnolia.ui.contentapp.detail.action.SaveDetailSubAppActionDefinition;
import info.magnolia.ui.datasource.ItemResolver;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.observation.DatasourceObservation;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import net.glxn.qrgen.javase.QRCode;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import static de.ibmix.magkit.vanityurl.PreviewImageConfig.ImageType.SVG;
import static de.ibmix.magkit.vanityurl.VanityUrlService.NN_IMAGE;
import static de.ibmix.magkit.vanityurl.VanityUrlService.PN_SITE;
import static de.ibmix.magkit.vanityurl.VanityUrlService.PN_VANITY_URL;
import static de.ibmix.magkit.vanityurl.VanityUrlService.getImageType;
import static info.magnolia.cms.beans.runtime.File.PROPERTY_CONTENTTYPE;
import static info.magnolia.cms.beans.runtime.File.PROPERTY_FILENAME;
import static info.magnolia.cms.beans.runtime.File.PROPERTY_LASTMODIFIED;
import static info.magnolia.jcr.util.NodeUtil.getNodeIdentifierIfPossible;
import static info.magnolia.jcr.util.PropertyUtil.getPropertyOrNull;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static info.magnolia.jcr.util.PropertyUtil.setProperty;
import static org.apache.commons.lang3.StringUtils.strip;
import static org.apache.commons.lang3.StringUtils.stripStart;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.jackrabbit.JcrConstants.JCR_DATA;

/**
 * Saves additional to the form fields a qr code image as preview image.
 *
 * @author frank.sommer
 * @since 28.05.14
 */
public class VanityUrlSaveFormAction extends SaveDetailSubAppAction<Node> {
    private static final Logger LOGGER = LoggerFactory.getLogger(VanityUrlSaveFormAction.class);
    private static final int QR_WIDTH = 500;
    private static final int QR_HEIGHT = 500;

    private final AppContext _appContext;
    private final LocationController _locationController;
    private final ItemResolver<Node> _itemResolver;
    private final Provider<VanityUrlModule> _vanityUrlModule;
    private final SimpleTranslator _simpleTranslator;
    private final VanityUrlService _vanityUrlService;
    private final NodeNameHelper _nodeNameHelper;
    private final FileSystemHelper _fileSystemHelper;

    //CHECKSTYLE:OFF
    @Inject
    public VanityUrlSaveFormAction(SaveDetailSubAppActionDefinition definition, CloseHandler closeHandler, ValueContext<Node> valueContext, EditorView<Node> form, Datasource<Node> datasource, DatasourceObservation.Manual datasourceObservation, LocationController locationController, AppContext appContext, ItemResolver<Node> itemResolver, Provider<VanityUrlModule> vanityUrlModule, SimpleTranslator simpleTranslator, VanityUrlService vanityUrlService, NodeNameHelper nodeNameHelper, FileSystemHelper fileSystemHelper) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation, locationController, appContext, itemResolver);
        _appContext = appContext;
        _locationController = locationController;
        _itemResolver = itemResolver;
        _vanityUrlModule = vanityUrlModule;
        _simpleTranslator = simpleTranslator;
        _vanityUrlService = vanityUrlService;
        _nodeNameHelper = nodeNameHelper;
        _fileSystemHelper = fileSystemHelper;
    }
    //CHECKSTYLE:ON

    @Override
    protected boolean validateForm() {
        boolean isValid = super.validateForm();
        if (isValid && getValueContext().getSingle().isPresent()) {
            Node node = getValueContext().getSingle().get();
            getForm().write(node);

            String site = getString(node, PN_SITE);
            String vanityUrl = getString(node, PN_VANITY_URL);
            if (site != null && vanityUrl != null) {
                List<Node> nodes = _vanityUrlService.queryForVanityUrlNodes(vanityUrl, site);
                String currentIdentifier = getNodeIdentifierIfPossible(node);
                for (Node resultNode : nodes) {
                    if (!currentIdentifier.equals(getNodeIdentifierIfPossible(resultNode))) {
                        isValid = false;
                        AlertBuilder.alert(_simpleTranslator.translate("actions.commit.failureMessage"))
                            .withLevel(Notification.Type.WARNING_MESSAGE)
                            .withBody(_simpleTranslator.translate("vanityUrl.errorMessage.notUnique"))
                            .withOkButtonCaption(_simpleTranslator.translate("button.ok"))
                            .buildAndOpen();
                        break;
                    }
                }
            }
        }
        return isValid;
    }

    @Override
    protected void write() {
        getValueContext().getSingle().ifPresent(Exceptions.wrap().consumer(
            item -> {
                setNodeName(item);
                setPreviewImage(item);

                getDatasource().save(item);
                getDatasourceObservation().trigger();
            }
        ));

        // update location after saving content
        _locationController.goTo(
            new ContentBrowserSubApp.BrowserLocation(
                _appContext.getName(), "browser", getValueContext().getSingle().map(_itemResolver::getId).orElse("")
            )
        );
    }

    private String getNormalizedVanityUrl(final Node node) {
        String vanityUrl = getString(node, PN_VANITY_URL);
        vanityUrl = stripStart(trimToEmpty(vanityUrl), "/");
        return vanityUrl;
    }

    private void setPreviewImage(final Node node) {
        String url = _vanityUrlService.createVanityUrl(node);
        String fileName = trim(strip(getString(node, PN_VANITY_URL, ""), "/")).replace("/", "-");

        final PreviewImageConfig.ImageType imageType = getImageType(_vanityUrlModule.get());
        final File qrCodeFile = generateQrCode(url, fileName, imageType);

        saveToNode(node, qrCodeFile, fileName, imageType);
    }

    private void saveToNode(Node node, File qrCodeFile, String fileName, PreviewImageConfig.ImageType imageType) {
        try (FileInputStream qrCodeInputStream = new FileInputStream(qrCodeFile)) {
            Node qrNode;
            if (node.hasNode(NN_IMAGE)) {
                qrNode = node.getNode(NN_IMAGE);
            } else {
                qrNode = node.addNode(NN_IMAGE, Resource.NAME);
            }

            populateItem(qrNode, qrCodeInputStream, fileName, imageType.getMimeType());
        } catch (RepositoryException e) {
            LOGGER.error("Error on saving preview image for vanity url.", e);
        } catch (IOException e) {
            LOGGER.error("Error reading qr image temp file.", e);
        }
    }

    private File generateQrCode(String url, String fileName, PreviewImageConfig.ImageType imageType) {
        File tmpQrCodeFile = new File(_fileSystemHelper.getTempDirectory(), fileName + imageType.getExtension());

        try (FileOutputStream outputStream = new FileOutputStream(tmpQrCodeFile)) {
            if (imageType == SVG) {
                QRCode.from(url).svg(outputStream);
            } else {
                QRCode.from(url).withSize(QR_WIDTH, QR_HEIGHT).writeTo(outputStream);
            }
            outputStream.flush();
        } catch (IOException e) {
            LOGGER.error("Error writing temp file for qr code.", e);
        }

        return tmpQrCodeFile;
    }

    private void populateItem(Node qrCodeNode, InputStream inputStream, final String fileName, String mimeType) {
        try {
            Property data = getPropertyOrNull(qrCodeNode, JCR_DATA);
            Binary binary = ValueFactoryImpl.getInstance().createBinary(inputStream);
            if (data == null) {
                qrCodeNode.setProperty(JCR_DATA, binary);
            } else {
                data.setValue(binary);
            }

            setProperty(qrCodeNode, PROPERTY_FILENAME, fileName);
            setProperty(qrCodeNode, PROPERTY_CONTENTTYPE, mimeType);
            Calendar calValue = new GregorianCalendar(TimeZone.getDefault());
            setProperty(qrCodeNode, PROPERTY_LASTMODIFIED, calValue);
        } catch (RepositoryException re) {
            LOGGER.error("Could not get Binary. Upload will not be performed", re);
        }
    }

    private void setNodeName(Node node) throws RepositoryException {
        if (node.hasProperty(PN_VANITY_URL) && !node.hasProperty("jcrName")) {
            String newNodeName = _nodeNameHelper.getValidatedName(getNormalizedVanityUrl(node));
            if (!node.getName().equals(newNodeName)) {
                newNodeName = _nodeNameHelper.getUniqueName(node.getParent(), newNodeName);
                NodeUtil.renameNode(node, newNodeName);
            }
        }
    }
}
