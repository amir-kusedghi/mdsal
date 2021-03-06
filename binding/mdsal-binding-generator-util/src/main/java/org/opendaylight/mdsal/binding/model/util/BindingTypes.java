/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import static org.opendaylight.mdsal.binding.model.util.Types.parameterizedTypeFor;
import static org.opendaylight.mdsal.binding.model.util.Types.typeForClass;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceNotification;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedListAction;
import org.opendaylight.yangtools.yang.binding.KeyedListNotification;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.ScalarTypeObject;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class BindingTypes {

    public static final ConcreteType BASE_IDENTITY = typeForClass(BaseIdentity.class);
    public static final ConcreteType DATA_CONTAINER = typeForClass(DataContainer.class);
    public static final ConcreteType DATA_OBJECT = typeForClass(DataObject.class);
    public static final ConcreteType TYPE_OBJECT = typeForClass(TypeObject.class);
    public static final ConcreteType DATA_ROOT = typeForClass(DataRoot.class);
    public static final ConcreteType NOTIFICATION = typeForClass(Notification.class);
    public static final ConcreteType NOTIFICATION_LISTENER = typeForClass(NotificationListener.class);
    public static final ConcreteType QNAME = typeForClass(QName.class);
    public static final ConcreteType RPC_INPUT = typeForClass(RpcInput.class);
    public static final ConcreteType RPC_OUTPUT = typeForClass(RpcOutput.class);
    public static final ConcreteType RPC_SERVICE = typeForClass(RpcService.class);

    // This is an annotation, we are current just referencing the type
    public static final JavaTypeName ROUTING_CONTEXT = JavaTypeName.create(RoutingContext.class);

    @VisibleForTesting
    static final ConcreteType AUGMENTABLE = typeForClass(Augmentable.class);
    @VisibleForTesting
    static final ConcreteType AUGMENTATION = typeForClass(Augmentation.class);
    @VisibleForTesting
    static final ConcreteType IDENTIFIABLE = typeForClass(Identifiable.class);
    @VisibleForTesting
    static final ConcreteType IDENTIFIER = typeForClass(Identifier.class);
    @VisibleForTesting
    static final ConcreteType INSTANCE_IDENTIFIER = typeForClass(InstanceIdentifier.class);

    private static final ConcreteType ACTION = typeForClass(Action.class);
    private static final ConcreteType CHILD_OF = typeForClass(ChildOf.class);
    private static final ConcreteType CHOICE_IN = typeForClass(ChoiceIn.class);
    private static final ConcreteType INSTANCE_NOTIFICATION = typeForClass(InstanceNotification.class);
    private static final ConcreteType KEYED_INSTANCE_IDENTIFIER = typeForClass(KeyedInstanceIdentifier.class);
    private static final ConcreteType KEYED_LIST_ACTION = typeForClass(KeyedListAction.class);
    private static final ConcreteType KEYED_LIST_NOTIFICATION = typeForClass(KeyedListNotification.class);
    private static final ConcreteType OPAQUE_OBJECT = typeForClass(OpaqueObject.class);
    private static final ConcreteType RPC_RESULT = typeForClass(RpcResult.class);
    private static final ConcreteType SCALAR_TYPE_OBJECT = typeForClass(ScalarTypeObject.class);

    private BindingTypes() {

    }

    /**
     * Type specializing {@link Action} for a particular type.
     *
     * @param parent Type of parent defining the action
     * @param input Type input type
     * @param output Type output type
     * @return A parameterized type corresponding to {@code Action<Parent, Input, Output>}
     * @throws NullPointerException if any argument is is null
     */
    public static ParameterizedType action(final Type parent, final Type input, final Type output) {
        return parameterizedTypeFor(ACTION, instanceIdentifier(parent), input, output);
    }

    /**
     * Type specializing {@link KeyedListAction} for a particular type.
     *
     * @param parent Type of parent defining the action
     * @param keyType Type of parent's key
     * @param input Type input type
     * @param output Type output type
     * @return A parameterized type corresponding to {@code KeyedListAction<ParentKey, Parent, Input, Output>}
     * @throws NullPointerException if any argument is is null
     */
    public static ParameterizedType keyedListAction(final Type parent, final Type keyType, final Type input,
            final Type output) {
        return parameterizedTypeFor(KEYED_LIST_ACTION, keyType, parent, input, output);
    }

    /**
     * Type specializing {@link InstanceNotification} for a particular type.
     *
     * @param parent Type of parent defining the notification
     * @return A parameterized type corresponding to {@code InstanceNotification<Parent>}
     * @throws NullPointerException if {@code parent} is is null
     */
    public static ParameterizedType instanceNotification(final Type concreteType, final Type parent) {
        return parameterizedTypeFor(INSTANCE_NOTIFICATION, concreteType, parent);
    }

    /**
     * Type specializing {@link InstanceNotification} for a particular type.
     *
     * @param parent Type of parent defining the notification
     * @param keyType Type of parent's key
     * @return A parameterized type corresponding to {@code KeyedInstanceNotification<ParentKey, Parent>}
     * @throws NullPointerException if any argument is is null
     */
    public static ParameterizedType keyedListNotification(final Type concreteType, final Type parent,
            final Type keyType) {
        return parameterizedTypeFor(KEYED_LIST_NOTIFICATION, concreteType, parent, keyType);
    }

    /**
     * Specialize {@link Augmentable} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Augmentable<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static @NonNull ParameterizedType augmentable(final Type type) {
        return parameterizedTypeFor(AUGMENTABLE, type);
    }

    /**
     * Specialize {@link Augmentation} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Augmentation<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static @NonNull ParameterizedType augmentation(final Type type) {
        return parameterizedTypeFor(AUGMENTATION, type);
    }

    /**
     * Specialize {@link ChildOf} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ChildOf<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType childOf(final Type type) {
        return parameterizedTypeFor(CHILD_OF, type);
    }

    /**
     * Type specializing {@link ChoiceIn} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ChoiceIn<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType choiceIn(final Type type) {
        return parameterizedTypeFor(CHOICE_IN, type);
    }

    /**
     * Type specializing {@link Identifier} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Identifier<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType identifier(final Type type) {
        return parameterizedTypeFor(IDENTIFIER, type);
    }

    /**
     * Type specializing {@link Identifiable} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Identifiable<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType identifiable(final Type type) {
        return parameterizedTypeFor(IDENTIFIABLE, type);
    }

    /**
     * Type specializing {@link InstanceIdentifier} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code InstanceIdentifier<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType instanceIdentifier(final Type type) {
        return parameterizedTypeFor(INSTANCE_IDENTIFIER, type);
    }

    /**
     * Type specializing {@link KeyedInstanceIdentifier} for a particular type.
     *
     * @param type Type for which to specialize
     * @param keyType Type of key
     * @return A parameterized type corresponding to {@code KeyedInstanceIdentifier<Type, KeyType>}
     * @throws NullPointerException if any argument is is null
     */
    public static ParameterizedType keyedInstanceIdentifier(final Type type, final Type keyType) {
        return parameterizedTypeFor(KEYED_INSTANCE_IDENTIFIER, type, keyType);
    }

    /**
     * Type specializing {@link OpaqueObject} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code OpaqueObject<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType opaqueObject(final Type type) {
        return parameterizedTypeFor(OPAQUE_OBJECT, type);
    }

    /**
     * Type specializing {@link RpcResult} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code RpcResult<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType rpcResult(final Type type) {
        return parameterizedTypeFor(RPC_RESULT, type);
    }

    /**
     * Type specializing {@link ScalarTypeObject} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ScalarTypeObject<Type>}
     * @throws NullPointerException if {@code type} is null
     */
    public static ParameterizedType scalarTypeObject(final Type type) {
        return parameterizedTypeFor(SCALAR_TYPE_OBJECT, type);
    }
}
