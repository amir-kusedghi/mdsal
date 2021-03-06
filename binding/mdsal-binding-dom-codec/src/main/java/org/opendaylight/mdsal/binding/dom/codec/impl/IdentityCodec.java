/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingIdentityCodec;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.AbstractIllegalArgumentCodec;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.common.QName;

final class IdentityCodec extends AbstractIllegalArgumentCodec<QName, Class<?>> implements BindingIdentityCodec {
    private final BindingRuntimeContext context;

    IdentityCodec(final BindingRuntimeContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    protected Class<?> deserializeImpl(final QName input) {
        return context.getIdentityClass(input);
    }

    @Override
    protected QName serializeImpl(final Class<?> input) {
        checkArgument(BaseIdentity.class.isAssignableFrom(input), "%s is not an identity", input);
        return BindingReflections.findQName(input);
    }

    @Override
    public Class<? extends BaseIdentity> toBinding(final QName qname) {
        final Class<?> identity = context.getIdentityClass(requireNonNull(qname));
        checkArgument(BaseIdentity.class.isAssignableFrom(identity), "%s resolves to non-identity %s", qname, identity);
        return identity.asSubclass(BaseIdentity.class);
    }

    @Override
    public QName fromBinding(final Class<? extends BaseIdentity> bindingClass) {
        return BindingReflections.getQName(bindingClass);
    }
}
