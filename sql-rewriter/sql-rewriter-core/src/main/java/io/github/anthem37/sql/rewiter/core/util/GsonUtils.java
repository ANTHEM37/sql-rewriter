package io.github.anthem37.sql.rewiter.core.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * gson的工具类，提供常见的 JSON 操作方法
 * <p>
 * 功能涵盖：
 * - 对象/集合 序列化与反序列化（支持泛型 Type）
 * - JSON 校验、漂亮打印
 * - Map/List 与 JSON 的互转
 * - JsonElement 解析与类型判断
 * - 深拷贝（通过 JSON）
 * - JsonObject 合并与便捷写入
 * <p>
 * 统一使用默认配置的单例 Gson：
 * - 日期格式：yyyy-MM-dd HH:mm:ss
 * - 关闭 HTML 转义
 * - 开启序列化 null（可选提供不序列化 null 的实例）
 *
 * @author anthem37
 * @since 2025/11/12 14:58:23
 */
public final class GsonUtils {

    private static final String DEFAULT_DATE_PATTERN = DatePattern.NORM_DATETIME_PATTERN;

    /**
     * 默认全局 Gson（serializeNulls=true, disableHtmlEscaping=true）
     */
    private static final Gson DEFAULT_GSON = new GsonBuilder().setDateFormat(DEFAULT_DATE_PATTERN).disableHtmlEscaping().serializeNulls().create();

    /**
     * 不序列化 null 的 Gson 实例
     */
    private static final Gson GSON_NO_NULLS = new GsonBuilder().setDateFormat(DEFAULT_DATE_PATTERN).disableHtmlEscaping().create();

    /**
     * 私有构造，禁止实例化
     */
    private GsonUtils() {
    }

    // ------------------------------ 基础获取 ------------------------------

    /**
     * 获取默认 Gson 单例
     */
    public static Gson gson() {
        return DEFAULT_GSON;
    }

    /**
     * 获取不序列化 null 的 Gson 单例
     */
    public static Gson gsonNoNulls() {
        return GSON_NO_NULLS;
    }

    /**
     * 创建自定义配置的 GsonBuilder（便于外部扩展 TypeAdapter 等）
     */
    public static GsonBuilder newGsonBuilder() {
        return new GsonBuilder().setDateFormat(DEFAULT_DATE_PATTERN).disableHtmlEscaping();
    }

    // ------------------------------ 序列化 ------------------------------

    /**
     * 对象序列化为 JSON 字符串（使用默认 Gson）
     */
    public static String toJson(Object src) {
        if (src == null) {
            return "null";
        }
        return DEFAULT_GSON.toJson(src);
    }

    /**
     * 对象序列化为 JSON 字符串（可选漂亮打印）
     */
    public static String toJson(Object src, boolean pretty) {
        if (src == null) {
            return "null";
        }
        Gson gson = pretty ? new GsonBuilder().setDateFormat(DEFAULT_DATE_PATTERN).disableHtmlEscaping().serializeNulls().setPrettyPrinting().create() : DEFAULT_GSON;
        return gson.toJson(src);
    }

    /**
     * 空安全序列化，src 为 null 时返回默认值
     */
    public static String safeToJson(Object src, String defaultValue) {
        return src == null ? defaultValue : DEFAULT_GSON.toJson(src);
    }

    // ------------------------------ 反序列化 ------------------------------

    /**
     * 字符串反序列化为对象（Class）
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json) || clazz == null) {
            return null;
        }
        return DEFAULT_GSON.fromJson(json, clazz);
    }

    /**
     * 字符串反序列化为对象（Type）——支持泛型
     */
    public static <T> T fromJson(String json, Type type) {
        if (StrUtil.isBlank(json) || type == null) {
            return null;
        }
        return DEFAULT_GSON.fromJson(json, type);
    }

