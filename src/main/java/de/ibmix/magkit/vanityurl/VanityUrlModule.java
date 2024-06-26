package de.ibmix.magkit.vanityurl;

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

import info.magnolia.objectfactory.Components;

import java.util.Collections;
import java.util.Map;

/**
 * Module class of this module.
 *
 * @author frank.sommer
 * @since 26.01.2012
 */
public class VanityUrlModule {
    public static final String WORKSPACE = "vanityUrls";
    public static final String NT_VANITY = "mgnl:vanityUrl";

    private Map<String, String> _excludes;
    private PreviewImageConfig _previewImage;
    private PublicUrlService _publicUrlService;

    private String _headlessEndpoint;

    public Map<String, String> getExcludes() {
        return _excludes == null ? Collections.emptyMap() : _excludes;
    }

    public void setExcludes(Map<String, String> excludes) {
        _excludes = excludes;
    }

    public PublicUrlService getPublicUrlService() {
        if (_publicUrlService == null) {
            _publicUrlService = Components.getComponent(PublicUrlService.class);
        }
        return _publicUrlService;
    }

    public void setPublicUrlService(final PublicUrlService publicUrlService) {
        _publicUrlService = publicUrlService;
    }

    public String getHeadlessEndpoint() {
        return _headlessEndpoint;
    }

    public void setHeadlessEndpoint(String headlessEndpoint) {
        _headlessEndpoint = headlessEndpoint;
    }

    public PreviewImageConfig getPreviewImage() {
        return _previewImage;
    }

    public void setPreviewImage(PreviewImageConfig previewImage) {
        _previewImage = previewImage;
    }
}
