package com.crms.licence;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LicenceStartupListener {

    private final LicenceService licenceService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        licenceService.logStartupSummary();
    }
}
