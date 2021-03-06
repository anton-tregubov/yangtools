/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.base.Converter;
import com.google.common.base.VerifyException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.xml.xpath.XPathExpressionException;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.UnresolvableException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.jaxen.api.XPathDocument;
import org.opendaylight.yangtools.yang.data.jaxen.api.XPathExpression;
import org.opendaylight.yangtools.yang.data.jaxen.api.XPathNodesetResult;
import org.opendaylight.yangtools.yang.data.jaxen.api.XPathResult;
import org.opendaylight.yangtools.yang.data.jaxen.api.XPathSchemaContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class JaxenTest {
    private ConverterNamespaceContext convertNctx;
    private XPathSchemaContext xpathSchemaContext;
    private XPathDocument xpathDocument;
    private XPathExpression xpathExpression;
    private NormalizedNodeNavigator navigator;

    private QNameModule moduleQName;
    private QName rootQName;
    private QName listAQName;
    private QName listBQName;
    private QName leafAQName;
    private QName leafBQName;
    private QName leafDQName;
    private QName containerAQName;
    private QName containerBQName;

    @Before
    public void setup() throws XPathExpressionException {
        final EffectiveModelContext schemaContext =
            YangParserTestUtils.parseYangResourceDirectory("/test/documentTest");
        assertNotNull(schemaContext);

        initQNames();
        xpathSchemaContext = new JaxenSchemaContextFactory().createContext(schemaContext);
        assertNotNull(xpathSchemaContext);

        xpathExpression = xpathSchemaContext.compileExpression(createSchemaPath(), createPrefixes(), createXPath(
                    false));
        assertNotNull(xpathExpression);

        xpathDocument = xpathSchemaContext.createDocument(TestUtils.createNormalizedNodes());
        assertNotNull(xpathDocument);
        String rootNodeName = xpathDocument.getRootNode().getNodeType().getLocalName();
        assertNotNull(rootNodeName);
        assertEquals("root", rootNodeName);

        Optional<? extends XPathResult<?>> resultExpressionEvaluate = xpathExpression
                .evaluate(xpathDocument, createYangInstanceIdentifier(false));
        assertNotNull(resultExpressionEvaluate);
        assertTrue(resultExpressionEvaluate.isPresent());
        XPathResult<?> xpathResult = resultExpressionEvaluate.get();
        assertTrue(xpathResult instanceof XPathNodesetResult);
        XPathNodesetResult nodeset = (XPathNodesetResult) xpathResult;

        Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = nodeset.getValue().iterator().next();
        assertNotNull(entry);
        assertEquals("three", entry.getValue().getValue());

        convertNctx = new ConverterNamespaceContext(createPrefixes());
        navigator = new NormalizedNodeNavigator(convertNctx, (JaxenDocument) xpathDocument);
        assertNotNull(navigator);
    }

    @Test
    public void testConverterNamespaceContextBackFront() {
        assertEquals("test2", convertNctx.doBackward(moduleQName));
        assertEquals(moduleQName, convertNctx.doForward("test2"));
    }

    @Test
    public void testConverterNamespaceContextPrefixJaxenName() {
        assertNotNull(rootQName);
        assertEquals("test2:root", convertNctx.jaxenQName(rootQName));
        String prefix = convertNctx.translateNamespacePrefixToUri("test2");
        assertNotNull(prefix);
        assertEquals("urn:opendaylight.test2", prefix);
    }

    @Test
    public void testCompileExpression() {
        assertNotNull(xpathExpression.getApexPath());
        assertEquals(createSchemaPath(), xpathExpression.getEvaluationPath());
    }

    @Test
    public void testJaxenXpath() throws XPathExpressionException {
        assertNotNull(xpathExpression.evaluate(xpathDocument, createYangInstanceIdentifier(false)));
    }

    @Test
    public void testXpathWithPredicates() throws XPathExpressionException {
        XPathExpression xpathExpressionWithPredicates = xpathSchemaContext.compileExpression(createSchemaPath(),
                createPrefixes(), createXPath(true));

        Optional<? extends XPathResult<?>> resultExpressionEvaluate = xpathExpressionWithPredicates
                .evaluate(xpathDocument, createYangInstanceIdentifier(true));
        assertTrue(resultExpressionEvaluate.isPresent());
        XPathResult<?> xpathResult = resultExpressionEvaluate.get();
        assertTrue(xpathResult instanceof XPathNodesetResult);
        XPathNodesetResult nodeset = (XPathNodesetResult) xpathResult;

        Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> entry = nodeset.getValue().iterator().next();
        assertNotNull(entry);
        assertEquals("two", entry.getValue().getValue());
    }

    @Test(expected = VerifyException.class)
    public void testIsMethodsInNodeNavigator() {
        assertTrue(navigator.isDocument("test"));
    }

    @Test(expected = XPathExpressionException.class)
    public void testCompileExpressionException() throws XPathExpressionException {
        assertNotNull(xpathSchemaContext.compileExpression(createSchemaPath(), createPrefixes(), "/broken-path*"));
    }

    @Test(expected = UnresolvableException.class)
    public void testYangFunctionContext() throws UnresolvableException, FunctionCallException {
        final YangFunctionContext yangFun = YangFunctionContext.getInstance();
        assertNotNull(yangFun);
        final Function function = yangFun.getFunction("urn:opendaylight.test2", null, "current");
        assertNotNull(function);

        try {
            final Context context = mock(Context.class);
            final ArrayList<Object> list = new ArrayList<>();
            function.call(context, list);
            fail();
        } catch (VerifyException e) {
            // Expected
        }

        yangFun.getFunction("urn:opendaylight.test2", "test2", "root");
    }

    /*
     * container-a -> container-b -> leaf-d
     *           list-a -> list-b -> leaf-b
     */
    private YangInstanceIdentifier createYangInstanceIdentifier(final boolean withPredicates) {
        YangInstanceIdentifier testYangInstanceIdentifier = YangInstanceIdentifier.of(containerAQName).node(
                containerBQName).node(leafDQName);
        if (withPredicates) {
            final Map<QName, Object> keys1 = new HashMap<>();
            keys1.put(leafAQName, "bar");

            final NodeIdentifierWithPredicates mapEntryPath1 = NodeIdentifierWithPredicates.of(listAQName , keys1);

            final Map<QName, Object> keys2 = new HashMap<>();
            keys2.put(leafBQName, "two");

            final NodeIdentifierWithPredicates mapEntryPath2 = NodeIdentifierWithPredicates.of(listBQName , keys2);

            testYangInstanceIdentifier = YangInstanceIdentifier.of(listAQName).node(mapEntryPath1)
                    .node(listBQName).node(mapEntryPath2).node(leafBQName);
        }
        return testYangInstanceIdentifier;
    }

    private static String createXPath(final boolean withPredicates) {
        return withPredicates ? "/list-a[leaf-a='bar']/list-b[leaf-b='two']/leaf-b" : "/container-a/container-b/leaf-d";
    }

    private Converter<String, QNameModule> createPrefixes() {
        BiMap<String, QNameModule> currentConverter = HashBiMap.create();
        currentConverter.put("test2", moduleQName);

        return Maps.asConverter(currentConverter);
    }

    // rootQName -> listAQName -> leafAQName
    private  SchemaPath createSchemaPath() {
        return SchemaPath.create(true, rootQName, listAQName, leafAQName);
    }

    private void initQNames() {
        this.moduleQName = QNameModule.create(URI.create("urn:opendaylight.test2"), Revision.of("2015-08-08"));
        this.rootQName = QName.create(moduleQName, "root");
        this.listAQName = QName.create(moduleQName, "list-a");
        this.listBQName = QName.create(moduleQName, "list-b");
        this.leafAQName = QName.create(moduleQName, "leaf-a");
        this.leafBQName = QName.create(moduleQName, "leaf-b");
        this.leafDQName = QName.create(moduleQName, "leaf-d");
        this.containerAQName = QName.create(moduleQName, "container-a");
        this.containerBQName = QName.create(moduleQName, "container-b");
    }
}
