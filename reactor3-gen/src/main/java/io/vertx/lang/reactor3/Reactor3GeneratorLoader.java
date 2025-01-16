package io.vertx.lang.reactor3;

import io.vertx.codegen.Generator;
import io.vertx.codegen.GeneratorLoader;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.stream.Stream;

public class Reactor3GeneratorLoader implements GeneratorLoader {
    @Override
    public Stream<Generator<?>> loadGenerators(ProcessingEnvironment processingEnv) {
        return Stream.of(new Reactor3Generator());
    }
}
