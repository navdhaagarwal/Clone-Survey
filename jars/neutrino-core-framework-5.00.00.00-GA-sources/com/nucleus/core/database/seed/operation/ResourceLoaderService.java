package com.nucleus.core.database.seed.operation;

import org.springframework.core.io.Resource;

import java.util.List;

public interface ResourceLoaderService{

    public List<Resource> getResourceList();
    public List<String> getActiveSeededTablesList();
}
