/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.api.MissingSchemaException;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A combinations of {@link BindingCodecTreeFactory} and {@link BindingNormalizedNodeSerializer}, with internal
 * caching of instance identifiers.
 *
 * <p>
 * NOTE: this class is non-final to allow controller adapter migration without duplicated code.
 */
@Singleton
public class BindingToNormalizedNodeCodec implements BindingNormalizedNodeSerializer, EffectiveModelContextListener,
        AutoCloseable {

    private static final long WAIT_DURATION_SEC = 5;
    private static final Logger LOG = LoggerFactory.getLogger(BindingToNormalizedNodeCodec.class);

    private final LoadingCache<InstanceIdentifier<?>, YangInstanceIdentifier> iiCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<InstanceIdentifier<?>, YangInstanceIdentifier>() {
                @Override
                public YangInstanceIdentifier load(final InstanceIdentifier<?> key) {
                    return toYangInstanceIdentifierBlocking(key);
                }
            });
    private final BindingNormalizedNodeCodecRegistry codecRegistry;
    private final ClassLoadingStrategy classLoadingStrategy;
    private final BindingRuntimeGenerator generator;
    private final FutureSchema futureSchema;

    private ListenerRegistration<?> listenerRegistration;

    @Inject
    public BindingToNormalizedNodeCodec(final BindingRuntimeGenerator generator,
            final ClassLoadingStrategy classLoadingStrategy, final BindingNormalizedNodeCodecRegistry codecRegistry) {
        this(generator, classLoadingStrategy, codecRegistry, false);
    }

    @Beta
    public BindingToNormalizedNodeCodec(final BindingRuntimeContext runtimeContext) {
        generator = (final SchemaContext context) -> {
            throw new UnsupportedOperationException("Static context assigned");
        };
        classLoadingStrategy = runtimeContext.getStrategy();
        codecRegistry = new BindingNormalizedNodeCodecRegistry(runtimeContext);
        // TODO: this should have a specialized constructor or not be needed
        futureSchema = FutureSchema.create(0, TimeUnit.SECONDS, false);
        futureSchema.onRuntimeContextUpdated(runtimeContext);
    }

    public BindingToNormalizedNodeCodec(final BindingRuntimeGenerator generator,
            final ClassLoadingStrategy classLoadingStrategy, final BindingNormalizedNodeCodecRegistry codecRegistry,
            final boolean waitForSchema) {
        this.generator = requireNonNull(generator, "generator");
        this.classLoadingStrategy = requireNonNull(classLoadingStrategy, "classLoadingStrategy");
        this.codecRegistry = requireNonNull(codecRegistry, "codecRegistry");
        this.futureSchema = FutureSchema.create(WAIT_DURATION_SEC, TimeUnit.SECONDS, waitForSchema);
    }

    public static BindingToNormalizedNodeCodec newInstance(final BindingRuntimeGenerator generator,
            final ClassLoadingStrategy classLoadingStrategy, final DOMSchemaService schemaService) {
        final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry();
        BindingToNormalizedNodeCodec instance = new BindingToNormalizedNodeCodec(generator, classLoadingStrategy,
            codecRegistry, true);
        instance.listenerRegistration = schemaService.registerSchemaContextListener(instance);
        return instance;
    }

    protected YangInstanceIdentifier toYangInstanceIdentifierBlocking(
            final InstanceIdentifier<? extends DataObject> binding) {
        try {
            return codecRegistry.toYangInstanceIdentifier(binding);
        } catch (final MissingSchemaException e) {
            waitForSchema(decompose(binding), e);
            return codecRegistry.toYangInstanceIdentifier(binding);
        }
    }

    @Override
    public final YangInstanceIdentifier toYangInstanceIdentifier(final InstanceIdentifier<?> binding) {
        return codecRegistry.toYangInstanceIdentifier(binding);
    }

    protected YangInstanceIdentifier toYangInstanceIdentifierCached(final InstanceIdentifier<?> binding) {
        return iiCache.getUnchecked(binding);
    }

    @Override
    public final <T extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> toNormalizedNode(
            final InstanceIdentifier<T> path, final T data) {
        try {
            return codecRegistry.toNormalizedNode(path, data);
        } catch (final MissingSchemaException e) {
            waitForSchema(decompose(path), e);
            return codecRegistry.toNormalizedNode(path, data);
        }
    }

    @Override
    public final Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        return codecRegistry.fromNormalizedNode(path, data);
    }

    @Override
    public final Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data) {
        return codecRegistry.fromNormalizedNodeNotification(path, data);
    }

    @Override
    public final Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data,
            final Instant eventInstant) {
        return codecRegistry.fromNormalizedNodeNotification(path, data, eventInstant);
    }

    @Override
    public final DataObject fromNormalizedNodeRpcData(final SchemaPath path, final ContainerNode data) {
        return codecRegistry.fromNormalizedNodeRpcData(path, data);
    }

    @Override
    public <T extends RpcInput> T fromNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode input) {
        return codecRegistry.fromNormalizedNodeActionInput(action, input);
    }

    @Override
    public <T extends RpcOutput> T fromNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode output) {
        return codecRegistry.fromNormalizedNodeActionOutput(action, output);
    }

    @Override
    public final <T extends DataObject> InstanceIdentifier<T> fromYangInstanceIdentifier(
            final YangInstanceIdentifier dom) {
        return codecRegistry.fromYangInstanceIdentifier(dom);
    }

    @Override
    public final ContainerNode toNormalizedNodeNotification(final Notification data) {
        return codecRegistry.toNormalizedNodeNotification(data);
    }

    @Override
    public final ContainerNode toNormalizedNodeRpcData(final DataContainer data) {
        return codecRegistry.toNormalizedNodeRpcData(data);
    }

    @Override
    public ContainerNode toNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final RpcInput input) {
        return codecRegistry.toNormalizedNodeActionInput(action, input);
    }

    @Override
    public ContainerNode toNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final RpcOutput output) {
        return codecRegistry.toNormalizedNodeActionOutput(action, output);
    }

    @Override
    public BindingLazyContainerNode<RpcInput> toLazyNormalizedNodeActionInput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcInput input) {
        return codecRegistry.toLazyNormalizedNodeActionInput(action, identifier, input);
    }

    @Override
    public BindingLazyContainerNode<RpcOutput> toLazyNormalizedNodeActionOutput(
            final Class<? extends Action<?, ?, ?>> action, final NodeIdentifier identifier, final RpcOutput output) {
        return codecRegistry.toLazyNormalizedNodeActionOutput(action, identifier, output);
    }

    /**
     * Returns a Binding-Aware instance identifier from normalized
     * instance-identifier if it is possible to create representation.
     *
     * <p>
     * Returns Optional.empty for cases where target is mixin node except
     * augmentation.
     */
    public final Optional<InstanceIdentifier<? extends DataObject>> toBinding(final YangInstanceIdentifier normalized)
                    throws DeserializationException {
        try {
            return Optional.ofNullable(codecRegistry.fromYangInstanceIdentifier(normalized));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void onModelContextUpdated(final EffectiveModelContext newModelContext) {
        final BindingRuntimeContext runtimeContext = DefaultBindingRuntimeContext.create(
            generator.generateTypeMapping(newModelContext), classLoadingStrategy);
        codecRegistry.onBindingRuntimeContextUpdated(runtimeContext);
        futureSchema.onRuntimeContextUpdated(runtimeContext);
    }

    public final BindingNormalizedNodeCodecRegistry getCodecRegistry() {
        return codecRegistry;
    }

    @Override
    @PreDestroy
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

    // FIXME: This should be probably part of Binding Runtime context
    public final ImmutableBiMap<Method, SchemaPath> getRpcMethodToSchemaPath(final Class<? extends RpcService> key) {
        final Module module = getModuleBlocking(key);
        final ImmutableBiMap.Builder<Method, SchemaPath> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                final Method method = findRpcMethod(key, rpcDef);
                ret.put(method, rpcDef.getPath());
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Rpc defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    protected ImmutableBiMap<Method, RpcDefinition> getRpcMethodToSchema(final Class<? extends RpcService> key) {
        final Module module = getModuleBlocking(key);
        final ImmutableBiMap.Builder<Method, RpcDefinition> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                final Method method = findRpcMethod(key, rpcDef);
                ret.put(method, rpcDef);
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Rpc defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    private Module getModuleBlocking(final Class<?> modeledClass) {
        final QNameModule moduleName = BindingReflections.getQNameModule(modeledClass);
        BindingRuntimeContext localRuntimeContext = runtimeContext();
        Module module = localRuntimeContext == null ? null :
            localRuntimeContext.getSchemaContext().findModule(moduleName).orElse(null);
        if (module == null && futureSchema.waitForSchema(moduleName)) {
            localRuntimeContext = runtimeContext();
            checkState(localRuntimeContext != null, "BindingRuntimeContext is not available.");
            module = localRuntimeContext.getSchemaContext().findModule(moduleName).orElse(null);
        }
        if (module != null) {
            return module;
        }

        LOG.debug("Schema for {} is not available; expected module name: {}; BindingRuntimeContext: {}",
                modeledClass, moduleName, localRuntimeContext);
        throw new IllegalStateException(String.format("Schema for %s is not available; expected module name: %s; "
                + "full BindingRuntimeContext available in debug log", modeledClass, moduleName));
    }

    private void waitForSchema(final Collection<Class<?>> binding, final MissingSchemaException exception) {
        LOG.warn("Blocking thread to wait for schema convergence updates for {} {}", futureSchema.getDuration(),
            futureSchema.getUnit());
        if (!futureSchema.waitForSchema(binding)) {
            throw exception;
        }
    }

    private Method findRpcMethod(final Class<? extends RpcService> key, final RpcDefinition rpcDef)
            throws NoSuchMethodException {
        final String methodName = BindingMapping.getRpcMethodName(rpcDef.getQName());
        final Class<?> inputClz = runtimeContext().getClassForSchema(rpcDef.getInput());
        return key.getMethod(methodName, inputClz);
    }

    protected @NonNull Entry<InstanceIdentifier<?>, BindingDataObjectCodecTreeNode<?>> getSubtreeCodec(
            final YangInstanceIdentifier domIdentifier) {

        final BindingCodecTree currentCodecTree = codecRegistry.getCodecContext();
        final InstanceIdentifier<?> bindingPath = codecRegistry.fromYangInstanceIdentifier(domIdentifier);
        checkArgument(bindingPath != null);
        /**
         * If we are able to deserialize YANG instance identifier, getSubtreeCodec must
         * return non-null value.
         */
        final BindingDataObjectCodecTreeNode<?> codecContext = currentCodecTree.getSubtreeCodec(bindingPath);
        return new SimpleEntry<>(bindingPath, codecContext);
    }

    final SchemaPath getActionPath(final Class<? extends Action<?, ?, ?>> type) {
        final ActionDefinition schema = runtimeContext().getActionDefinition(type);
        checkArgument(schema != null, "Failed to find schema for %s", type);
        return schema.getPath();
    }

    final BindingRuntimeContext runtimeContext() {
        return futureSchema.runtimeContext();
    }

    private static Collection<Class<?>> decompose(final InstanceIdentifier<?> path) {
        return ImmutableSet.copyOf(Iterators.transform(path.getPathArguments().iterator(), PathArgument::getType));
    }

    protected Collection<DOMDataTreeIdentifier> toDOMDataTreeIdentifiers(
            final Collection<DataTreeIdentifier<?>> subtrees) {
        return subtrees.stream().map(this::toDOMDataTreeIdentifier).collect(Collectors.toSet());
    }

    protected DOMDataTreeIdentifier toDOMDataTreeIdentifier(final DataTreeIdentifier<?> path) {
        final YangInstanceIdentifier domPath = toYangInstanceIdentifierBlocking(path.getRootIdentifier());
        return new DOMDataTreeIdentifier(path.getDatastoreType(), domPath);
    }
}
