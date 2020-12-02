/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.if_feature;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithArgument.WithSubstatements;

final class RegularIfFeatureStatement extends WithSubstatements<IfFeatureExpr> implements IfFeatureStatement {
    RegularIfFeatureStatement(final @NonNull String rawArgument, final @NonNull IfFeatureExpr argument,
                              final ImmutableList<? extends DeclaredStatement<?>> substatements,
                              final StatementSourceReference sourceReference) {
        super(rawArgument, argument, substatements, sourceReference);
    }
}
