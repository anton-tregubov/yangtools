/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.stream.Stream;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Utility forwarding implementation of {@link StatementSupport} contract. This class is useful for implementing
 * wrapped statements.
 *
 * @author Robert Varga
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class ForwardingStatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
    extends ForwardingObject implements StatementSupport<A, D, E> {

    @Override
    protected abstract StatementSupport<A, D, E> delegate();

    @Override
    public D createDeclared(final StmtContext<A, D, ?> ctx) {
        return delegate().createDeclared(ctx);
    }

    @Override
    public E createEffective(final Current<A, D> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> declaredSubstatements,
            final Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements) {
        return delegate().createEffective(stmt, declaredSubstatements, effectiveSubstatements);
    }

    @Override
    public StatementDefinition getPublicView() {
        return delegate().getPublicView();
    }

    @Override
    public A parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return delegate().parseArgumentValue(ctx, value);
    }

    @Override
    public void onStatementAdded(final Mutable<A, D, E> stmt) {
        delegate().onStatementAdded(stmt);
    }

    @Override
    public void onPreLinkageDeclared(final Mutable<A, D, E> stmt) {
        delegate().onPreLinkageDeclared(stmt);
    }

    @Override
    public void onLinkageDeclared(final Mutable<A, D, E> stmt) {
        delegate().onLinkageDeclared(stmt);
    }

    @Override
    public void onStatementDefinitionDeclared(final Mutable<A, D, E> stmt) {
        delegate().onStatementDefinitionDeclared(stmt);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<A, D, E> stmt) {
        delegate().onFullDefinitionDeclared(stmt);
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        return delegate().hasArgumentSpecificSupports();
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        return delegate().getSupportSpecificForArgument(argument);
    }

    @Override
    public CopyPolicy applyCopyPolicy(final Mutable<?, ?, ?> stmt, final Mutable<?, ?, ?> parent,
            final CopyType copyType, final QNameModule targetModule) {
        return delegate().applyCopyPolicy(stmt, parent, copyType, targetModule);
    }
}
