/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.IncorrectDataStructureException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNodeFactory;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class SchemaAwareApplyOperation implements ModificationApplyOperation {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaAwareApplyOperation.class);

    public static SchemaAwareApplyOperation from(final DataSchemaNode schemaNode) {
        if (schemaNode instanceof ContainerSchemaNode) {
            return new DataNodeContainerModificationStrategy.ContainerModificationStrategy((ContainerSchemaNode) schemaNode);
        } else if (schemaNode instanceof ListSchemaNode) {
            return fromListSchemaNode((ListSchemaNode) schemaNode);
        } else if (schemaNode instanceof ChoiceNode) {
            return new NormalizedNodeContainerModificationStrategy.ChoiceModificationStrategy((ChoiceNode) schemaNode);
        } else if (schemaNode instanceof LeafListSchemaNode) {
            return fromLeafListSchemaNode((LeafListSchemaNode) schemaNode);
        } else if (schemaNode instanceof LeafSchemaNode) {
            return new ValueNodeModificationStrategy.LeafModificationStrategy((LeafSchemaNode) schemaNode);
        }
        throw new IllegalArgumentException("Not supported schema node type for " + schemaNode.getClass());
    }

    public static SchemaAwareApplyOperation from(final DataNodeContainer resolvedTree,
            final AugmentationTarget augSchemas, final AugmentationIdentifier identifier) {
        AugmentationSchema augSchema = null;

        allAugments:
            for (AugmentationSchema potential : augSchemas.getAvailableAugmentations()) {
                for (DataSchemaNode child : potential.getChildNodes()) {
                    if (identifier.getPossibleChildNames().contains(child.getQName())) {
                        augSchema = potential;
                        break allAugments;
                    }
                }
            }

        if (augSchema != null) {
            return new DataNodeContainerModificationStrategy.AugmentationModificationStrategy(augSchema, resolvedTree);
        }
        return null;
    }

    public static boolean checkConflicting(final YangInstanceIdentifier path, final boolean condition, final String message) throws ConflictingModificationAppliedException {
        if(!condition) {
            throw new ConflictingModificationAppliedException(path, message);
        }
        return condition;
    }

    private static SchemaAwareApplyOperation fromListSchemaNode(final ListSchemaNode schemaNode) {
        List<QName> keyDefinition = schemaNode.getKeyDefinition();
        if (keyDefinition == null || keyDefinition.isEmpty()) {
            return new UnkeyedListModificationStrategy(schemaNode);
        }
        if (schemaNode.isUserOrdered()) {
            return new NormalizedNodeContainerModificationStrategy.OrderedMapModificationStrategy(schemaNode);
        }

        return new NormalizedNodeContainerModificationStrategy.UnorderedMapModificationStrategy(schemaNode);
    }

    private static SchemaAwareApplyOperation fromLeafListSchemaNode(final LeafListSchemaNode schemaNode) {
        if(schemaNode.isUserOrdered()) {
            return new NormalizedNodeContainerModificationStrategy.OrderedLeafSetModificationStrategy(schemaNode);
        } else {
            return new NormalizedNodeContainerModificationStrategy.UnorderedLeafSetModificationStrategy(schemaNode);
        }
    }

    private static final void checkNotConflicting(final YangInstanceIdentifier path, final TreeNode original, final TreeNode current) throws ConflictingModificationAppliedException {
        checkConflicting(path, original.getVersion().equals(current.getVersion()),
                "Node was replaced by other transaction.");
        checkConflicting(path, original.getSubtreeVersion().equals(current.getSubtreeVersion()),
                "Node children was modified by other transaction");
    }

    protected final ModificationApplyOperation resolveChildOperation(final PathArgument child) {
        Optional<ModificationApplyOperation> potential = getChild(child);
        Preconditions.checkArgument(potential.isPresent(), "Operation for child %s is not defined.", child);
        return potential.get();
    }

    @Override
    public void verifyStructure(final ModifiedNode modification) throws IllegalArgumentException {
        if (modification.getType() == ModificationType.WRITE) {
            verifyWrittenStructure(modification.getWrittenValue());
        }
    }

    @Override
    public final void checkApplicable(final YangInstanceIdentifier path,final NodeModification modification, final Optional<TreeNode> current) throws DataValidationFailedException {
        switch (modification.getType()) {
        case DELETE:
            checkDeleteApplicable(modification, current);
        case SUBTREE_MODIFIED:
            checkSubtreeModificationApplicable(path, modification, current);
            return;
        case WRITE:
            checkWriteApplicable(path, modification, current);
            return;
        case MERGE:
            checkMergeApplicable(path, modification, current);
            return;
        case UNMODIFIED:
            return;
        default:
            throw new UnsupportedOperationException("Suplied modification type "+ modification.getType()+ "is not supported.");
        }

    }

    protected void checkMergeApplicable(final YangInstanceIdentifier path, final NodeModification modification, final Optional<TreeNode> current) throws DataValidationFailedException {
        Optional<TreeNode> original = modification.getOriginal();
        if (original.isPresent() && current.isPresent()) {
            /*
             * We need to do conflict detection only and only if the value of leaf changed
             * before two transactions. If value of leaf is unchanged between two transactions
             * it should not cause transaction to fail, since result of this merge
             * leads to same data.
             */
            if(!original.get().getData().equals(current.get().getData())) {
                checkNotConflicting(path, original.get(), current.get());
            }
        }
    }

    protected void checkWriteApplicable(final YangInstanceIdentifier path, final NodeModification modification, final Optional<TreeNode> current) throws DataValidationFailedException {
        Optional<TreeNode> original = modification.getOriginal();
        if (original.isPresent() && current.isPresent()) {
            checkNotConflicting(path, original.get(), current.get());
        } else if(original.isPresent()) {
            throw new ConflictingModificationAppliedException(path,"Node was deleted by other transaction.");
        }
    }

    private void checkDeleteApplicable(final NodeModification modification, final Optional<TreeNode> current) {
        // Delete is always applicable, we do not expose it to subclasses
        if (current.isPresent()) {
            LOG.trace("Delete operation turned to no-op on missing node {}", modification);
        }
    }

    @Override
    public final Optional<TreeNode> apply(final ModifiedNode modification,
            final Optional<TreeNode> currentMeta, final Version version) {

        switch (modification.getType()) {
        case DELETE:
            return modification.storeSnapshot(Optional.<TreeNode> absent());
        case SUBTREE_MODIFIED:
            Preconditions.checkArgument(currentMeta.isPresent(), "Metadata not available for modification",
                    modification);
            return modification.storeSnapshot(Optional.of(applySubtreeChange(modification, currentMeta.get(),
                    version)));
        case MERGE:
            if(currentMeta.isPresent()) {
                return modification.storeSnapshot(Optional.of(applyMerge(modification,currentMeta.get(), version)));
            }
            // intentional fall-through: if the node does not exist a merge is same as a write
        case WRITE:
            return modification.storeSnapshot(Optional.of(applyWrite(modification, currentMeta, version)));
        case UNMODIFIED:
            return currentMeta;
        default:
            throw new IllegalArgumentException("Provided modification type is not supported.");
        }
    }

    protected abstract TreeNode applyMerge(ModifiedNode modification,
            TreeNode currentMeta, Version version);

    protected abstract TreeNode applyWrite(ModifiedNode modification,
            Optional<TreeNode> currentMeta, Version version);

    protected abstract TreeNode applySubtreeChange(ModifiedNode modification,
            TreeNode currentMeta, Version version);

    /**
     *
     * Checks is supplied {@link NodeModification} is applicable for Subtree Modification.
     *
     * @param path Path to current node
     * @param modification Node modification which should be applied.
     * @param current Current state of data tree
     * @throws ConflictingModificationAppliedException If subtree was changed in conflicting way
     * @throws IncorrectDataStructureException If subtree modification is not applicable (e.g. leaf node).
     */
    protected abstract void checkSubtreeModificationApplicable(YangInstanceIdentifier path, final NodeModification modification,
            final Optional<TreeNode> current) throws DataValidationFailedException;

    protected abstract void verifyWrittenStructure(NormalizedNode<?, ?> writtenValue);

    public static class UnkeyedListModificationStrategy extends SchemaAwareApplyOperation {

        private final Optional<ModificationApplyOperation> entryStrategy;

        protected UnkeyedListModificationStrategy(final ListSchemaNode schema) {
            entryStrategy = Optional.<ModificationApplyOperation> of(new DataNodeContainerModificationStrategy.UnkeyedListItemModificationStrategy(schema));
        }

        @Override
        protected TreeNode applyMerge(final ModifiedNode modification, final TreeNode currentMeta,
                final Version version) {
            return applyWrite(modification, Optional.of(currentMeta), version);
        }

        @Override
        protected TreeNode applySubtreeChange(final ModifiedNode modification,
                final TreeNode currentMeta, final Version version) {
            throw new UnsupportedOperationException("UnkeyedList does not support subtree change.");
        }

        @Override
        protected TreeNode applyWrite(final ModifiedNode modification,
                final Optional<TreeNode> currentMeta, final Version version) {
            /*
             * FIXME: BUG-1258: This is inefficient: it needlessly creates index nodes for the entire subtree.
             *        We can determine the depth into which metadata need to be created from the modification
	     *        -- if it does not have children, no need to bother with metadata.
             */
            return TreeNodeFactory.createTreeNode(modification.getWrittenValue(), version);
        }

        @Override
        public Optional<ModificationApplyOperation> getChild(final PathArgument child) {
            if (child instanceof NodeIdentifier) {
                return entryStrategy;
            }
            return Optional.absent();
        }

        @Override
        protected void verifyWrittenStructure(final NormalizedNode<?, ?> writtenValue) {

        }

        @Override
        protected void checkSubtreeModificationApplicable(final YangInstanceIdentifier path, final NodeModification modification,
                final Optional<TreeNode> current) throws IncorrectDataStructureException {
            throw new IncorrectDataStructureException(path, "Subtree modification is not allowed.");
        }
    }
}
