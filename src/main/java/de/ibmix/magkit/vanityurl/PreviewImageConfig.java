package de.ibmix.magkit.vanityurl;

/*-
 * #%L
 * magkit-vanity-url Magnolia Module
 * %%
 * Copyright (C) 2013 - 2024 IBM iX
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

import java.util.Arrays;

/**
 * Configuration class for preview image.
 *
 * @author IBM iX
 */
public class PreviewImageConfig {
    private ImageType _type = ImageType.SVG;

    public ImageType getType() {
        return _type;
    }

    public void setType(ImageType type) {
        _type = type;
    }

    /**
     * Type for the QR code image generation.
     *
     * @author IBM iX
     */
    public enum ImageType {
        PNG(".png", "image/png"),
        SVG(".svg", "image/svg+xml");

        private final String _extension;
        private final String _mimeType;

        ImageType(String extension, String mimeType) {
            _extension = extension;
            _mimeType = mimeType;
        }

        public String getExtension() {
            return _extension;
        }

        public String getMimeType() {
            return _mimeType;
        }

        public static String getExtensionByMimeType(String mimeType) {
            return Arrays.stream(ImageType.values()).filter(it -> it.getMimeType().equals(mimeType)).findFirst().map(ImageType::getExtension).orElse(SVG.getExtension());
        }
    }
}
