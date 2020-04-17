package com.nucleus.core;


import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class CustomSizeLinkedHashMap<K,V> extends LinkedHashMap<K, V> {
       private final int maxSize;

           public CustomSizeLinkedHashMap(int maxSize) {
               this.maxSize = maxSize;
           }

           @Override
           protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
               return size() > maxSize;
           }
}

