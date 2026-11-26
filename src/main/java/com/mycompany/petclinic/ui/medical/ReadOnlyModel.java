package com.mycompany.petclinic.ui.medical;

import javax.swing.table.DefaultTableModel;

public class ReadOnlyModel extends DefaultTableModel {

    private final Class<?>[] types;

    public ReadOnlyModel(String[] columns, Class<?>... types) {
        super(columns, 0);
        this.types = types;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return column < types.length ? types[column] : Object.class;
    }
}