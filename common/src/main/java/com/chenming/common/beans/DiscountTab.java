package com.chenming.common.beans;

import com.flyco.tablayout.listener.CustomTabEntity;

public class DiscountTab implements CustomTabEntity {

    public String title;
    public int selectedIcon;
    public int unSelectedIcon;

    public DiscountTab(String title, int selectedIcon, int unSelectedIcon) {
        this.title = title;
        this.selectedIcon = selectedIcon;
        this.unSelectedIcon = unSelectedIcon;
    }

    @Override
    public String getTabTitle() {
        return title;
    }

    @Override
    public int getTabSelectedIcon() {
        return selectedIcon;
    }

    @Override
    public int getTabUnselectedIcon() {
        return unSelectedIcon;
    }
}
