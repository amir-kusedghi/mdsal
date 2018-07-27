/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.ValueContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Codec for serialize/deserialize list.
 */
@Beta
public final class IdentifiableItemCodec implements Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> {

    private final Map<QName, ValueContext> keyValueContexts;
    private final List<QName> keysInBindingOrder;
    private final ListSchemaNode schema;
    private final Class<?> identifiable;
    private final MethodHandle ctorInvoker;
    private final MethodHandle ctor;

    /**
     * Prepare constructor of binding object of list with resolving key values if exists.
     *
     * @param schema
     *            - list schema node
     * @param keyClass
     *            - binding class of key
     * @param identifiable
     *            - binding class of list
     * @param keyValueContexts
     *            - key value contexts
     */
    public IdentifiableItemCodec(final ListSchemaNode schema, final Class<? extends Identifier> keyClass,
            final Class<?> identifiable, final Map<QName, ValueContext> keyValueContexts) {
        this.schema = schema;
        this.identifiable = identifiable;

        try {
            ctor = MethodHandles.publicLookup().unreflectConstructor(getConstructor(keyClass));
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException("Missing construct in class " + keyClass, e);
        }
        final MethodHandle inv = MethodHandles.spreadInvoker(ctor.type(), 0);
        this.ctorInvoker = inv.asType(inv.type().changeReturnType(Identifier.class));

        /*
         * We need to re-index to make sure we instantiate nodes in the order in which they are defined.
         */
        final Map<QName, ValueContext> keys = new LinkedHashMap<>();
        for (final QName qname : schema.getKeyDefinition()) {
            keys.put(qname, keyValueContexts.get(qname));
        }
        this.keyValueContexts = ImmutableMap.copyOf(keys);

        /*
         * When instantiating binding objects we need to specify constructor arguments in alphabetic order. We
         * play a couple of tricks here to optimize CPU/memory trade-offs.
         *
         * We do not have to perform a sort if the source collection has less than two elements.
         *
         * We always perform an ImmutableList.copyOf(), as that will turn into a no-op if the source is
         * already immutable. It will also produce optimized implementations for empty and singleton
         * collections.
         *
         * BUG-2755: remove this if order is made declaration-order-dependent
         */
        final List<QName> unsortedKeys = schema.getKeyDefinition();
        final List<QName> sortedKeys;
        if (unsortedKeys.size() > 1) {
            final List<QName> tmp = new ArrayList<>(unsortedKeys);
            tmp.sort((q1, q2) -> q1.getLocalName().compareToIgnoreCase(q2.getLocalName()));
            sortedKeys = tmp;
        } else {
            sortedKeys = unsortedKeys;
        }

        this.keysInBindingOrder = ImmutableList.copyOf(sortedKeys);
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public IdentifiableItem<?, ?> deserialize(final NodeIdentifierWithPredicates input) {
        final Object[] bindingValues = new Object[keysInBindingOrder.size()];
        int offset = 0;

        for (final QName key : keysInBindingOrder) {
            final Object yangValue = input.getKeyValues().get(key);
            bindingValues[offset++] = keyValueContexts.get(key).deserialize(yangValue);
        }

        final Identifier identifier;
        try {
            identifier = (Identifier) ctorInvoker.invokeExact(ctor, bindingValues);
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        final IdentifiableItem identifiableItem = new IdentifiableItem(identifiable, identifier);
        return identifiableItem;
    }

    @Override
    public NodeIdentifierWithPredicates serialize(final IdentifiableItem<?, ?> input) {
        final Object value = input.getKey();

        final Map<QName, Object> values = new LinkedHashMap<>();
        for (final Entry<QName, ValueContext> valueCtx : keyValueContexts.entrySet()) {
            values.put(valueCtx.getKey(), valueCtx.getValue().getAndSerialize(value));
        }
        return new NodeIdentifierWithPredicates(schema.getQName(), values);
    }

    @SuppressWarnings("unchecked")
    private static Constructor<? extends Identifier> getConstructor(final Class<? extends Identifier> clazz) {
        for (@SuppressWarnings("rawtypes") final Constructor constr : clazz.getConstructors()) {
            final Class<?>[] parameters = constr.getParameterTypes();
            if (!clazz.equals(parameters[0])) {
                // It is not copy constructor;
                return constr;
            }
        }
        throw new IllegalArgumentException("Supplied class " + clazz + "does not have required constructor.");
    }
}
