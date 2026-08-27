package com.thealtening;

public class AltService {

    public enum EnumAltService {
        MOJANG,
        THEALTENING
    }

    private EnumAltService currentService = EnumAltService.MOJANG;

    public EnumAltService getCurrentService() {
        return currentService;
    }

    public void switchService(EnumAltService service) throws NoSuchFieldException, IllegalAccessException {
        this.currentService = service;
    }
}
