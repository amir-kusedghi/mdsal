/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeComment;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;

public final class CodegenEnumerationBuilder extends AbstractEnumerationBuilder {
    private String description;
    private String reference;
    private String moduleName;
    private SchemaPath schemaPath;

    public CodegenEnumerationBuilder(final JavaTypeName identifier) {
        super(identifier);
    }

    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setSchemaPath(final SchemaPath schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;

    }

    @Override
    public Enumeration toInstance(final Type definingType) {
        return new EnumerationImpl(this, definingType);
    }

    @Override
    EnumPair createEnumPair(final String name, final String mappedName, final int value, final Status status,
            final String enumDescription, final String enumReference) {
        return new EnumPair(name, mappedName, value, status, enumDescription, enumReference);
    }

    private static final class EnumPair extends AbstractPair {
        private final String description;
        private final String reference;
        private final Status status;

        EnumPair(final String name, final String mappedName, final int value, final Status status,
                final String description, final String reference) {
            super(name, mappedName, value);
            this.status = requireNonNull(status);
            this.description = description;
            this.reference = reference;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public Optional<String> getReference() {
            return Optional.ofNullable(reference);
        }

        @Override
        public Status getStatus() {
            return status;
        }
    }

    private static final class EnumerationImpl extends AbstractEnumeration {
        private final String description;
        private final String reference;
        private final String moduleName;
        private final SchemaPath schemaPath;

        EnumerationImpl(final CodegenEnumerationBuilder builder, final Type definingType) {
            super(builder, definingType);
            this.description = builder.description;
            this.moduleName = builder.moduleName;
            this.schemaPath = builder.schemaPath;
            this.reference = builder.reference;
        }

        @Override
        public TypeComment getComment() {
            return null;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getReference() {
            return this.reference;
        }

        @Override
        public Iterable<QName> getSchemaPath() {
            return this.schemaPath.getPathFromRoot();
        }

        @Override
        public String getModuleName() {
            return this.moduleName;
        }

        @Override
        public Optional<YangSourceDefinition> getYangSourceDefinition() {
            return Optional.empty();
        }
    }
}
