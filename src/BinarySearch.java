public class BinarySearch {
    public static int getLexemeIndex(String[] arr, String val) {
        var first = 0;
        var last = arr.length - 1;

        while (first <= last) {
            var middle = (first + last) / 2;

            if (val.equals(arr[middle])) {
                return middle;
            }

            if (val.compareTo(arr[middle]) < 0) {
                last = middle - 1;
            } else {
                first = middle + 1;
            }
        }

        return -1;
    }
}