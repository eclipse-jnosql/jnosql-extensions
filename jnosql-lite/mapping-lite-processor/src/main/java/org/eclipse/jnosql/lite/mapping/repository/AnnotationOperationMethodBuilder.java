/*
 *  Copyright (c) 2023 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.repository;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

enum AnnotationOperationMethodBuilder implements Function<MethodMetadata, List<String>> {

    INSERT {
        @Override
        public List<String> apply(MethodMetadata methodMetadata) {
            Parameter parameter = getParameter(methodMetadata, "The insert method must have only one parameter");
            var returnType = methodMetadata.getReturnType();
            boolean isVoid = AnnotationOperationMethodBuilder.isVoid(returnType);
            boolean isInt = isInt(returnType);
            boolean isLong = isLong(returnType);
           if(parameter.isGeneric()){
               if(isVoid) {
                   return Collections.singletonList( "this.template.insert(" + parameter.name() + ")");
               } else if(isInt){
                   return List.of("this.template.insert(" + parameter.name() + ")",
                           "int resultJNoSQL = (int)java.util.stream.StreamSupport.stream("+ parameter.name()+ ".spliterator(), false).count()");
               } else if (isLong) {
                   return List.of("this.template.insert(" + parameter.name() + ")",
                           "long resultJNoSQL = java.util.stream.StreamSupport.stream(" + parameter.name() + ".spliterator(), false).count()");
               }
               return Collections.singletonList( "var resultJNoSQL = " +
                       "java.util.stream.StreamSupport.stream(this.template.insert(" + parameter.name() + ").spliterator(), false).toList()");
           } else if(parameter.isArray()){
               if(isVoid) {

                   return Collections.singletonList("this.template.insert(java.util.Arrays.stream(" + parameter.name() + ").toList())");
               } else if(isInt){
                   return List.of("this.template.insert(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                           "int resultJNoSQL = " + parameter.name() + ".length");
               }else if(isLong){
                   return List.of("this.template.insert(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                           "long resultJNoSQL = (long)" + parameter.name() + ".length");
               }
               return List.of("var insertResult = this.template.insert(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                              "var resultJNoSQL = java.util.stream.StreamSupport.stream(insertResult.spliterator(), false).toArray("+
                                      parameter.arrayType()+"::new)");
           }
            if(isVoid) {
                return Collections.singletonList( "this.template.insert(" + parameter.name() + ")");
            } else if(isInt){
                    return List.of("this.template.insert(" + parameter.name() + ")", "int resultJNoSQL = 1");
            } else if(isLong){
                return List.of("this.template.insert(" + parameter.name() + ")", "long resultJNoSQL = 1L");
            }
            return Collections.singletonList( "var resultJNoSQL = this.template.insert(" + parameter.name() + ")");
        }
    }, SAVE {
        @Override
        public List<String> apply(MethodMetadata methodMetadata) {
            Parameter parameter = getParameter(methodMetadata, "The save method must have only one parameter");
            var returnType = methodMetadata.getReturnType();
            boolean isVoid = isVoid(returnType);
            boolean isInt = isInt(returnType);
            boolean isLong = isLong(returnType);
            if(parameter.isGeneric()){
                if(isVoid) {
                    return Collections.singletonList( "this.saveAll(" + parameter.name() + ")");
                } else if(isInt){
                    return List.of("this.saveAll(" + parameter.name() + ")",
                            "int resultJNoSQL = (int)java.util.stream.StreamSupport.stream("+ parameter.name()+ ".spliterator(), false).count()");
                }else if(isLong){
                    return List.of("this.saveAll(" + parameter.name() + ")",
                            "long resultJNoSQL = java.util.stream.StreamSupport.stream("+ parameter.name()+ ".spliterator(), false).count()");
                }
                return Collections.singletonList( "var resultJNoSQL = this.saveAll(" + parameter.name() + ")");
            } else if(parameter.isArray()){
                if(isVoid) {

                    return Collections.singletonList("this.saveAll(java.util.Arrays.stream(" + parameter.name() + ").toList())");
                } else if(isInt){
                    return List.of("this.saveAll(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                            "int resultJNoSQL = " + parameter.name() + ".length");
                } else if(isLong){
                    return List.of("this.saveAll(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                            "long resultJNoSQL = (long)" + parameter.name() + ".length");
                }
                return List.of("var saveResult = this.saveAll(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                        "var resultJNoSQL = java.util.stream.StreamSupport.stream(saveResult.spliterator(), false).toArray("+
                                parameter.arrayType()+"::new)");
            }
            if(isVoid) {
                return Collections.singletonList( "this.template.insert(" + parameter.name() + ")");
            } else if(isInt){
                return List.of("this.template.insert(" + parameter.name() + ")", "int resultJNoSQL = 1");
            } else if(isLong){
                return List.of("this.template.insert(" + parameter.name() + ")", "long resultJNoSQL = 1");
            }
            return Collections.singletonList( "var resultJNoSQL = this.template.insert(" + parameter.name() + ")");
        }
    },
    UPDATE {
        @Override
        public List<String> apply(MethodMetadata methodMetadata) {
            Parameter parameter = getParameter(methodMetadata, "The insert method must have only one parameter");
            var returnType = methodMetadata.getReturnType();
            boolean isVoid = isVoid(returnType);
            boolean isInt = AnnotationOperationMethodBuilder.isInt(returnType);
            boolean isLong = isLong(returnType);
            if(parameter.isGeneric()){
                if(isVoid) {
                    return Collections.singletonList( "this.template.update(" + parameter.name() + ")");
                } else if(isInt){
                    return List.of("this.template.update(" + parameter.name() + ")",
                            "int resultJNoSQL = (int)java.util.stream.StreamSupport.stream("+ parameter.name()+ ".spliterator(), false).count()");
                } else if(isLong){
                    return List.of("this.template.update(" + parameter.name() + ")",
                            "long resultJNoSQL = java.util.stream.StreamSupport.stream("+ parameter.name()+ ".spliterator(), false).count()");
                }
                return Collections.singletonList( "var resultJNoSQL = " +
                        "java.util.stream.StreamSupport.stream(this.template.update(" + parameter.name() + ").spliterator(), false).toList()");
            } else if(parameter.isArray()){
                if(isVoid) {

                    return Collections.singletonList("this.template.update(java.util.Arrays.stream(" + parameter.name() + ").toList())");
                } else if(isInt){
                    return List.of("this.template.update(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                            "int resultJNoSQL = " + parameter.name() + ".length");
                } else if(isLong){
                    return List.of("this.template.update(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                            "long resultJNoSQL = (long)" + parameter.name() + ".length");
                }
                return List.of("var insertResult = this.template.update(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                        "var resultJNoSQL = java.util.stream.StreamSupport.stream(insertResult.spliterator(), false).toArray("+
                                parameter.arrayType()+"::new)");
            }
            if(isVoid) {
                return Collections.singletonList( "this.template.update(" + parameter.name() + ")");
            } else if(isInt){
                return List.of("this.template.update(" + parameter.name() + ")", "int resultJNoSQL = 1");
            }else if(isLong){
                return List.of("this.template.update(" + parameter.name() + ")", "long resultJNoSQL = 1L");
            }
            return Collections.singletonList( "var resultJNoSQL = this.template.update(" + parameter.name() + ")");
        }
    },

    DELETE {
        @Override
        public List<String> apply(MethodMetadata methodMetadata) {
            Parameter parameter = getParameter(methodMetadata, "The insert method must have only one parameter");
            var returnType = methodMetadata.getReturnType();
            boolean isVoid = isVoid(returnType);
            boolean isInt = isInt(returnType);
            boolean isLong = isLong(returnType);
            boolean isBoolean = isBoolean(returnType);
            if(parameter.isGeneric()){
                if(isVoid) {
                    return Collections.singletonList( "this.deleteAll(" + parameter.name() + ")");
                } else if(isInt){
                    return List.of("this.deleteAll(" + parameter.name() + ")",
                            "int resultJNoSQL = (int)java.util.stream.StreamSupport.stream("+ parameter.name()+ ".spliterator(), false).count()");
                } else if(isLong){
                    return List.of("this.deleteAll(" + parameter.name() + ")",
                            "long resultJNoSQL = java.util.stream.StreamSupport.stream("+ parameter.name()+ ".spliterator(), false).count()");
                } else if (isBoolean) {
                    return List.of("this.deleteAll(" + parameter.name() + ")",
                            "boolean resultJNoSQL = true");

                }
                return List.of( "this.deleteAll(" + parameter.name() + ")", "var resultJNoSQL = "+parameter.name());
            } else if(parameter.isArray()){
                if(isVoid) {
                    return Collections.singletonList("this.deleteAll(java.util.Arrays.stream(" + parameter.name() + ").toList())");
                } else if(isInt){
                    return List.of("this.deleteAll(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                            "int resultJNoSQL = " + parameter.name() + ".length");
                } else if(isLong){
                    return List.of("this.deleteAll(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                            "long resultJNoSQL = (long)" + parameter.name() + ".length");
                }  else if (isBoolean) {
                    return List.of("this.deleteAll(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                            "boolean resultJNoSQL = true");

                }
                return List.of("var insertResultJNoSQL = this.deleteAll(java.util.Arrays.stream(" + parameter.name() + ").toList())",
                        "var resultJNoSQL = java.util.stream.StreamSupport.stream(insertResultJNoSQL.spliterator(), false).toArray("+
                                parameter.arrayType()+"::new)");
            }
            if(isVoid) {
                return Collections.singletonList( "this.delete(" + parameter.name() + ")");
            } else if(isInt){
                return List.of("this.delete(" + parameter.name() + ")", "int resultJNoSQL = 1");
            } else if(isLong){
                return List.of("this.delete(" + parameter.name() + ")", "long resultJNoSQL = 1L");
            } else if(isBoolean){
                return List.of("this.delete(" + parameter.name() + ")", "boolean resultJNoSQL = true");
            }
            return List.of("this.delete(" + parameter.name() + ")", "var resultJNoSQL =" + parameter.name());
        }
    };

    private static Parameter getParameter(MethodMetadata methodMetadata, String s) {
        List<Parameter> parameters = methodMetadata.getParameters();
        if (parameters.size() != 1) {
            throw new IllegalStateException(s);
        }
        return parameters.getFirst();
    }

    private static boolean isInt(String returnType) {
        return returnType.equals(Integer.TYPE.getName())|| returnType.equals(Integer.class.getName());
    }

    private static boolean isLong(String returnType) {
        return returnType.equals(Long.TYPE.getName())|| returnType.equals(Long.class.getName());
    }

    private static boolean isBoolean(String returnType) {
        return returnType.equals(Boolean.TYPE.getName())|| returnType.equals(Boolean.class.getName());
    }

    private static boolean isVoid(String returnType) {
        return returnType.equals(Void.TYPE.getName());
    }

}
