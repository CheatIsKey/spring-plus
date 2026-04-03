package org.example.expert.aop;

import org.example.expert.aop.dto.Masking;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LogMaskingUtils {

    /**
     * 파라미터 배열을 순회하면서 객체 내부의 @Masking 필드를 찾아 가려주는 문자열을 반환한다.
     */
    public static String buildMaskedParamsString(String[] parameterNames, Object[] args) {
        if (args == null || args.length == 0) {
            return "없음";
        }
        List<String> paramList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String paramName = parameterNames != null ? parameterNames[i] : "arg" + i;
            Object arg = args[i];

            if (arg == null) {
                paramList.add(paramName + "=null");
                continue;
            }

            // DTO 객체인 경우: 리플렉션으로 내부 필드를 탐색하여 @Masking 필드만 가린다.
            // 기본형 래퍼·String은 DTO가 아니므로 그대로 출력한다.
            if (!isWrapperType(arg.getClass()) && arg.getClass() != String.class) {
                String maskedObjectString = applyMaskingToObject(arg);
                paramList.add(paramName + "=" + maskedObjectString);
            } else {
                paramList.add(paramName + "=" + arg);
            }
        }
        return StringUtils.collectionToCommaDelimitedString(paramList);
    }

    /**
     * DTO 객체의 필드들을 리플렉션으로 읽어 @Masking이 붙은 필드는 "******"로 치환한다.
     */
    private static String applyMaskingToObject(Object obj) {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldStrings = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true); // private 필드에도 접근할 수 있도록 허용
            try {
                Object value = field.get(obj);
                // @Masking이 붙어있고 값이 null이 아니면 마스킹
                if (field.isAnnotationPresent(Masking.class) && value != null) {
                    fieldStrings.add(field.getName() + "='******'");
                } else {
                    fieldStrings.add(field.getName() + "=" + value);
                }
            } catch (IllegalAccessException e) {
                fieldStrings.add(field.getName() + "=ERROR");
            }
        }
        return clazz.getSimpleName() + "{"
                + StringUtils.collectionToCommaDelimitedString(fieldStrings) + "}";
    }

    /**
     * 자바 기본형 래퍼 클래스인지 확인한다.
     * 수정 포인트: LocalDate, BigDecimal 등 값 객체를 추가로 처리해야 하면 이 메서드에 추가한다.
     */
    private static boolean isWrapperType(Class<?> clazz) {
        return clazz == Boolean.class   || clazz == Character.class ||
               clazz == Byte.class      || clazz == Short.class     ||
               clazz == Integer.class   || clazz == Long.class      ||
               clazz == Float.class     || clazz == Double.class;
    }
}