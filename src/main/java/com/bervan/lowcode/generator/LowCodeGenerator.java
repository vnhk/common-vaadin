package com.bervan.lowcode.generator;

import com.bervan.lowcode.LowCodeClass;

import java.io.IOException;

public interface LowCodeGenerator {
    void generate(LowCodeClass lowCodeClass) throws IOException;
}
