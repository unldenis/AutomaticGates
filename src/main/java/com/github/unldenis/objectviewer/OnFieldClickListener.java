package com.github.unldenis.objectviewer;

import org.bukkit.event.inventory.ClickType;

import java.lang.reflect.Field;

public interface OnFieldClickListener {

    void onFieldClick(Field field, Object value, ClickType clickType);
}