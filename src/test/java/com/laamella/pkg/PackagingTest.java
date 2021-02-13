package com.laamella.pkg;

import com.laamella.sout.SoutTemplate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

class PackagingTest {
    @Test
    void makeSureEverythingIsScopedRight() throws IOException {
        var template = SoutTemplate.parse(new StringReader(""), '{', '|', '}', '\\');
        template.render("", new StringWriter());
    }
}
