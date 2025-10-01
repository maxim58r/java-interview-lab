package ru.max.test.junittest.extensiontest;

import org.junit.jupiter.api.extension.*;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final String KEY = "startedAt";

    @Override
    public void beforeTestExecution(ExtensionContext ctx) {
        ExtensionContext.Store store = ctx.getStore(ExtensionContext.Namespace.create(getClass(), ctx.getUniqueId()));
        store.put(KEY, System.nanoTime());
    }

    @Override
    public void afterTestExecution(ExtensionContext ctx) {
        ExtensionContext.Store store = ctx.getStore(ExtensionContext.Namespace.create(getClass(), ctx.getUniqueId()));
        long start = store.remove(KEY, long.class);
        long tookMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("[TIMING] " + ctx.getDisplayName() + " took " + tookMs + "ms");
    }
}
