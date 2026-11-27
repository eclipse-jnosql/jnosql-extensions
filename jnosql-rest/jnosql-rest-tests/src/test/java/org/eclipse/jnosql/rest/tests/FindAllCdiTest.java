/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.rest.tests;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.jnosql.rest.pagination.PageResponse;
import org.eclipse.jnosql.rest.tests.findall.Gadget;
import org.eclipse.jnosql.rest.tests.findall.GadgetRepository;
import org.eclipse.jnosql.rest.tests.findall.GadgetRepositoryResource;
import org.jboss.weld.junit.MockBean;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableWeld
@DisplayName("Generated resource for custom findAll methods with CDI")
class FindAllCdiTest {

    private static final GadgetRepository GADGETS = Mockito.mock(GadgetRepository.class);

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(WeldInitiator.createWeld()
                    .addBeanClasses(GadgetRepositoryResource.class))
            .addBeans(MockBean.<GadgetRepository>builder().types(GadgetRepository.class)
                    .scope(ApplicationScoped.class).creating(GADGETS).build())
            .activate(RequestScoped.class)
            .build();

    @BeforeEach
    void resetMocks() {
        Mockito.reset(GADGETS);
    }

    private GadgetRepositoryResource resource() {
        return weld.select(GadgetRepositoryResource.class).get();
    }

    private static Gadget gadget(long id, String name) {
        Gadget gadget = new Gadget();
        gadget.setId(id);
        gadget.setName(name);
        return gadget;
    }

    @Test
    @DisplayName("Should serve the custom findAll at /findAll and delegate to it")
    void shouldDelegateCustomFindAll() throws NoSuchMethodException {
        var method = GadgetRepositoryResource.class.getMethod("findAll");
        when(GADGETS.findAll()).thenReturn(List.of(gadget(1, "lamp")));

        List<Gadget> result = resource().findAll();

        assertThat(method.getAnnotation(GET.class)).isNotNull();
        assertThat(method.getAnnotation(Path.class).value()).isEqualTo("/findAll");
        assertThat(result).extracting(Gadget::getName).containsExactly("lamp");
        verify(GADGETS).findAll();
    }

    @Test
    @DisplayName("Should serve the custom paginated findAll at its explicit path and delegate to it")
    @SuppressWarnings("unchecked")
    void shouldDelegateCustomPaginatedFindAll() throws NoSuchMethodException {
        var method = GadgetRepositoryResource.class.getMethod("findAll", String.class, String.class, String.class);
        Page<Gadget> page = Mockito.mock(Page.class);
        when(page.content()).thenReturn(List.of(gadget(2, "fan")));
        when(page.pageRequest()).thenReturn(PageRequest.ofPage(1, 20, false));
        when(GADGETS.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponse<Gadget> response = resource().findAll(null, null, null);

        assertThat(method.getAnnotation(Path.class).value()).isEqualTo("/paged");
        assertThat(response.content()).extracting(Gadget::getName).containsExactly("fan");
        verify(GADGETS).findAll(PageRequest.ofPage(1, 20, false));
    }
}
