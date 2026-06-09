package com.kelompok1.service;

import com.kelompok1.dao.SettingsDAO;

public class SettingsService {
    private final SettingsDAO settingsDAO;

    public SettingsService() {
        this.settingsDAO = new SettingsDAO();
    }

    public String getSetting(String key, String defaultValue) {
        return settingsDAO.getSetting(key, defaultValue);
    }

    public boolean setSetting(String key, String value) {
        return settingsDAO.setSetting(key, value);
    }
}
