/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;

import com.google.common.base.Optional;

public interface DataContainerNode<K extends PathArgument> extends //
        NormalizedNodeContainer<K, PathArgument, DataContainerChild<? extends PathArgument, ?>> {

    @Override
    public K getIdentifier();

    @Override
    public Iterable<DataContainerChild<?, ?>> getValue();

    @Override
    public Optional<DataContainerChild<?, ?>> getChild(PathArgument child);

}
