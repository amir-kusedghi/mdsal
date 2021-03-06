/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;

public abstract class AbstractTypesTest {

    private final URL testSourcesDirUrl;
    protected Set<File> testModels;

    AbstractTypesTest(final URL testSourcesDirUrl) {
        this.testSourcesDirUrl = testSourcesDirUrl;
    }

    @Before
    public void loadTestResources() throws URISyntaxException {
        File testSourcesDir = new File(testSourcesDirUrl.toURI());
        File[] testFiles = requireNonNull(testSourcesDir.listFiles(), testSourcesDir + " does not denote a directory");
        testModels = new HashSet<>();
        for (File file : testFiles) {
            if (file.isFile()) {
                testModels.add(file);
            }
        }
    }

}
