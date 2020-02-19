/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi;

import java.util.EventListener;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;

@Deprecated
public interface BindingRuntimeContextListener extends EventListener {

    void onBindingRuntimeContextUpdated(BindingRuntimeContext context);
}
