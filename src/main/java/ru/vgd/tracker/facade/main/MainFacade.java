package ru.vgd.tracker.facade.main;

import java.util.UUID;

public interface MainFacade {
    MainPageResponse getMainPageData(UUID userId);
}
