/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.dao.query;

import java.util.List;

import javax.persistence.EntityManager;

import com.nucleus.core.exceptions.InvalidDataException;

/**
 * @author Nucleus Software Exports Limited
 */
public class DatabaseResultContainer<T> {

    private QueryExecutor<T> executor;

    private Integer          startIndex           = null;
    private Integer          pageSize             = null;
    private Integer          currentStartPosition = null;
    private Integer          currentEndPosition   = null;
    private Long             totalSize            = null;
    // Single boolean field to denote if container is paginated so that we do not have to check several fields
    private boolean          paginated;
    // Single variable to store if first page has been fetched and all calculation variables are initialized for further
    // correct calculations
    private boolean          initialized;

    public DatabaseResultContainer(QueryExecutor<T> executor, Integer startIndex, Integer pageSize) {
        this.executor = executor;
        if (startIndex != null && pageSize != null) {
            this.startIndex = startIndex;
            this.pageSize = pageSize;
            this.paginated = true;
        }

    }

    public List<T> getCurrentPage(EntityManager em) {
        initializeTotalSize(em);
        if (!paginated) {
            return executor.executeQuery(em, startIndex, pageSizeIfNotInit());
        } else {
            if (!initialized) {
                List<T> result = executor.executeQuery(em, startIndex, pageSizeIfNotInit());
                initializeWithResult(result.size());
                return result;
            } else {
                int tempPageSize;
                /*
                 * Determines and updates the current start, end indexes and the current page size 
                 * if the user delete records
                 */
                if (currentEndPosition > totalSize) {
                    currentEndPosition = totalSize.intValue();
                    if (totalSize % pageSize == 0) {
                        tempPageSize = pageSize;
                        currentStartPosition = currentEndPosition - tempPageSize;
                    } else {
                        tempPageSize = (int) (totalSize % pageSize);
                        currentStartPosition = currentEndPosition - tempPageSize;
                    }
                } else {
                    tempPageSize = pageSizeIfInit();
                }
                List<T> result = executor.executeQuery(em, currentStartPosition, tempPageSize);
                currentEndPosition = currentStartPosition + result.size();
                return result;
            }
        }
    }

    public List<T> getFirstPage(EntityManager em) {
        initializeTotalSize(em);
        if (!paginated) {
            return executor.executeQuery(em, startIndex, pageSizeIfNotInit());
        } else {
            if (!initialized) {
                List<T> result = executor.executeQuery(em, startIndex, pageSizeIfNotInit());
                initializeWithResult(result.size());
                return result;
            } else {
                currentStartPosition = startIndex;
                List<T> result = executor.executeQuery(em, currentStartPosition, pageSizeIfInit());
                currentEndPosition = currentStartPosition + result.size();
                return result;
            }
        }
    }

    public List<T> getLastPage(EntityManager em) {
        initializeTotalSize(em);
        if (!paginated) {
            return executor.executeQuery(em, startIndex, pageSizeIfNotInit());
        } else {
            if (!initialized) {
                List<T> result = executor.executeQuery(em, startIndex, pageSizeIfNotInit());
                initializeWithResult(result.size());
                return result;
            } else {
                /*
                 * Determines and updates the current start, end indexes and the current page size 
                 * if the user starts to fetch results from index other than zero.
                 */
                int tempPageSize;
                int tempTotalSize = (int) (totalSize - startIndex);
                if ((tempTotalSize % pageSize) != 0) {
                    tempPageSize = tempTotalSize % pageSize;
                    currentStartPosition = (int) (totalSize - tempPageSize);

                } else {
                    if (startIndex == 0) {
                        currentStartPosition = tempTotalSize - pageSize;
                    } else {
                        currentStartPosition = tempTotalSize - pageSize + 1;
                    }
                    tempPageSize = pageSize;
                }
                List<T> result = executor.executeQuery(em, currentStartPosition, tempPageSize);
                currentEndPosition = tempTotalSize;
                return result;
            }
        }
    }

    public List<T> getNextPage(EntityManager em) {
        if (nextPageExists(em)) {
            List<T> result;
            if (!paginated) {
                return executor.executeQuery(em, startIndex, pageSizeIfNotInit());
            } else {
                if (!initialized) {
                    result = executor.executeQuery(em, startIndex, pageSizeIfNotInit());
                    initializeWithResult(result.size());
                } else {
                    int tempPageSize;
                    if((totalSize - currentEndPosition ) >= pageSize){
                        tempPageSize = pageSize;
                    }else{
                        tempPageSize = (int) (totalSize - currentEndPosition);
                    }
                    result = executor.executeQuery(em, currentEndPosition, tempPageSize);
                    updateWithQueryResult(result.size());
                }
            }
            return result;
        } else {
            throw new InvalidDataException("You are already on the last page.");
        }
    }

