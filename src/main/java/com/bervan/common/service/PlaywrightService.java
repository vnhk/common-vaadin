package com.bervan.common.service;

import ch.qos.logback.core.testUtil.RandomUtil;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class PlaywrightService {
    @Value("#{'${playwright.user-agents.list}'.split(',,,,')}")
    private List<String> userAgents;

    public Page getPage(Playwright playwright, boolean headless) {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(Arrays.asList("--disable-blink-features=AutomationControlled",
                        "--disable-web-security",
                        "--disable-site-isolation-trials",
                        "--disable-features=IsolateOrigins,site-per-process"));

        String userAgent = userAgents.get(RandomUtil.getPositiveInt() % userAgents.size());

        BrowserContext context = playwright.chromium().launchPersistentContext(
                Paths.get("profile-stealth"),
                new BrowserType.LaunchPersistentContextOptions()
                        .setHeadless(headless)
                        .setArgs(launchOptions.args)
                        .setUserAgent(userAgent.trim())
                        .setLocale("en-US")
                        .setTimezoneId("Europe/Warsaw")
                        .setViewportSize(1280, 800)
        );

        Page page = context.newPage();

        page.addInitScript("""
                    Object.defineProperty(navigator, 'webdriver', {
                        get: () => undefined
                    });
                
                    Object.defineProperty(navigator, 'plugins', {
                        get: () => [1, 2, 3]
                    });
                
                    Object.defineProperty(navigator, 'languages', {
                        get: () => ['pl-PL', 'pl']
                    });
                """);
        return page;
    }

}
