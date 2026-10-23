/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package org.eclipse.jnosql.jakartapersistence.glassfish;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Runs one scenario per request; answers HTTP 200 with "OK ..." or HTTP 500 with the full stack trace.
 */
@WebServlet("/scenario")
public class ScenarioServlet extends HttpServlet {

    @Inject
    private CallerManagedService service;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String scenario = req.getParameter("name");
        resp.setContentType("text/plain");
        try {
            String result = run(scenario);
            resp.getWriter().println("OK " + result);
        } catch (Throwable t) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter writer = resp.getWriter();
            t.printStackTrace(writer);
        }
    }

    private String run(String scenario) throws Exception {
        switch (scenario) {
            case "direct" -> {
                service.commitAfter(r -> {
                    if (r.findById(100L).isEmpty()) {
                        r.insert(new Item(100L, "direct"));
                    }
                });
                return "exists=" + service.exists(100L);
            }
            case "default-commit" -> {
                service.commitAfter(r -> r.addIfMissing(new Item(200L, "default")));
                return "exists=" + service.exists(200L);
            }
            case "default-rollback" -> {
                service.rollbackAfter(r -> r.addIfMissing(new Item(300L, "rolledback")));
                return "exists=" + service.exists(300L);
            }
            case "default-transactional" -> {
                service.commitAfter(r -> r.addIfMissingTransactional(new Item(400L, "tx")));
                return "exists=" + service.exists(400L);
            }
            case "nested" -> {
                service.commitAfter(r -> r.addBothIfMissing(new Item(500L, "n1"), new Item(501L, "n2")));
                return "exists=" + (service.exists(500L) && service.exists(501L));
            }
            case "nested-transactional" -> {
                service.commitAfter(r -> r.addBothIfMissingTransactional(new Item(600L, "n1"), new Item(601L, "n2")));
                return "exists=" + (service.exists(600L) && service.exists(601L));
            }
            default -> throw new IllegalArgumentException("Unknown scenario " + scenario);
        }
    }
}
