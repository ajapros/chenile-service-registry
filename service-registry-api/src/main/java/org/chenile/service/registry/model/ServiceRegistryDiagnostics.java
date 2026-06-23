package org.chenile.service.registry.model;

import java.util.ArrayList;
import java.util.List;

public class ServiceRegistryDiagnostics {
    public int totalServices;
    public int duplicateServiceVersionGroups;
    public int duplicateOperationLinks;
    public int duplicateParamLinks;
    public int changedSameVersionGroups;
    public int invalidServiceRows;
    public List<ServiceVersionIssue> duplicateServiceVersions = new ArrayList<>();
    public List<ServiceVersionIssue> changedSameVersions = new ArrayList<>();
    public List<ServiceVersionIssue> invalidServices = new ArrayList<>();
    public List<ServiceLinkIssue> duplicateOperationLinkDetails = new ArrayList<>();
    public List<ServiceLinkIssue> duplicateParamLinkDetails = new ArrayList<>();
    public List<String> warnings = new ArrayList<>();

    public boolean isClean() {
        return duplicateServiceVersionGroups == 0
                && duplicateOperationLinks == 0
                && duplicateParamLinks == 0
                && changedSameVersionGroups == 0
                && invalidServiceRows == 0;
    }

    public static class ServiceVersionIssue {
        public String serviceId;
        public String serviceVersion;
        public int rowCount;
        public int fingerprintCount;
        public List<String> rowIds = new ArrayList<>();
    }

    public static class ServiceLinkIssue {
        public String serviceId;
        public String serviceVersion;
        public String serviceRowId;
        public String operation;
        public String param;
        public int duplicateCount;
    }
}
