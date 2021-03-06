/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class GeneratedTypesTest {

    @Test
    public void testMultipleModulesResolving() {
        final SchemaContext context = YangParserTestUtils.parseYangResources(GeneratedTypesTest.class,
            "/abstract-topology.yang", "/ietf/ietf-inet-types.yang");
        assertNotNull(context);

        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        assertNotNull(genTypes);
        assertEquals(29, genTypes.size());
    }

    @Test
    public void testContainerResolving() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/simple-container-demo.yang");
        assertNotNull(context);

        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        assertNotNull(genTypes);
        assertEquals(3, genTypes.size());

        GeneratedType simpleContainer = (GeneratedType) genTypes.get(1);
        GeneratedType nestedContainer = (GeneratedType) genTypes.get(2);
        for (Type t : genTypes) {
            if ("SimpleContainer".equals(t.getName())) {
                simpleContainer = (GeneratedType) t;
            } else if ("NestedContainer".equals(t.getName())) {
                nestedContainer = (GeneratedType) t;
            }
        }
        assertNotNull(simpleContainer);
        assertNotNull(nestedContainer);
        assertEquals(4, simpleContainer.getMethodDefinitions().size());
        assertEquals(3, nestedContainer.getMethodDefinitions().size());

        int getFooMethodCounter = 0;
        int getBarMethodCounter = 0;
        int getNestedContainerCounter = 0;

        String getFooMethodReturnTypeName = "";
        String getBarMethodReturnTypeName = "";
        String getNestedContainerReturnTypeName = "";
        for (final MethodSignature method : simpleContainer.getMethodDefinitions()) {
            if (method.getName().equals("getFoo")) {
                getFooMethodCounter++;
                getFooMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getBar")) {
                getBarMethodCounter++;
                getBarMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getNestedContainer")) {
                getNestedContainerCounter++;
                getNestedContainerReturnTypeName = method.getReturnType().getName();
            }
        }

        assertEquals(1, getFooMethodCounter);
        assertEquals("Integer", getFooMethodReturnTypeName);

        assertEquals(1, getBarMethodCounter);
        assertEquals("String", getBarMethodReturnTypeName);

        assertEquals(1, getNestedContainerCounter);
        assertEquals("NestedContainer", getNestedContainerReturnTypeName);

        getFooMethodCounter = 0;
        getBarMethodCounter = 0;

        getFooMethodReturnTypeName = "";
        getBarMethodReturnTypeName = "";

        for (final MethodSignature method : nestedContainer.getMethodDefinitions()) {

            if (method.getName().equals("getFoo")) {
                getFooMethodCounter++;
                getFooMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getBar")) {
                getBarMethodCounter++;
                getBarMethodReturnTypeName = method.getReturnType().getName();
            }
        }

        assertEquals(1, getFooMethodCounter);
        assertEquals("Uint8", getFooMethodReturnTypeName);

        assertEquals(1, getBarMethodCounter);
        assertEquals("String", getBarMethodReturnTypeName);
    }

    @Test
    public void testLeafListResolving() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/simple-leaf-list-demo.yang");
        assertNotNull(context);

        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        assertNotNull(genTypes);
        assertEquals(3, genTypes.size());

        GeneratedType simpleContainer = (GeneratedType) genTypes.get(1);
        GeneratedType nestedContainer = (GeneratedType) genTypes.get(2);
        for (Type t : genTypes) {
            if ("SimpleContainer".equals(t.getName())) {
                simpleContainer = (GeneratedType) t;
            } else if ("NestedContainer".equals(t.getName())) {
                nestedContainer = (GeneratedType) t;
            }
        }
        assertNotNull(simpleContainer);
        assertNotNull(nestedContainer);
        assertEquals(4, simpleContainer.getMethodDefinitions().size());
        assertEquals(3, nestedContainer.getMethodDefinitions().size());

        int getFooMethodCounter = 0;
        int getBarMethodCounter = 0;
        int getNestedContainerCounter = 0;

        String getFooMethodReturnTypeName = "";
        String getBarMethodReturnTypeName = "";
        String getNestedContainerReturnTypeName = "";
        for (final MethodSignature method : simpleContainer.getMethodDefinitions()) {
            if (method.isDefault()) {
                continue;
            }
            if (method.getName().equals("getFoo")) {
                getFooMethodCounter++;
                getFooMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getBar")) {
                getBarMethodCounter++;
                getBarMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getNestedContainer")) {
                getNestedContainerCounter++;
                getNestedContainerReturnTypeName = method.getReturnType().getName();
            }
        }

        assertEquals(1, getFooMethodCounter);
        assertEquals("List", getFooMethodReturnTypeName);

        assertEquals(1, getBarMethodCounter);
        assertEquals("String", getBarMethodReturnTypeName);

        assertEquals(1, getNestedContainerCounter);
        assertEquals("NestedContainer", getNestedContainerReturnTypeName);

        getFooMethodCounter = 0;
        getBarMethodCounter = 0;

        getFooMethodReturnTypeName = "";
        getBarMethodReturnTypeName = "";

        for (final MethodSignature method : nestedContainer.getMethodDefinitions()) {
            if (method.getName().equals("getFoo")) {
                getFooMethodCounter++;
                getFooMethodReturnTypeName = method.getReturnType().getName();
            }

            if (method.getName().equals("getBar")) {
                getBarMethodCounter++;
                getBarMethodReturnTypeName = method.getReturnType().getName();
            }
        }

        assertEquals(1, getFooMethodCounter);
        assertEquals("Uint8", getFooMethodReturnTypeName);

        assertEquals(1, getBarMethodCounter);
        assertEquals("List", getBarMethodReturnTypeName);
    }

    @Test
    public void testListResolving() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/simple-list-demo.yang");
        assertNotNull(context);

        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        assertNotNull(genTypes);
        assertEquals(5, genTypes.size());

        int listParentContainerMethodsCount = 0;
        int simpleListMethodsCount = 0;
        int listChildContainerMethodsCount = 0;
        int listKeyClassCount = 0;

        int getSimpleListKeyMethodCount = 0;
        int getListChildContainerMethodCount = 0;
        int getFooMethodCount = 0;
        int setFooMethodCount = 0;
        int getSimpleLeafListMethodCount = 0;
        int setSimpleLeafListMethodCount = 0;
        int getBarMethodCount = 0;

        String getSimpleListKeyMethodReturnTypeName = "";
        String getListChildContainerMethodReturnTypeName = "";

        int listKeyClassPropertyCount = 0;
        String listKeyClassPropertyName = "";
        String listKeyClassPropertyTypeName = "";
        boolean listKeyClassPropertyReadOnly = false;

        int hashMethodParameterCount = 0;
        String hashMethodParameterName = "";
        String hashMethodParameterReturnTypeName = "";

        int equalMethodParameterCount = 0;
        String equalMethodParameterName = "";
        String equalMethodParameterReturnTypeName = "";

        for (final Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                final GeneratedType genType = (GeneratedType) type;
                if (genType.getName().equals("ListParentContainer")) {
                    listParentContainerMethodsCount = genType.getMethodDefinitions().size();
                } else if (genType.getName().equals("SimpleList")) {
                    simpleListMethodsCount = genType.getMethodDefinitions().size();
                    final List<MethodSignature> methods = genType.getMethodDefinitions();
                    for (final MethodSignature method : methods) {
                        switch (method.getName()) {
                            case BindingMapping.IDENTIFIABLE_KEY_NAME:
                                getSimpleListKeyMethodCount++;
                                getSimpleListKeyMethodReturnTypeName = method.getReturnType().getName();
                                break;
                            case "getListChildContainer":
                                getListChildContainerMethodCount++;
                                getListChildContainerMethodReturnTypeName = method.getReturnType().getName();
                                break;
                            case "getFoo":
                                getFooMethodCount++;
                                break;
                            case "setFoo":
                                setFooMethodCount++;
                                break;
                            case "getSimpleLeafList":
                                getSimpleLeafListMethodCount++;
                                break;
                            case "setSimpleLeafList":
                                setSimpleLeafListMethodCount++;
                                break;
                            case "getBar":
                                getBarMethodCount++;
                                break;
                            default:
                        }
                    }
                } else if (genType.getName().equals("ListChildContainer")) {
                    listChildContainerMethodsCount = genType.getMethodDefinitions().size();
                }
            } else if (type instanceof GeneratedTransferObject) {
                final GeneratedTransferObject genTO = (GeneratedTransferObject) type;
                final List<GeneratedProperty> properties = genTO.getProperties();
                final List<GeneratedProperty> hashProps = genTO.getHashCodeIdentifiers();
                final List<GeneratedProperty> equalProps = genTO.getEqualsIdentifiers();

                listKeyClassCount++;
                listKeyClassPropertyCount = properties.size();
                listKeyClassPropertyName = properties.get(0).getName();
                listKeyClassPropertyTypeName = properties.get(0).getReturnType().getName();
                listKeyClassPropertyReadOnly = properties.get(0).isReadOnly();

                hashMethodParameterCount = hashProps.size();
                hashMethodParameterName = hashProps.get(0).getName();
                hashMethodParameterReturnTypeName = hashProps.get(0).getReturnType().getName();

                equalMethodParameterCount = equalProps.size();
                equalMethodParameterName = equalProps.get(0).getName();
                equalMethodParameterReturnTypeName = equalProps.get(0).getReturnType().getName();

            }
        }

        assertEquals(3, listParentContainerMethodsCount);
        assertEquals(2, listChildContainerMethodsCount);
        assertEquals(1, getSimpleListKeyMethodCount);
        assertEquals(1, listKeyClassCount);

        assertEquals(1, listKeyClassPropertyCount);
        assertEquals("listKey", listKeyClassPropertyName);
        assertEquals("Byte", listKeyClassPropertyTypeName);
        assertTrue(listKeyClassPropertyReadOnly);
        assertEquals(1, hashMethodParameterCount);
        assertEquals("listKey", hashMethodParameterName);
        assertEquals("Byte", hashMethodParameterReturnTypeName);
        assertEquals(1, equalMethodParameterCount);
        assertEquals("listKey", equalMethodParameterName);
        assertEquals("Byte", equalMethodParameterReturnTypeName);

        assertEquals("SimpleListKey", getSimpleListKeyMethodReturnTypeName);

        assertEquals(1, getListChildContainerMethodCount);
        assertEquals("ListChildContainer", getListChildContainerMethodReturnTypeName);
        assertEquals(1, getFooMethodCount);
        assertEquals(0, setFooMethodCount);
        assertEquals(1, getSimpleLeafListMethodCount);
        assertEquals(0, setSimpleLeafListMethodCount);
        assertEquals(1, getBarMethodCount);

        assertEquals(7, simpleListMethodsCount);
    }

    @Test
    public void testListCompositeKeyResolving() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/list-composite-key.yang");
        assertNotNull(context);

        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        assertNotNull(genTypes);
        assertEquals(7, genTypes.size());

        int genTypesCount = 0;
        int genTOsCount = 0;

        int compositeKeyListKeyPropertyCount = 0;
        int compositeKeyListKeyCount = 0;
        int innerListKeyPropertyCount = 0;

        for (final Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                genTypesCount++;
            } else if (type instanceof GeneratedTransferObject) {
                final GeneratedTransferObject genTO = (GeneratedTransferObject) type;

                if (genTO.getName().equals("CompositeKeyListKey")) {
                    compositeKeyListKeyCount++;
                    final List<GeneratedProperty> properties = genTO.getProperties();
                    for (final GeneratedProperty prop : properties) {
                        if (prop.getName().equals("key1")) {
                            compositeKeyListKeyPropertyCount++;
                        } else if (prop.getName().equals("key2")) {
                            compositeKeyListKeyPropertyCount++;
                        }
                    }
                    genTOsCount++;
                } else if (genTO.getName().equals("InnerListKey")) {
                    final List<GeneratedProperty> properties = genTO.getProperties();
                    innerListKeyPropertyCount = properties.size();
                    genTOsCount++;
                }
            }
        }
        assertEquals(1, compositeKeyListKeyCount);
        assertEquals(2, compositeKeyListKeyPropertyCount);

        assertEquals(1, innerListKeyPropertyCount);

        assertEquals(5, genTypesCount);
        assertEquals(2, genTOsCount);
    }

    @Test
    public void testGeneratedTypes() {
        final SchemaContext context = YangParserTestUtils.parseYangResource("/demo-topology.yang");
        assertNotNull(context);

        final List<Type> genTypes = DefaultBindingGenerator.generateFor(context);

        assertNotNull(genTypes);
        assertEquals(14, genTypes.size());

        int genTypesCount = 0;
        int genTOsCount = 0;
        for (final Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                genTypesCount++;
            } else if (type instanceof GeneratedTransferObject) {
                genTOsCount++;
            }
        }

        assertEquals(11, genTypesCount);
        assertEquals(3, genTOsCount);
    }
}
