package dev.robocode.tankroyale.botapi.internal;

import org.junit.jupiter.api.Test;

import static dev.robocode.tankroyale.botapi.internal.JsonUtil.escaped;
import static org.assertj.core.api.Assertions.assertThat;

class JsonUtilTest {

    @Test
    void testEscapedBackslash() {
        assertThat(escaped("\b")).isEqualTo("\\b");
    }

    @Test
    void testEscapedFormFeed() {
        assertThat(escaped("\f")).isEqualTo("\\f");
    }

    @Test
    void testEscapedNewline() {
        assertThat(escaped("\n")).isEqualTo("\\n");
    }

    @Test
    void testEscapedCarriageReturn() {
        assertThat(escaped("\r")).isEqualTo("\\r");
    }

    @Test
    void testEscapedTab() {
        assertThat(escaped("\t")).isEqualTo("\\t");
    }

    @Test
    void testEscapedDoubleQuotes() {
        assertThat(escaped("\"")).isEqualTo("\\\"");
    }

    @Test
    void testEscapedDoubleBackslashes() {
        assertThat(escaped("\\\\")).isEqualTo("\\\\");
    }
}
