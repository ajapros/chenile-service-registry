package org.chenile.service.registry.model;

import org.chenile.core.model.HTTPMethod;
import org.chenile.core.model.HttpBindingType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

public final class ServiceRegistryFingerprint {
    private static final Pattern VERSION_SPLIT_PATTERN = Pattern.compile("[.\\-_+]");

    private ServiceRegistryFingerprint() {
    }

    public static String serviceKey(ChenileRemoteServiceDefinition service) {
        if (service == null)
            return "";
        return String.join("|", nullSafe(service.serviceId), nullSafe(service.serviceVersion));
    }

    public static String serviceFingerprint(ChenileRemoteServiceDefinition service) {
        if (service == null)
            return "";
        return String.join("|",
                nullSafe(service.baseUrl),
                nullSafe(service.serviceId),
                nullSafe(service.serviceVersion),
                nullSafe(service.getMonolithName()),
                nullSafe(service.healthCheckerName),
                listKey(service.clientInterceptorNames),
                operationListKey(service.operations));
    }

    public static boolean semanticallyEquals(ChenileRemoteServiceDefinition first,
                                             ChenileRemoteServiceDefinition second) {
        return Objects.equals(serviceFingerprint(first), serviceFingerprint(second));
    }

    public static int compareVersions(String first, String second) {
        if (Objects.equals(first, second))
            return 0;
        if (first == null || first.isBlank())
            return -1;
        if (second == null || second.isBlank())
            return 1;
        String[] firstParts = VERSION_SPLIT_PATTERN.split(first);
        String[] secondParts = VERSION_SPLIT_PATTERN.split(second);
        int length = Math.max(firstParts.length, secondParts.length);
        for (int i = 0; i < length; i++) {
            String firstPart = i < firstParts.length ? firstParts[i] : "0";
            String secondPart = i < secondParts.length ? secondParts[i] : "0";
            int comparison = compareVersionPart(firstPart, secondPart);
            if (comparison != 0)
                return comparison;
        }
        return first.compareTo(second);
    }

    public static List<ChenileRemoteOperationDefinition> normalizeOperations(
            List<ChenileRemoteOperationDefinition> operations) {
        return normalize(operations, ServiceRegistryFingerprint::operationFingerprint);
    }

    public static List<ChenileRemoteParamDefinition> normalizeParams(List<ChenileRemoteParamDefinition> params) {
        return normalizePreservingOrder(params, ServiceRegistryFingerprint::paramFingerprint);
    }

    public static String operationFingerprint(ChenileRemoteOperationDefinition operation) {
        if (operation == null)
            return "";
        return String.join("|",
                nullSafe(operation.description),
                nullSafe(operation.name),
                nullSafe(operation.consumes),
                nullSafe(operation.url),
                nullSafe(operation.output),
                nullSafe(operation.httpMethod),
                nullSafe(operation.getOutputAsStringReference()),
                listKey(operation.clientInterceptorNames),
                listKey(operation.bodyTypeSelectorComponentNames),
                paramListKey(operation.params));
    }

    public static String paramFingerprint(ChenileRemoteParamDefinition param) {
        if (param == null)
            return "";
        return String.join("|",
                nullSafe(param.name),
                nullSafe(param.description),
                nullSafe(param.paramClassName),
                nullSafe(param.paramTypeReference),
                nullSafe(param.type));
    }

    public static String operationDisplayKey(ChenileRemoteOperationDefinition operation) {
        if (operation == null)
            return "";
        return String.join(" ",
                nullSafe(operation.httpMethod),
                nullSafe(operation.url),
                nullSafe(operation.name)).trim();
    }

    public static String paramDisplayKey(ChenileRemoteParamDefinition param) {
        if (param == null)
            return "";
        return String.join(" ",
                nullSafe(param.type),
                nullSafe(param.name),
                nullSafe(param.paramTypeReference != null ? param.paramTypeReference : param.paramClassName)).trim();
    }

    private static String operationListKey(List<ChenileRemoteOperationDefinition> operations) {
        if (operations == null)
            return "";
        return normalizeOperations(operations).stream()
                .map(ServiceRegistryFingerprint::operationFingerprint)
                .collect(Collectors.joining(","));
    }

    private static String paramListKey(List<ChenileRemoteParamDefinition> params) {
        if (params == null)
            return "";
        return normalizeParams(params).stream()
                .map(ServiceRegistryFingerprint::paramFingerprint)
                .collect(Collectors.joining(","));
    }

    private static <T> List<T> normalize(List<T> items, Function<T, String> keyExtractor) {
        if (items == null)
            return null;
        return new ArrayList<>(items.stream()
                .collect(Collectors.toMap(keyExtractor, Function.identity(), (first, ignored) -> first))
                .values())
                .stream()
                .sorted(Comparator.comparing(keyExtractor))
                .toList();
    }

    private static <T> List<T> normalizePreservingOrder(List<T> items, Function<T, String> keyExtractor) {
        if (items == null)
            return null;
        Map<String, T> uniqueItems = new LinkedHashMap<>();
        for (T item : items) {
            uniqueItems.putIfAbsent(keyExtractor.apply(item), item);
        }
        return new ArrayList<>(uniqueItems.values());
    }

    private static int compareVersionPart(String first, String second) {
        boolean firstNumeric = first.matches("\\d+");
        boolean secondNumeric = second.matches("\\d+");
        if (firstNumeric && secondNumeric) {
            return Long.compare(Long.parseLong(first), Long.parseLong(second));
        }
        if (firstNumeric != secondNumeric)
            return firstNumeric ? 1 : -1;
        return first.compareTo(second);
    }

    private static String listKey(List<String> values) {
        if (values == null)
            return "";
        return String.join(",", values);
    }

    private static String nullSafe(Object value) {
        if (value == null)
            return "";
        if (value instanceof HTTPMethod || value instanceof HttpBindingType)
            return value.toString();
        return String.valueOf(value);
    }
}
