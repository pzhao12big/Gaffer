/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.federatedstore.operation.handler;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.gov.gchq.gaffer.federatedstore.FederatedStore;
import uk.gov.gchq.gaffer.federatedstore.FederatedStoreConstants;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.OperationChain;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.user.User;

import java.util.LinkedHashSet;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class FederatedOperationHandlerTest {
    public static final String TEST_USER = "testUser";
    private User user;

    @Before
    public void setUp() throws Exception {
        user = new User(TEST_USER);
    }

    @Test
    final public void shouldMergeResultsFromFieldObjects() throws Exception {
        // Given
        final Operation op = Mockito.mock(Operation.class);

        Schema unusedSchema = new Schema.Builder().build();
        Store mockStore1 = getMockStore(unusedSchema);
        Store mockStore2 = getMockStore(unusedSchema);
        Store mockStore3 = getMockStore(unusedSchema);
        Store mockStore4 = getMockStore(unusedSchema);

        Graph graph1 = getGraphWithMockStore(mockStore1);
        Graph graph2 = getGraphWithMockStore(mockStore2);
        Graph graph3 = getGraphWithMockStore(mockStore3);
        Graph graph4 = getGraphWithMockStore(mockStore4);

        Context testContext = new Context(user);

        FederatedStore mockStore = Mockito.mock(FederatedStore.class);
        LinkedHashSet<Graph> linkedGraphs = Sets.newLinkedHashSet();
        linkedGraphs.add(graph1);
        linkedGraphs.add(graph2);
        linkedGraphs.add(graph3);
        linkedGraphs.add(graph4);
        Mockito.when(mockStore.getGraphs(null)).thenReturn(linkedGraphs);

        // When
        new FederatedOperationHandler().doOperation(op, testContext, mockStore);

        verify(mockStore1).execute(new OperationChain<>(op).shallowClone(), user);
        verify(mockStore2).execute(new OperationChain<>(op).shallowClone(), user);
        verify(mockStore3).execute(new OperationChain<>(op).shallowClone(), user);
        verify(mockStore4).execute(new OperationChain<>(op).shallowClone(), user);

    }

    @Test
    final public void shouldMergeResultsFromFieldObjectsWithGivenGraphIds() throws Exception {
        // Given
        final Operation op = Mockito.mock(Operation.class);
        given(op.getOption(FederatedStoreConstants.GRAPH_IDS)).willReturn("1,3");

        Schema unusedSchema = new Schema.Builder().build();
        Store mockStore1 = getMockStore(unusedSchema);
        Store mockStore2 = getMockStore(unusedSchema);
        Store mockStore3 = getMockStore(unusedSchema);
        Store mockStore4 = getMockStore(unusedSchema);

        Graph graph1 = getGraphWithMockStore(mockStore1);
        Graph graph3 = getGraphWithMockStore(mockStore3);

        Context testContext = new Context(user);

        FederatedStore mockStore = Mockito.mock(FederatedStore.class);
        LinkedHashSet<Graph> filteredGraphs = Sets.newLinkedHashSet();
        filteredGraphs.add(graph1);
        filteredGraphs.add(graph3);
        Mockito.when(mockStore.getGraphs("1,3")).thenReturn(filteredGraphs);

        // When
        new FederatedOperationHandler().doOperation(op, testContext, mockStore);

        verify(mockStore1).execute(new OperationChain<>(op).shallowClone(), user);
        verify(mockStore2, never()).execute(new OperationChain<>(op).shallowClone(), user);
        verify(mockStore3).execute(new OperationChain<>(op).shallowClone(), user);
        verify(mockStore4, never()).execute(new OperationChain<>(op).shallowClone(), user);
    }

    private Graph getGraphWithMockStore(final Store mockStore) throws uk.gov.gchq.gaffer.operation.OperationException {
        return new Graph.Builder()
                .graphId("testGraphId")
                .store(mockStore)
                .build();
    }

    private Store getMockStore(final Schema unusedSchema) throws uk.gov.gchq.gaffer.operation.OperationException {
        Store mockStore1 = Mockito.mock(Store.class);
        given(mockStore1.getSchema()).willReturn(unusedSchema);
        return mockStore1;
    }

}