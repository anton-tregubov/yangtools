/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.ArgumentToString.WithSubstatements;

final class RegularRevisionStatement extends WithSubstatements<Revision> implements RevisionStatement {
    RegularRevisionStatement(final Revision argument,
                             final ImmutableList<? extends DeclaredStatement<?>> substatements,
                             final StatementSourceReference sourceReference) {
        super(argument, substatements, sourceReference);
    }
}
