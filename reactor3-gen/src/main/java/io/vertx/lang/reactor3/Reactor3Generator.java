package io.vertx.lang.reactor3;

import io.vertx.codegen.*;
import io.vertx.codegen.type.*;
import io.vertx.lang.rx.AbstractRxGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class Reactor3Generator extends AbstractRxGenerator {
    Reactor3Generator() {
        super("reactor3");
        this.kinds = Collections.singleton("class");
        this.name = "Reactor3";
    }

    @Override
    protected void genImports(ClassModel model, PrintWriter writer) {
        writer.println("import io.vertx.reactor3.core.FluxHelper;");
        writer.println("import io.vertx.reactor3.core.impl.AsyncResultMono;");
        super.genImports(model, writer);
    }

    @Override
    protected void genToObservable(TypeInfo streamType, PrintWriter writer) {
        writer.print("  private ");
        writer.print(Flux.class.getName());
        writer.print("<");
        writer.print(genTranslatedTypeName(streamType));
        writer.println("> flux;");

        writer.println();

        genToFlux(streamType, writer);
    }

    private void genToFlux(TypeInfo streamType, PrintWriter writer) {
        String rxType = Flux.class.getName();
        String rxName = "flux";
        writer.print("  public synchronized ");
        writer.print(rxType);
        writer.print("<");
        writer.print(genTranslatedTypeName(streamType));
        writer.println("> toFlux() {");

        writer.print("    ");
        writer.print("if (");
        writer.print(rxName);
        writer.println(" == null) {");

        if (streamType.getKind() == ClassKind.API) {
            writer.print("      Function<");
            writer.print(streamType.getName());
            writer.print(", ");
            writer.print(genTranslatedTypeName(streamType));
            writer.print("> conv = ");
            writer.print(genTranslatedTypeName(streamType.getRaw()));
            writer.println("::newInstance;");

            writer.print("      ");
            writer.print(rxName);
            writer.print(" = ");
            writer.print("io.vertx.reactor3.core.impl.FluxReadStream.fromStream(delegate)");
            writer.println(".map(conv);");
        } else if (streamType.isVariable()) {
            String typeVar = streamType.getSimpleName();
            writer.print("      Function<");
            writer.print(typeVar);
            writer.print(", ");
            writer.print(typeVar);
            writer.print("> conv = (Function<");
            writer.print(typeVar);
            writer.print(", ");
            writer.print(typeVar);
            writer.println(">) __typeArg_0.wrap;");

            writer.print("      ");
            writer.print(rxName);
            writer.print(" = ");
            writer.print("io.vertx.reactor3.core.impl.FluxReadStream.fromStream(delegate)");
            writer.println(".map(conv);");
        } else {
            writer.print("      ");
            writer.print(rxName);
            writer.print(" = ");
            writer.println("io.vertx.reactor3.core.impl.FluxReadStream.fromStream(delegate);");
        }

        writer.println("    }");
        writer.print("    return ");
        writer.print(rxName);
        writer.println(";");
        writer.println("  }");
        writer.println();
    }

    @Override
    protected void genToSubscriber(TypeInfo streamType, PrintWriter writer) {
//        writer.format("  private WriteStreamSubscriber<%s> subscriber;%n", genTranslatedTypeName(streamType));
//        writer.println();
//        genToXXXEr(streamType, "Subscriber", "subscriber", writer);

        writer.format("  public reactor.core.publisher.Flux<%1$s> write(org.reactivestreams.Publisher<? extends %1$s> publisher, int concurrency) {%n", genTranslatedTypeName(streamType));
        if (streamType.getKind() == ClassKind.API) {
            writer.format("    Function<%s, %s> conv = %s::getDelegate;%n", genTranslatedTypeName(streamType.getRaw()), streamType.getName(), genTranslatedTypeName(streamType));
            writer.println("    return FluxHelper.write(this, publisher, conv, concurrency);");
        } else if (streamType.isVariable()) {
            String typeVar = streamType.getSimpleName();
            writer.format("    Function<%s, %s> conv = (Function<%s, %s>) __typeArg_0.unwrap;%n", typeVar, typeVar, typeVar, typeVar);
            writer.println("    return FluxHelper.write(this, publisher, conv, concurrency);");
        } else {
            writer.println("    return FluxHelper.write(this, publisher, concurrency);");
        }
        writer.println("  }");
        writer.println();
    }

    private void genToXXXEr(TypeInfo streamType, String rxType, String rxName, PrintWriter writer) {
        writer.format("  public synchronized WriteStream%s<%s> to%s() {%n", rxType, genTranslatedTypeName(streamType), rxType);
        writer.format("    if (%s == null) {%n", rxName);
        if (streamType.getKind() == ClassKind.API) {
            writer.format("      Function<%s, %s> conv = %s::getDelegate;%n", genTranslatedTypeName(streamType.getRaw()), streamType.getName(), genTranslatedTypeName(streamType));
            writer.format("      %s = RxHelper.to%s(getDelegate(), conv);%n", rxName, rxType);
        } else if (streamType.isVariable()) {
            String typeVar = streamType.getSimpleName();
            writer.format("      Function<%s, %s> conv = (Function<%s, %s>) __typeArg_0.unwrap;%n", typeVar, typeVar, typeVar, typeVar);
            writer.format("      %s = RxHelper.to%s(getDelegate(), conv);%n", rxName, rxType);
        } else {
            writer.format("      %s = RxHelper.to%s(getDelegate());%n", rxName, rxType);
        }
        writer.println("    }");
        writer.format("    return %s;%n", rxName);
        writer.println("  }");
        writer.println();
    }

    @Override
    protected void genMethods(ClassModel model, MethodInfo method, List<String> cacheDecls, boolean genBody, PrintWriter writer) {
        if (method.getKind() == MethodKind.CALLBACK || method.getKind() == MethodKind.FUTURE) {
            genRxMethod(model, method, genBody, writer);
            genLazyRxMethod(model, method, genBody, writer);
        } else {
            genSimpleMethod("public", model, method, cacheDecls, genBody, writer);
        }
    }

    private void genRxMethod(ClassModel model, MethodInfo method, boolean genBody, PrintWriter writer) {
        MethodInfo futMethod = genFutureMethod(method);
        startMethodTemplate("public", model.getType(), futMethod, "", writer);
        if (genBody) {
            String rxName = genFutureMethodName(method);
            writer.println(" { ");
            writer.print("    ");
            writer.print(genReturnTypeDecl(futMethod.getReturnType()));
            writer.print(" ret = ");
            writer.print(rxName);
            writer.print("(");
            List<ParamInfo> params = futMethod.getParams();
            writer.print(params.stream().map(ParamInfo::getName).collect(Collectors.joining(", ")));
            writer.println(");");
            writer.println("    ret = ret.cache();");
            writer.print("    ret.subscribe(io.vertx.reactor3.core.");
            writer.print(futMethod.getReturnType().getRaw().getSimpleName());
            writer.println("Helper.nullObserver());");
            writer.println("    return ret;");
            writer.println("  }");
        } else {
            writer.println(";");
        }
        writer.println();
    }

    private void genLazyRxMethod(ClassModel model, MethodInfo method, boolean genBody, PrintWriter writer) {
        MethodInfo futMethod = genFutureMethod(method);
        futMethod.setName(genFutureMethodName(futMethod));
        ClassTypeInfo raw = futMethod.getReturnType().getRaw();
        String methodSimpleName = raw.getSimpleName();
        String adapterType = "AsyncResult" + methodSimpleName + ".to" + methodSimpleName;
        startMethodTemplate("public", model.getType(), futMethod, "", writer);
        if (genBody) {
            writer.println(" { ");
            if (method.getKind() == MethodKind.FUTURE) {
                writer.print("    return ");
                writer.print(adapterType);
                writer.print("(");
                writer.print(genInvokeDelegate(model, method));
                if (!futMethod.getReturnType().getSimpleName().equals("Completable")) {
                    writer.print(", __value -> ");
                    TypeInfo asyncType = ((ParameterizedTypeInfo) method.getReturnType()).getArg(0);
                    writer.print(genConvReturn(asyncType, method, "__value"));
                }
                writer.println(");");
            } else {
                writer.print("    return ");
                writer.print(adapterType);
                writer.print("( ");
                writer.print(method.getParam(futMethod.getParams().size()).getName());
                writer.println(" -> {");
                writer.print("      ");
                writer.print(genInvokeDelegate(model, method));
                writer.println(";");
                writer.println("    });");
            }
            writer.println("  }");
        } else {
            writer.println(";");
        }
        writer.println();
    }

    @Override
    protected void genReadStream(List<? extends TypeParamInfo> typeParams, PrintWriter writer) {
        writer.print("  ");
        writer.print(Flux.class.getName());
        writer.print("<");
        writer.print(typeParams.get(0).getName());
        writer.println("> toFlux();");
        writer.println();
    }

    @Override
    protected void genWriteStream(List<? extends TypeParamInfo> typeParams, PrintWriter writer) {
        String typeArg = typeParams.get(0).getName();
        writer.print("  reactor.core.publisher.Flux<");
        writer.print(typeArg);
        writer.print("> write(org.reactivestreams.Publisher<? extends ");
        writer.print(typeArg);
        writer.println("> publisher, int concurrency);");
        writer.println();
    }

    private TypeInfo rewriteParamType(TypeInfo type) {
        if (type.isParameterized()) {
            if (type.getRaw().getName().equals("io.vertx.core.streams.ReadStream")) {
                return new io.vertx.codegen.type.ParameterizedTypeInfo(
                    io.vertx.codegen.type.TypeReflectionFactory.create(Flux.class).getRaw(),
                    false,
                    java.util.Collections.singletonList(((ParameterizedTypeInfo) type).getArg(0))
                );
            } else if (type.getKind() == ClassKind.FUTURE) {
                TypeInfo futType = ((ParameterizedTypeInfo) type).getArg(0);
                return rewriteFutType(futType);
            } else if (type.getKind() == ClassKind.FUNCTION) {
                ParameterizedTypeInfo functionType = (ParameterizedTypeInfo) type;
                TypeInfo argType = functionType.getArg(0); // Return not param
                TypeInfo retType = rewriteParamType(functionType.getArg(1));
                if (argType != functionType.getArg(0) || retType != functionType.getArg(1)) {
                    return new ParameterizedTypeInfo(
                        functionType.getRaw(),
                        functionType.isNullable(),
                        Arrays.asList(argType, retType));
                }
            }
        }
        return type;
    }

    @Override
    protected String genParamTypeDecl(TypeInfo type) {
        return super.genParamTypeDecl(rewriteParamType(type));
    }

    @Override
    protected String genConvParam(TypeInfo type, MethodInfo method, String expr) {
//        isSameType() is private in superclass
//        if (isSameType(type, method)) {
//            return expr;
//        }
        if (type.isParameterized()) {
            if (type.getRaw().getName().equals("io.vertx.core.streams.ReadStream")) {
                ParameterizedTypeInfo parameterizedType = (ParameterizedTypeInfo) type;
                String adapterFunction = "obj -> " + genConvParam(parameterizedType.getArg(0), method, "obj");
                return "io.vertx.reactor3.core.impl.ReadStreamSubscriber.asReadStream(" + expr + ", " + adapterFunction + ").resume()";
            }
            if (type.getKind() == ClassKind.FUTURE) {
                TypeInfo futType = ((ParameterizedTypeInfo) type).getArg(0);
                if (futType.getKind() == ClassKind.VOID) {
                    return "io.vertx.reactor3.core.MonoHelper.toFuture(" + expr + ")";
                } else {
                    ParameterizedTypeInfo parameterizedType = (ParameterizedTypeInfo) type;
                    String adapterFunction = "obj -> " + genConvParam(parameterizedType.getArg(0), method, "obj");
                    return "io.vertx.reactor3.core.MonoHelper.toFuture(" + expr + ", " + adapterFunction + ")";
                }
            }
        }
        return super.genConvParam(type, method, expr);
    }

    private MethodInfo genFutureMethod(MethodInfo method) {
        List<ParamInfo> futParams;
        TypeInfo futType;
        if (method.getKind() == MethodKind.FUTURE) {
            futParams = new ArrayList<>(method.getParams());
            futType = ((ParameterizedTypeInfo) method.getReturnType()).getArg(0);
        } else {
            futParams = new ArrayList<>();
            int count = 0;
            int size = method.getParams().size() - 1;
            while (count < size) {
                ParamInfo param = method.getParam(count);
                /* Transform ReadStream -> Flowable */
                futParams.add(param);
                count = count + 1;
            }
            ParamInfo futParam = method.getParam(size);
            futType = ((ParameterizedTypeInfo) ((ParameterizedTypeInfo) futParam.getType()).getArg(0)).getArg(0);
        }
        TypeInfo futReturnType = rewriteFutType(futType);
        return method.copy().setReturnType(futReturnType).setParams(futParams);
    }

    private TypeInfo rewriteFutType(TypeInfo futType) {
        return new io.vertx.codegen.type.ParameterizedTypeInfo(
            io.vertx.codegen.type.TypeReflectionFactory.create(Mono.class).getRaw(),
            false, // XXX futType.isNullable() ?
            Collections.singletonList(futType)
        );
    }
}
