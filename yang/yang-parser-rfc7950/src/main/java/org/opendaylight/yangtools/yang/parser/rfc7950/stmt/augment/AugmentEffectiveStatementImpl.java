/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.QNameModuleAware;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultDataNodeContainer;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.ActionNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.NotificationNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.WhenConditionMixin;

final class AugmentEffectiveStatementImpl extends DefaultDataNodeContainer<SchemaNodeIdentifier, AugmentStatement>
        implements AugmentEffectiveStatement, AugmentationSchemaNode, QNameModuleAware,
            DocumentedNodeMixin.WithStatus<SchemaNodeIdentifier, AugmentStatement>,
            ActionNodeContainerMixin<SchemaNodeIdentifier, AugmentStatement>,
            NotificationNodeContainerMixin<SchemaNodeIdentifier, AugmentStatement>,
            WhenConditionMixin<SchemaNodeIdentifier, AugmentStatement> {
    private final @Nullable AugmentationSchemaNode original;
    private final @NonNull SchemaNodeIdentifier argument;
    private final @NonNull QNameModule rootModuleQName;
    private final int flags;

    AugmentEffectiveStatementImpl(final AugmentStatement declared, final SchemaNodeIdentifier argument, final int flags,
            final QNameModule rootModuleQName, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final @Nullable AugmentationSchemaNode original) {
        super(declared, substatements);
        this.argument = requireNonNull(argument);
        this.rootModuleQName = requireNonNull(rootModuleQName);
        this.flags = flags;
        this.original = original;
    }

    @Override
    public @NonNull SchemaNodeIdentifier argument() {
        return argument;
    }

    @Override
    public Optional<AugmentationSchemaNode> getOriginalDefinition() {
        return Optional.ofNullable(this.original);
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public QNameModule getQNameModule() {
        return rootModuleQName;
    }

    @Override
    public AugmentEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        return AugmentEffectiveStatementImpl.class.getSimpleName() + "[" + "targetPath=" + getTargetPath() + ", when="
                + getWhenCondition() + "]";
    }
}
