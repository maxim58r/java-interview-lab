package ru.max.test.test5;

import java.util.Arrays;

public class Test {
    public String collapseArray(int[] arr) {
        if (arr == null || arr.length == 0) return "";
        if (arr.length == 1) return String.valueOf(arr[0]);

        Arrays.sort(arr);

        StringBuilder res = new StringBuilder();
        int start = arr[0];
        int end = arr[0];

        for (int i = 1; i < arr.length; i++) {
            int prev = arr[i - 1];
            int curr = arr[i];

            if (curr == prev) {
                // дубликат: просто пропускаем
                continue;
            }

            // соседний по числовому ряду? сравнение без переполнения
            if ((long) curr == (long) prev + 1L) {
                end = curr; // расширяем текущий диапазон
            } else {
                // разрыв — закрываем диапазон [start..end] и начинаем новый
                appendRange(res, start, end);
                start = end = curr;
            }
        }

        // финализируем последний диапазон
        appendRange(res, start, end);
        return res.toString();
    }

    private static void appendRange(StringBuilder sb, int start, int end) {
        if (!sb.isEmpty()) sb.append(',');
        if (start == end) {
            sb.append(start);
        } else {
            sb.append(start).append('-').append(end);
        }
    }
}


//public class Test {
//    public String collapseArray(int[] arr) {
//        if (arr == null || arr.length == 0) return "";
//        if (arr.length == 1) return String.valueOf(arr[0]);
//
//        Arrays.sort(arr);
//        StringBuilder res = new StringBuilder();
//        int start = arr[0];
//        int end = arr[0];
//
//        for (int curr = 1; curr < arr.length; curr++) {
//            if (arr[curr] - arr[curr - 1] == 0) continue;
//            else if (arr[curr] - arr[curr - 1] != 1) {
//                collapse(res, start, arr[curr - 1]);
//                start = end = arr[curr];
//            } else {
//                end = arr[curr];
//            }
//        }
//
//        collapse(res, start, end);
//        return res.toString();
//    }
//
//    private void collapse(StringBuilder res, int start, int curr) {
//        if (!res.isEmpty()) res.append(',');
//        if (start < curr) res.append(start).append('-').append(curr);
//        if (start == curr) res.append(start);
//
//    }
//}
