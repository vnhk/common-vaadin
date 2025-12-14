package com.bervan.lowcode.generator;

import com.bervan.lowcode.LowCodeClass;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("production || it")
@Service
public class NotLocalLowCodeGenerator implements LowCodeGenerator {
    @Override
    public void generate(LowCodeClass lowCodeClass) {
        throw new UnsupportedOperationException("This operation is not supported in this environment");
    }
}