    /**
     * 字符串反序列化为对象，失败时返回默认值
     */
    public static <T> T fromJsonOrDefault(String json, Class<T> clazz, T defaultValue) {
        try {
            T value = fromJson(json, clazz);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 字符串反序列化为对象（Type），失败时返回默认值
     */
    public static <T> T fromJsonOrDefault(String json, Type type, T defaultValue) {
        try {
            T value = fromJson(json, type);
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ------------------------------ Map/List 转换 ------------------------------

    /**
     * JSON 转 Map<String, Object>
     */
    public static Map<String, Object> toMap(String json) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyMap();
        }
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = fromJson(json, type);
        return map == null ? Collections.emptyMap() : map;
    }

    /**
     * JSON 转 List<T>（通过 Class<T>，内部使用 TypeToken）
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        if (StrUtil.isBlank(json) || elementClass == null) {
            return Collections.emptyList();
        }
        Type type = TypeToken.getParameterized(List.class, elementClass).getType();
        List<T> list = fromJson(json, type);
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * JSON 转 List<Object>（非确定类型场景）
     */
    public static List<Object> toObjectList(String json) {
        if (StrUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        Type type = new TypeToken<List<Object>>() {
        }.getType();
        List<Object> list = fromJson(json, type);
        return list == null ? Collections.emptyList() : list;
    }

    // ------------------------------ JsonElement 解析与判断 ------------------------------

    /**
     * 解析为 JsonElement（空白返回 null）
     */
    public static JsonElement parse(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return JsonParser.parseString(json);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析为 JsonObject（非对象或异常返回 null）
     */
    public static JsonObject parseObject(String json) {
        JsonElement el = parse(json);
        return el != null && el.isJsonObject() ? el.getAsJsonObject() : null;
    }

    /**
     * 解析为 JsonArray（非数组或异常返回 null）
     */
    public static JsonArray parseArray(String json) {
        JsonElement el = parse(json);
        return el != null && el.isJsonArray() ? el.getAsJsonArray() : null;
    }

    /**
     * 是否是合法 JSON（对象或数组均视为合法）
     */
    public static boolean isJson(String json) {
        return parse(json) != null;
    }

    /**
     * 是否是合法的 JSON 对象
     */
    public static boolean isJsonObject(String json) {
        JsonElement el = parse(json);
        return el != null && el.isJsonObject();
    }

    /**
     * 是否是合法的 JSON 数组
     */
    public static boolean isJsonArray(String json) {
        JsonElement el = parse(json);
        return el != null && el.isJsonArray();
    }

    /**
     * 对象转为 JsonElement
     */
    public static JsonElement toJsonTree(Object src) {
        return DEFAULT_GSON.toJsonTree(src);
    }

    /**
     * JsonElement 转对象（Class）
     */
    public static <T> T fromJson(JsonElement element, Class<T> clazz) {
        if (element == null || clazz == null) {
            return null;
        }
        return DEFAULT_GSON.fromJson(element, clazz);
    }

    /**
     * JsonElement 转对象（Type）
     */
    public static <T> T fromJson(JsonElement element, Type type) {
        if (element == null || type == null) {
            return null;
        }
        return DEFAULT_GSON.fromJson(element, type);
    }

    // ------------------------------ 漂亮打印与格式化 ------------------------------

    /**
     * 漂亮打印 JSON 字符串（输入非 JSON 原样返回）
     */
    public static String pretty(String json) {
        JsonElement el = parse(json);
        if (el == null) {
            return json;
        }
        Gson gson = new GsonBuilder().setDateFormat(DEFAULT_DATE_PATTERN).disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();
        return gson.toJson(el);
    }

    // ------------------------------ 深拷贝 ------------------------------

    /**
     * 深拷贝对象（通过 JSON）（Class）
     */
    public static <T> T deepCopy(T src, Class<T> clazz) {
        if (src == null || clazz == null) {
            return null;
        }
        String json = DEFAULT_GSON.toJson(src);
        return DEFAULT_GSON.fromJson(json, clazz);
    }

    /**
     * 深拷贝对象（通过 JSON）（Type）
     */
    public static <T> T deepCopy(T src, Type type) {
        if (src == null || type == null) {
            return null;
        }
        String json = DEFAULT_GSON.toJson(src);
        return DEFAULT_GSON.fromJson(json, type);
    }

    // ------------------------------ JsonObject 合并与便捷写入 ------------------------------

    /**
     * 合并多个 JsonObject，后者覆盖前者，返回新对象（不修改入参）
     */
    public static JsonObject merge(JsonObject first, JsonObject... others) {
        JsonObject result = new JsonObject();
        if (first != null) {
            copyAll(result, first);
        }
        if (others != null) {
            for (JsonObject obj : others) {
                if (obj != null) {
                    copyAll(result, obj);
                }
            }
        }
        return result;
    }

    /**
     * 将 source 的所有键值复制到 target（覆盖）
     */
    private static void copyAll(JsonObject target, JsonObject source) {
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            target.add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取或创建子对象
     */
    public static JsonObject getOrCreateObject(JsonObject parent, String key) {
        if (parent == null || StrUtil.isBlank(key)) {
            return null;
        }
        JsonElement el = parent.get(key);
        if (el != null && el.isJsonObject()) {
            return el.getAsJsonObject();
        }
        JsonObject obj = new JsonObject();
        parent.add(key, obj);
        return obj;
    }

    /**
     * 便捷写入：当 value 非空时写入到 JsonObject
     */
    public static void putIfNotNull(JsonObject obj, String key, Object value) {
        if (obj == null || StrUtil.isBlank(key) || ObjectUtil.isEmpty(value)) {
            return;
        }
        obj.add(key, DEFAULT_GSON.toJsonTree(value));
    }
}
