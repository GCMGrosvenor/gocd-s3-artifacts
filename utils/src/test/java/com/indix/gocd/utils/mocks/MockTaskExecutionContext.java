package com.indix.gocd.utils.mocks;

import com.thoughtworks.go.plugin.api.task.Console;
import com.thoughtworks.go.plugin.api.task.EnvironmentVariables;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

public class MockTaskExecutionContext implements TaskExecutionContext {
    private Map<String, String> mockEnvironmentVariables;
    private String workingDir = ".";

    public MockTaskExecutionContext(Map<String, String> mockEnvironmentVariables) {
        this.mockEnvironmentVariables = mockEnvironmentVariables;
    }

    public MockTaskExecutionContext(Map<String, String> mockEnvironmentVariables, String workingDir) {
        this.mockEnvironmentVariables = mockEnvironmentVariables;
        this.workingDir = workingDir;
    }

    @Override
    public EnvironmentVariables environment() {
        return new EnvironmentVariables() {
            @Override
            public Map<String, String> asMap() {
                return mockEnvironmentVariables;
            }

            @Override
            public void writeTo(Console console) {
            }

            @Override
            public Console.SecureEnvVarSpecifier secureEnvSpecifier() {
                return new Console.SecureEnvVarSpecifier() {
                    @Override
                    public boolean isSecure(String s) {
                        return false;
                    }
                };
            }
        };
    }

    @Override
    public Console console() {
        return new Console() {
            @Override
            public void printLine(String s) {
            }

            @Override
            public void readErrorOf(InputStream inputStream) {
            }

            @Override
            public void readOutputOf(InputStream inputStream) {
            }

            @Override
            public void printEnvironment(Map<String, String> stringStringMap, SecureEnvVarSpecifier secureEnvVarSpecifier) {
            }
        };
    }

    @Override
    public String workingDir() {
        return this.workingDir;
    }
}
