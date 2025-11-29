package com.example.mcp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationJvmTest {
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final java.io.InputStream originalIn = System.in;

    private ByteArrayOutputStream outBuf;
    private ByteArrayOutputStream errBuf;

    @BeforeEach
    void setUp() {
        outBuf = new ByteArrayOutputStream();
        errBuf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBuf));
        System.setErr(new PrintStream(errBuf));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);
    }

    @Test
    void statefulFlowRunsInSameProcess() throws Exception {
        String requests = String.join("\n",
            "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"create_note\",\"arguments\":{\"title\":\"JUnit\",\"content\":\"junit note\"}}}",
            "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\",\"params\":{\"name\":\"list_notes\",\"arguments\":{}}}",
            "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"calculate\",\"arguments\":{\"operation\":\"add\",\"a\":7,\"b\":8}}}"
        ) + "\n";

        System.setIn(new ByteArrayInputStream(requests.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        // Call main; it should read until EOF and then return
        Main.main(new String[0]);

        String stdout = outBuf.toString("UTF-8");
        String stderr = errBuf.toString("UTF-8");

        assertTrue(stdout.contains("Note created successfully"), "Expected note creation in stdout\n" + stdout + "\nERR:\n" + stderr);
        assertTrue(stdout.contains("JUnit"), "Expected created note title in list_notes output\n" + stdout);
        assertTrue(stdout.contains("Result"), "Expected calculate result in stdout\n" + stdout);
    }
}
