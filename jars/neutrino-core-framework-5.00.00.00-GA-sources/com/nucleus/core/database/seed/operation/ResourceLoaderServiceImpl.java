package com.nucleus.core.database.seed.operation;

import com.nucleus.core.database.seed.viewobject.CsvDataVO;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.service.BaseServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.inject.Named;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Named("resourceLoaderService")
public class ResourceLoaderServiceImpl extends BaseServiceImpl implements ResourceLoaderService {


    @Value("${seed.folder.location}")
    private String seedLocation;

    @Value("${seed.meta.csv.location}")
    private String seedMetaLocation;

    @Value("${database.app.schemaname}")
    private String schemaName;

    public List<Resource> getResourceList() {
        List<Resource> resourceList = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<String> seedLocationList = readCsvFormLocation(resolver);
        if(CollectionUtils.isNotEmpty(seedLocationList)) {
            seedLocationList.forEach(seedLocation -> resourceList.add(resolver.getResource(seedLocation)));
        }
        return resourceList;
    }

    public List<String> getActiveSeededTablesList() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource resource = resolver.getResource(seedMetaLocation);
            List<String> csvDataVOList = prepareSeededTableList(resource);
            return csvDataVOList;
        } catch (IOException e) {
            throw new SystemException("Exception occurred while reading csv", e);
        }
    }

    public List<String> readCsvFormLocation(PathMatchingResourcePatternResolver resolver){
        try {
            Resource resource = resolver.getResource(seedMetaLocation);
            String normalizedPath = String.format("%s%s%s","file:", Paths.get(seedLocation).toString(), File.separator);
            List<CsvDataVO> csvDataVOList = prepareCsvDataVOList(resource, normalizedPath);
            if(validateCsv(csvDataVOList)){
                return csvDataVOList
                    .stream()
                    .map(CsvDataVO::getSeedLocation)
                    .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new SystemException("Exception occurred while reading csv", e);
        }
        return null;
    }

    private boolean validateCsv(List<CsvDataVO> csvDataVOList) {
        String duplicateSeedLocation = validateSheetName(csvDataVOList);
        Integer duplicateSeedSequence = validateSheetSequence(csvDataVOList);
        if(duplicateSeedLocation != null){
            throw new SystemException(String.format("%s%s","Duplicate Seed location not allowed but found duplicate entries for ",duplicateSeedLocation));
        }
        if(duplicateSeedSequence != null) {
            throw new SystemException(String.format("%s%d","Duplicate Seed Sequence not allowed but found duplicate entries for ", duplicateSeedSequence));
        }
        return true;
    }

    public String validateSheetName(List<CsvDataVO> csvDataVOList){
        Set<String> seedNameSet = new HashSet<>();
        return csvDataVOList
                .stream()
                .filter(element -> !seedNameSet.add(element.getSeedLocation()))
                .map(CsvDataVO::getSeedLocation)
                .findFirst()
                .orElse(null);
    }

    public Integer validateSheetSequence(List<CsvDataVO> csvDataVOList){
        Set<Integer> seedSequenceSet = new HashSet<>();
        return csvDataVOList
                .stream()
                .filter(element -> !seedSequenceSet.add(element.getSeedSequence()))
                .map(CsvDataVO::getSeedSequence)
                .findFirst()
                .orElse(null);
    }

    private List<CsvDataVO> prepareCsvDataVOList(Resource resource, String normalizedPath) throws IOException {
        CsvDataVO.SCHEMA_NAME = (StringUtils.isNotEmpty(schemaName) ? schemaName : null);
        if(CsvDataVO.SCHEMA_NAME == null) {
            throw new SystemException("Schema Name is mandatory##");
        }
        return new BufferedReader(new InputStreamReader(resource.getInputStream()))
            .lines()
            .skip(1)
            .map(s -> new CsvDataVO(s, normalizedPath))
            .filter(CsvDataVO::isSeedActive)
            .sorted(Comparator.comparing(CsvDataVO::getSeedSequence))
            .collect(Collectors.toList());
    }

    private List<String> prepareSeededTableList(Resource resource) throws IOException {
        return new BufferedReader(new InputStreamReader(resource.getInputStream()))
                .lines()
                .skip(1)
                .filter(s -> {
                    String[] splitVal = s.split(",",-1);
                    return Boolean.parseBoolean(splitVal[2]) ==  true;
                })
                .map(s -> {
                    String newStrVal = "";
                    String[] splitVal = s.split(",",-1);
                    newStrVal = splitVal[0];
                    return newStrVal;
                }).collect(Collectors.toList());
    }

}
