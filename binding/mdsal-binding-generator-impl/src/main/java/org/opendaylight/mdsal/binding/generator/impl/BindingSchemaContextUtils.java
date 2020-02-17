/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BindingSchemaContextUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BindingSchemaContextUtils.class);

    private BindingSchemaContextUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    // FIXME: This method does not search in case augmentations.
    public static Optional<DataNodeContainer> findDataNodeContainer(final SchemaContext ctx,
            final InstanceIdentifier<?> path) {
        Iterator<PathArgument> pathArguments = path.getPathArguments().iterator();
        PathArgument currentArg = pathArguments.next();
        Preconditions.checkArgument(currentArg != null);
        QName currentQName = BindingReflections.findQName(currentArg.getType());

        Optional<DataNodeContainer> currentContainer = Optional.empty();
        if (BindingReflections.isNotification(currentArg.getType())) {
            currentContainer = findNotification(ctx, currentQName);
        } else if (BindingReflections.isRpcType(currentArg.getType())) {
            currentContainer = findFirstDataNodeContainerInRpc(ctx, currentArg.getType());
            if (currentQName == null && currentContainer.isPresent()) {
                currentQName = ((DataSchemaNode) currentContainer.get()).getQName();
            }
        } else {
            currentContainer = findDataNodeContainer(ctx, currentQName);
        }

        while (currentContainer.isPresent() && pathArguments.hasNext()) {
            currentArg = pathArguments.next();
            if (Augmentation.class.isAssignableFrom(currentArg.getType())) {
                currentQName = BindingReflections.findQName(currentArg.getType());
                if (pathArguments.hasNext()) {
                    currentArg = pathArguments.next();
                } else {
                    return currentContainer;
                }
            }
            if (ChildOf.class.isAssignableFrom(currentArg.getType())
                    && BindingReflections.isAugmentationChild(currentArg.getType())) {
                currentQName = BindingReflections.findQName(currentArg.getType());
            } else {
                currentQName = BindingReflections.findQName(currentArg.getType()).withModule(currentQName.getModule());
            }
            Optional<DataNodeContainer> potential = findDataNodeContainer(currentContainer.get(), currentQName);
            if (potential.isPresent()) {
                currentContainer = potential;
            } else {
                return Optional.empty();
            }
        }
        return currentContainer;
    }

    private static Optional<DataNodeContainer> findDataNodeContainer(final DataNodeContainer ctx,
            final QName targetQName) {

        for (DataSchemaNode child : ctx.getChildNodes()) {
            if (child instanceof ChoiceSchemaNode) {
                DataNodeContainer potential = findInCases((ChoiceSchemaNode) child, targetQName);
                if (potential != null) {
                    return Optional.of(potential);
                }
            } else if (child instanceof DataNodeContainer) {
                final QName qname = child.getQName();
                if (qname.equals(targetQName)
                        || child.isAddedByUses() && qname.getLocalName().equals(targetQName.getLocalName())) {
                    return Optional.of((DataNodeContainer) child);
                }
            }

        }
        return Optional.empty();
    }

    private static Optional<DataNodeContainer> findNotification(final SchemaContext ctx,
            final QName notificationQName) {
        for (NotificationDefinition notification : ctx.getNotifications()) {
            if (notification.getQName().equals(notificationQName)) {
                return Optional.of(notification);
            }
        }
        return Optional.empty();
    }

    private static DataNodeContainer findInCases(final ChoiceSchemaNode choiceNode, final QName targetQName) {
        for (CaseSchemaNode caze : choiceNode.getCases().values()) {
            Optional<DataNodeContainer> potential = findDataNodeContainer(caze, targetQName);
            if (potential.isPresent()) {
                return potential.get();
            }
        }
        return null;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static Optional<DataNodeContainer> findFirstDataNodeContainerInRpc(final SchemaContext ctx,
            final Class<? extends DataObject> targetType) {
        final QNameModule targetModule;
        try {
            targetModule = BindingReflections.getModuleInfo(targetType).getName().getModule();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to load module information for class %s", targetType), e);
        }

        for (RpcDefinition rpc : ctx.getOperations()) {
            if (targetModule.equals(rpc.getQName().getModule())) {
                final Optional<DataNodeContainer> potential = findInputOutput(rpc,targetType.getSimpleName());
                if (potential.isPresent()) {
                    return potential;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<DataNodeContainer> findInputOutput(final RpcDefinition rpc, final String targetType) {
        final String rpcName = BindingMapping.getClassName(rpc.getQName());
        final String rpcInputName = rpcName + BindingMapping.RPC_INPUT_SUFFIX;
        if (targetType.equals(rpcInputName)) {
            return Optional.of(rpc.getInput());
        }
        final String rpcOutputName = rpcName + BindingMapping.RPC_OUTPUT_SUFFIX;
        if (targetType.equals(rpcOutputName)) {
            return Optional.of(rpc.getOutput());
        }
        return Optional.empty();
    }

    public static Set<AugmentationSchemaNode> collectAllAugmentationDefinitions(final SchemaContext currentSchema,
            final AugmentationTarget ctxNode) {
        HashSet<AugmentationSchemaNode> augmentations = new HashSet<>();
        augmentations.addAll(ctxNode.getAvailableAugmentations());
        if (ctxNode instanceof DataSchemaNode && ((DataSchemaNode) ctxNode).isAddedByUses()) {
            LOG.info("DataSchemaNode target added by uses {}", ctxNode);
        }

        return augmentations;
    }

    public static Optional<ChoiceSchemaNode> findInstantiatedChoice(final DataNodeContainer parent,
            final Class<?> choiceClass) {
        return findInstantiatedChoice(parent, BindingReflections.findQName(choiceClass));
    }

    public static Optional<ChoiceSchemaNode> findInstantiatedChoice(final DataNodeContainer ctxNode,
            final QName choiceName) {
        DataSchemaNode potential = ctxNode.getDataChildByName(choiceName);
        if (potential instanceof ChoiceSchemaNode) {
            return Optional.of((ChoiceSchemaNode) potential);
        }

        return Optional.empty();
    }
}
