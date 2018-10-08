/*
 * GRAKN.AI - THE KNOWLEDGE GRAPH
 * Copyright (C) 2018 Grakn Labs Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ai.grakn.util;

import com.google.common.base.Preconditions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This Util class just takes care of going from host and port to string and vice-versa
 * The URI class would require a schema
 *
 * @author pluraliseseverythings
 */
public class SimpleURI {
    private final static String DEFAULT_PROTOCOL_NAME = "http";
    private final int port;
    private final String host;

    public SimpleURI(String uri) {
        String[] uriSplit = uri.split(":");
        Preconditions.checkArgument(
                uriSplit.length == 2 || (uriSplit.length == 3 && uriSplit[1].contains("//")),
                "Malformed URI " + uri);
        // if it has the schema, we start parsing from after
        int bias = uriSplit.length == 3 ? 1 : 0;
        this.host = uriSplit[bias].replace("/", "").trim();
        this.port = Integer.parseInt(uriSplit[1 + bias].trim());
    }

    public SimpleURI(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", host, port);
    }

    public URI toURI() {
        try {
            return new URL(DEFAULT_PROTOCOL_NAME, host, Integer.toString(port)).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
