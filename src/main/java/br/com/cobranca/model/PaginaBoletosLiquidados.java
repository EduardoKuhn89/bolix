package br.com.cobranca.model;

import java.util.List;

/**
 * Resultado paginado da consulta de liquidados por dia (campo "hasNext" do
 * Sicredi).
 */
public final class PaginaBoletosLiquidados {

    private final List<BoletoLiquidado> items;
    private final boolean hasNext;

    public PaginaBoletosLiquidados(List<BoletoLiquidado> items, boolean hasNext) {
        this.items = items;
        this.hasNext = hasNext;
    }

    public List<BoletoLiquidado> getItems() {
        return items;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
