/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ordered_by;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;

final class RegularOrderedByStatement extends AbstractOrderedByStatement {
    private final @NonNull Object substatements;

    RegularOrderedByStatement(final Ordering argument,
                              final ImmutableList<? extends DeclaredStatement<?>> substatements,
                              final StatementSourceReference sourceReference) {
        super(argument, sourceReference);
        this.substatements = maskList(substatements);
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return unmaskList(substatements);
    }
}
