package com.aperto.magnolia.vanity.app;

/*
 * #%L
 * magnolia-vanity-url Magnolia Module
 * %%
 * Copyright (C) 2013 - 2014 Aperto AG
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;

import java.util.Locale;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.startsWithIgnoreCase;

/**
 * Handles input of links by urls (external) or absolute paths (internal).
 *
 * @author philipp.guettler
 * @since 06.03.14
 */
public class LinkConverter extends BaseIdentifierToPathConverter {

    @Override
    public String convertToModel(final String path, final Class<? extends String> targetType, final Locale locale) {
        String result = path;
        if (!isExternalLink(path) && isNotBlank(path)) {
            result = super.convertToModel(path, targetType, locale);
        }
        return result;
    }

    @Override
    public String convertToPresentation(final String uuid, final Class<? extends String> targetType, final Locale locale) {
        String result = uuid;
        if (!isExternalLink(uuid) && isNotBlank(uuid)) {
            result = super.convertToPresentation(uuid, targetType, locale);
        }
        return result;
    }

    /**
     * Checks, if the link starts with a web protocol.
     *
     * @param linkValue to check
     * @return true for external link
     */
    public static boolean isExternalLink(final String linkValue) {
        return startsWithIgnoreCase(linkValue, "https://") || startsWithIgnoreCase(linkValue, "http://");
    }
}
