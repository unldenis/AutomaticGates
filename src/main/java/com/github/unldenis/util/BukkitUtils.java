package com.github.unldenis.util;


import lombok.*;
import org.bukkit.inventory.*;

import java.util.*;


public class BukkitUtils {

    private static final Comparator<ItemStack> comparator = Comparator.comparing($ -> $.getType().toString());

    public static boolean compareArrays(@NonNull ItemStack[] arr1, @NonNull ItemStack[] arr2) {
        if (arr1.length != arr2.length) {
            return false;
        }
        Arrays.sort(arr1, comparator);
        Arrays.sort(arr2, comparator);
        return Arrays.equals(arr1, arr2);
    }
}
