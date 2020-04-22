/*
 * Copyright 2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.starter.cli.command.project.test;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.util.functional.ThrowingSupplier;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.cli.CodeGenConfig;
import io.micronaut.starter.cli.command.CodeGenCommand;
import io.micronaut.starter.io.ConsoleOutput;
import io.micronaut.starter.io.OutputHandler;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import io.micronaut.starter.template.RenderResult;
import io.micronaut.starter.template.RockerTemplate;
import io.micronaut.starter.template.TemplateRenderer;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "create-test", description = "Creates a simple test for the project's testing framework")
@Prototype
public class CreateTestCommand extends CodeGenCommand {

    @CommandLine.Parameters(paramLabel = "TEST-NAME", description = "The name of the test class to create")
    String testName;

    public CreateTestCommand(@Parameter CodeGenConfig config) {
        super(config);
    }

    public CreateTestCommand(CodeGenConfig config, ThrowingSupplier<OutputHandler, IOException> outputHandlerSupplier, ConsoleOutput consoleOutput) {
        super(config, outputHandlerSupplier, consoleOutput);
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    public Integer call() throws Exception {
        Project project = getProject(testName);

        TemplateRenderer templateRenderer = getTemplateRenderer(project);

        RenderResult renderResult = null;
        if (config.getTestFramework() == TestFramework.JUNIT) {
            if (config.getSourceLanguage() == Language.JAVA) {
                renderResult = templateRenderer.render(new RockerTemplate("src/test/java/{packagePath}/{className}Test.java", javaJunit.template(project)), overwrite);
            } else if (config.getSourceLanguage() == Language.GROOVY) {
                renderResult = templateRenderer.render(new RockerTemplate("src/test/groovy/{packagePath}/{className}Test.groovy", groovyJunit.template(project)), overwrite);
            } else if (config.getSourceLanguage() == Language.KOTLIN) {
                renderResult = templateRenderer.render(new RockerTemplate("src/test/kotlin/{packagePath}/{className}Test.kt", kotlinJunit.template(project)), overwrite);
            }
        } else if (config.getTestFramework() == TestFramework.SPOCK) {
            renderResult = templateRenderer.render(new RockerTemplate("src/test/groovy/{packagePath}/{className}Spec.groovy", spock.template(project)), overwrite);
        } else if (config.getTestFramework() == TestFramework.KOTLINTEST) {
            renderResult = templateRenderer.render(new RockerTemplate("src/test/kotlin/{packagePath}/{className}Test.kt", kotlinTest.template(project)), overwrite);
        }

        if (renderResult != null) {
            if (renderResult.isSuccess()) {
                out("@|blue ||@ Rendered test to " + renderResult.getPath());
            } else if (renderResult.isSkipped()) {
                warning("Rendering skipped for " + renderResult.getPath() + " because it already exists. Run again with -f to overwrite.");
            } else if (renderResult.getError() != null) {
                throw renderResult.getError();
            }
        }

        return 0;
    }
}
