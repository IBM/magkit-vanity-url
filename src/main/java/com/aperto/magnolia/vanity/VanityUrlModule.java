package com.aperto.magnolia.vanity;

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
    private Map<String, String> _excludes;

    public Map<String, String> getExcludes() {
        return _excludes == null ? Collections.<String, String>emptyMap() : _excludes;
    }

    public void setExcludes(Map<String, String> excludes) {
        _excludes = excludes;
    }
}