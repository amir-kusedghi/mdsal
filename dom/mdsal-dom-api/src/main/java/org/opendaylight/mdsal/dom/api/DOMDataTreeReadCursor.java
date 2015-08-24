/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface DOMDataTreeReadCursor extends DOMDataTreeCursor {

    /**
     * Read a particular node from the snapshot.
     *
     * @param child Child identifier
     * @return Optional result encapsulating the presence and value of the node
     * @throws IllegalArgumentException when specified path does not identify a valid child.
     */
    CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> readNode(@Nonnull PathArgument child);

    /**
     * Checks if data is available in the logical data store located at provided path.
     * <p>
     *
     * Note: a successful result from this method makes no guarantee that a subsequent call to
     * {@link #readNode(PathArgument)} will succeed. It is possible that the data resides in a data store on a remote
     * node and, if that node goes down or a network failure occurs, a subsequent read would fail.
     * Another scenario is if the data is deleted in between the calls to <code>exists</code> and
     * <code>readNode</code>
     *
     * @param child Child identifier
     * @return a CheckFuture containing the result of the check.
     *         <ul>
     *         <li>If the data at the supplied path exists, the Future returns a Boolean whose value
     *         is true, false otherwise</li> <li>If checking for the data fails, the Future will
     *         fail with a {@link ReadFailedException} or an exception derived from
     *         ReadFailedException.</li>
     *         </ul>
     */
    CheckedFuture<Boolean, ReadFailedException> exists(@Nonnull PathArgument child);
}
