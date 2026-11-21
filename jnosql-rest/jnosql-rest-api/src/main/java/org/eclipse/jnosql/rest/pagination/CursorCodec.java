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
package org.eclipse.jnosql.rest.pagination;

import jakarta.data.page.PageRequest;
import jakarta.ws.rs.BadRequestException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts the key of a Jakarta Data cursor into an opaque, URL-safe continuation token and back.
 * <p>
 * Token layout, before Base64URL encoding: {@code v1;<N|P>;<context hash>;<type>~<value>;...}. {@code N} continues
 * after the cursor, {@code P} before it. Each value is Base64URL encoded and tagged with a one-letter type, so key
 * types survive the round trip without Java serialization. The context hash binds the token to the repository method
 * and its criteria.
 * <p>
 * The token is <strong>not</strong> encrypted or signed: it exposes the sort key values of one element and a client
 * can forge one. A forged token can only move the position inside a result set the client may already query; it must
 * never be used to carry authorization.
 */
public final class CursorCodec {

    /** The longest token accepted, in characters. */
    public static final int MAX_TOKEN_LENGTH = 1024;

    private static final String VERSION = "v1";
    private static final int MAX_KEYS = 8;
    private static final Map<Class<?>, Character> TAGS = tags();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private CursorCodec() {
    }

    /**
     * A decoded continuation token.
     *
     * @param cursor   the cursor key
     * @param previous whether the page before the cursor is requested
     */
    public record Token(PageRequest.Cursor cursor, boolean previous) {
    }

    /**
     * Encodes the cursor of a navigation request obtained from {@code nextPageRequest()} or
     * {@code previousPageRequest()}.
     *
     * @param request the navigation request
     * @param context the query context the token is bound to
     * @return the token
     * @throws IllegalStateException if the request holds no cursor, a key of an unsupported type, more than eight
     *                               keys or a token longer than {@value #MAX_TOKEN_LENGTH} characters
     */
    public static String encode(PageRequest request, String context) {
        PageRequest.Cursor cursor = request.cursor()
                .orElseThrow(() -> new IllegalStateException("The page request carries no cursor"));
        boolean previous = request.mode() == PageRequest.Mode.CURSOR_PREVIOUS;
        if (cursor.elements().size() > MAX_KEYS) {
            throw new IllegalStateException("The cursor has more than " + MAX_KEYS + " keys");
        }
        StringBuilder text = new StringBuilder(VERSION).append(';').append(previous ? 'P' : 'N').append(';')
                .append(hash(context));
        for (Object key : cursor.elements()) {
            text.append(';').append(tag(key)).append('~').append(encodeText(String.valueOf(key)));
        }
        String token = ENCODER.encodeToString(text.toString().getBytes(StandardCharsets.UTF_8));
        if (token.length() > MAX_TOKEN_LENGTH) {
            throw new IllegalStateException("The cursor token exceeds " + MAX_TOKEN_LENGTH + " characters");
        }
        return token;
    }

    /**
     * Decodes and validates a token.
     *
     * @param token   the token received from the client
     * @param context the query context of the current request
     * @return the decoded token
     * @throws BadRequestException if the token is malformed, unsupported, oversized or issued for another query
     */
    public static Token decode(String token, String context) {
        if (token.length() > MAX_TOKEN_LENGTH) {
            throw invalid("is too long");
        }
        String[] parts;
        try {
            parts = new String(DECODER.decode(token), StandardCharsets.UTF_8).split(";", -1);
        } catch (IllegalArgumentException exception) {
            throw invalid("is malformed");
        }
        if (parts.length < 4 || parts.length > 3 + MAX_KEYS) {
            throw invalid("is malformed");
        }
        if (!VERSION.equals(parts[0])) {
            throw invalid("uses an unsupported version");
        }
        if (!parts[1].equals("N") && !parts[1].equals("P")) {
            throw invalid("is malformed");
        }
        if (!parts[2].equals(hash(context))) {
            throw invalid("was issued for a different query");
        }
        List<Object> keys = new ArrayList<>();
        for (int index = 3; index < parts.length; index++) {
            keys.add(key(parts[index]));
        }
        return new Token(PageRequest.Cursor.forKey(keys.toArray()), parts[1].equals("P"));
    }

    private static BadRequestException invalid(String reason) {
        return new BadRequestException("The cursor " + reason);
    }

    private static String hash(String context) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(context.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String encodeText(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static Map<Class<?>, Character> tags() {
        Map<Class<?>, Character> tags = new LinkedHashMap<>();
        tags.put(String.class, 'S');
        tags.put(Integer.class, 'I');
        tags.put(Long.class, 'L');
        tags.put(Short.class, 'H');
        tags.put(Byte.class, 'B');
        tags.put(Boolean.class, 'Z');
        tags.put(Double.class, 'D');
        tags.put(Float.class, 'F');
        tags.put(BigDecimal.class, 'N');
        tags.put(BigInteger.class, 'G');
        tags.put(UUID.class, 'U');
        tags.put(LocalDate.class, 'd');
        tags.put(LocalDateTime.class, 't');
        tags.put(LocalTime.class, 'm');
        tags.put(Instant.class, 'i');
        return tags;
    }

    private static char tag(Object key) {
        if (key == null) {
            throw new IllegalStateException("A null cursor key cannot be encoded");
        }
        for (Map.Entry<Class<?>, Character> entry : TAGS.entrySet()) {
            if (entry.getKey().isInstance(key)) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException("The cursor key type " + key.getClass().getName() + " is not supported");
    }

    private static Object key(String part) {
        int separator = part.indexOf('~');
        if (separator != 1) {
            throw invalid("is malformed");
        }
        try {
            String value = new String(DECODER.decode(part.substring(2)), StandardCharsets.UTF_8);
            return switch (part.charAt(0)) {
                case 'S' -> value;
                case 'I' -> Integer.valueOf(value);
                case 'L' -> Long.valueOf(value);
                case 'H' -> Short.valueOf(value);
                case 'B' -> Byte.valueOf(value);
                case 'Z' -> bool(value);
                case 'D' -> Double.valueOf(value);
                case 'F' -> Float.valueOf(value);
                case 'N' -> new BigDecimal(value);
                case 'G' -> new BigInteger(value);
                case 'U' -> UUID.fromString(value);
                case 'd' -> LocalDate.parse(value);
                case 't' -> LocalDateTime.parse(value);
                case 'm' -> LocalTime.parse(value);
                case 'i' -> Instant.parse(value);
                default -> throw invalid("holds an unsupported key type");
            };
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException exception) {
            throw invalid("is malformed");
        }
    }

    private static Boolean bool(String value) {
        if (!value.equals("true") && !value.equals("false")) {
            throw new IllegalArgumentException(value);
        }
        return Boolean.valueOf(value);
    }
}
