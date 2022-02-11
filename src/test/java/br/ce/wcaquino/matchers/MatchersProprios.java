package br.ce.wcaquino.matchers;

import java.util.Calendar;

public class MatchersProprios {

    public static DiaSemanaMatcher caiEm(Integer diaDaSemana) {
        return new DiaSemanaMatcher(diaDaSemana);
    }

    public static DiaSemanaMatcher caiEmUmaSegunda() {
        return new DiaSemanaMatcher(Calendar.MONDAY);
    }

    public static DataDiferencaDiasMatcher ehHojeDataDiferencaDias(Integer qtdDias) {
        return new DataDiferencaDiasMatcher(qtdDias);
    }

    public static DataDiferencaDiasMatcher ehHoje() {
        return new DataDiferencaDiasMatcher(0);
    }
}
