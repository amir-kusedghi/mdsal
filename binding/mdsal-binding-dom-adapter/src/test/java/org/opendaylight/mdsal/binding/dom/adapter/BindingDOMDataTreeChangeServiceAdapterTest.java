/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.binding.runtime.spi.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Unit tests for BindingDOMDataTreeChangeServiceAdapter.
 *
 * @author Thomas Pantelis
 */
public class BindingDOMDataTreeChangeServiceAdapterTest {
    private static final InstanceIdentifier<Top> TOP_PATH = InstanceIdentifier.create(Top.class);

    @Mock
    private DOMDataTreeChangeService mockDOMService;

    @Mock
    private GeneratedClassLoadingStrategy classLoadingStrategy;

    @Mock
    private BindingNormalizedNodeCodecRegistry codecRegistry;

    @Mock
    private YangInstanceIdentifier mockYangID;

    @SuppressWarnings("rawtypes")
    @Mock
    private ListenerRegistration mockDOMReg;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(this.mockYangID).when(this.codecRegistry).toYangInstanceIdentifier(TOP_PATH);
    }

    @Test
    public void testRegisterDataTreeChangeListener() {
        final BindingToNormalizedNodeCodec codec = new BindingToNormalizedNodeCodec(
            new DefaultBindingRuntimeGenerator(), classLoadingStrategy, codecRegistry);

        final DataTreeChangeService service = BindingDOMDataTreeChangeServiceAdapter.create(codec, this.mockDOMService);

        doReturn(this.mockDOMReg).when(this.mockDOMService).registerDataTreeChangeListener(
                domDataTreeIdentifier(this.mockYangID),
                any(DOMDataTreeChangeListener.class));
        final DataTreeIdentifier<Top> treeId = DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, TOP_PATH);
        final TestClusteredDataTreeChangeListener mockClusteredListener = new TestClusteredDataTreeChangeListener();
        service.registerDataTreeChangeListener(treeId , mockClusteredListener);

        verify(this.mockDOMService).registerDataTreeChangeListener(domDataTreeIdentifier(this.mockYangID),
                isA(ClusteredDOMDataTreeChangeListener.class));

        reset(this.mockDOMService);
        doReturn(this.mockDOMReg).when(this.mockDOMService).registerDataTreeChangeListener(
                domDataTreeIdentifier(this.mockYangID), any(DOMDataTreeChangeListener.class));
        final TestDataTreeChangeListener mockNonClusteredListener = new TestDataTreeChangeListener();
        service.registerDataTreeChangeListener(treeId , mockNonClusteredListener);

        verify(this.mockDOMService).registerDataTreeChangeListener(domDataTreeIdentifier(this.mockYangID),
                not(isA(ClusteredDOMDataTreeChangeListener.class)));
    }

    static DOMDataTreeIdentifier domDataTreeIdentifier(final YangInstanceIdentifier yangID) {
        return argThat(arg -> arg.getDatastoreType() == LogicalDatastoreType.CONFIGURATION
                && yangID.equals(arg.getRootIdentifier()));
    }

    private static class TestClusteredDataTreeChangeListener implements ClusteredDataTreeChangeListener<Top> {
        @Override
        public void onDataTreeChanged(final Collection<DataTreeModification<Top>> changes) {
        }
    }

    private static class TestDataTreeChangeListener implements DataTreeChangeListener<Top> {
        @Override
        public void onDataTreeChanged(final Collection<DataTreeModification<Top>> changes) {
        }
    }
}
