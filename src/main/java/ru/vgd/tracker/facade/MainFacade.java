package ru.vgd.tracker.facade;

import java.util.UUID;

public interface MainFacade {
    MainPageDto getMainPageData(UUID userId);
}