    public List<T> getPreviousPage(EntityManager em) {
        if (previousPageExists(em)) {
            List<T> result;
            if (!paginated) {
                return executor.executeQuery(em, startIndex, pageSizeIfNotInit());
            } else {
                if (!initialized) {
                    result = executor.executeQuery(em, startIndex, pageSizeIfNotInit());
                    initializeWithResult(result.size());
                } else {
                    currentStartPosition = currentStartPosition - pageSize;
                    result = executor.executeQuery(em, currentStartPosition, pageSizeIfInit());
                    currentEndPosition = currentStartPosition + result.size();
                }
            }
            return result;
        } else {
            throw new InvalidDataException("You are already on the first page.");
        }
    }

    public boolean nextPageExists(EntityManager em) {
        initializeTotalSize(em);
        /*
         * Determines and updates the current start, end indexes and the current page size 
         * if the user delete records
         */
        if (currentEndPosition != null && currentEndPosition > totalSize) {
            currentEndPosition = totalSize.intValue();
            if (totalSize % pageSize == 0) {
                currentStartPosition = currentEndPosition - pageSize;
            } else {
                currentStartPosition = currentEndPosition - (totalSize.intValue() % pageSize);
            }
            currentEndPosition = currentStartPosition;
        }
        if (!initialized) {
            return true;
        } else {
            if ((currentEndPosition + 1) < totalSize) {
                return true;
            }
        }
        return false;
    }

    public boolean previousPageExists(EntityManager em) {
        initializeTotalSize(em);
        /*
         * Determines and updates the current start, end indexes and the current page size 
         * if the user delete records
         */
        if (currentEndPosition != null && currentEndPosition > totalSize) {
            currentEndPosition = totalSize.intValue();
            currentStartPosition = currentEndPosition - pageSizeIfInit();
        }
        if (!initialized) {
            return true;
        } else {
            if (currentEndPosition >= currentStartPosition && currentStartPosition != startIndex) {
                return true;
            }
        }
        return false;
    }

    public String getIndexRange() {
        String indexRange = null;
        if (startIndex == 0) {
            indexRange = String.valueOf(currentStartPosition + 1) + "-" + currentEndPosition;
        } else {
            int tempPageSize;
            if((totalSize - currentStartPosition ) >= pageSize){
                tempPageSize = pageSize;
            }else{
                tempPageSize = (int) (totalSize - currentEndPosition);
            }
            if (tempPageSize == pageSize || tempPageSize == 0) {
                indexRange = String.valueOf(currentStartPosition) + "-" + (currentEndPosition-1);
            } else {
                indexRange = String.valueOf(currentStartPosition) + "-" + currentEndPosition;
            }
        }
        return indexRange;
    }

    private void initializeWithResult(int resultSize) {
        if (resultSize == 0) {
            return;
        } else {
            currentStartPosition = startIndex;
            currentEndPosition = currentStartPosition + resultSize;
            initialized = true;
        }
    }

    private void initializeTotalSize(EntityManager em) {
        this.totalSize = executor.executeTotalRowsQuery(em);
    }

    private void updateWithQueryResult(int resultSize) {
        if (resultSize == 0) {
            return;
        } else {
            currentStartPosition = currentEndPosition;
            currentEndPosition = currentEndPosition + resultSize;
        }
    }

    private int pageSizeIfInit() {
        int noOfRecordsPerPage;
        if ((totalSize - (currentStartPosition + pageSize)) >= 0) {
            noOfRecordsPerPage = pageSize;
        } else {
            noOfRecordsPerPage = (int) (totalSize % pageSize);
        }
        return noOfRecordsPerPage;
    }

    private int pageSizeIfNotInit() {
        int noOfRecordsPerPage;
        int tempIndex = startIndex + pageSize;
        if ((totalSize % pageSize == 0) || totalSize >= tempIndex) {
            noOfRecordsPerPage = pageSize;
        } else {
            noOfRecordsPerPage = (int) (totalSize % pageSize);
        }
        return noOfRecordsPerPage;
    }

}