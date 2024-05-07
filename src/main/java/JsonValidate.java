import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonValidate {

    int index = 0;

    private static boolean isJsonWs(char c) {
        switch (c) {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                return true;
            default:
                return false;
        }
    }

    private static boolean isJsonDigit(char c) {
        return c <= '9' && c >= '0';
    }

    private void readWhitespace(char[] json) {
        while (index != json.length && isJsonWs(json[index])) {
            index++;
        }
    }

    private Double readNumber(char[] json) throws JsonValidateException {
        double sign = 1.0;
        Double num = 0.0;
        if (json[index] == '-') {
            sign *= -1;
            index++;
            if (json.length == index) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
        }
        if (!isJsonDigit(json[index])) {
            throw new JsonValidateException("Данная строка не является JSON файлом!");
        }
        if (json[index] != '0') {
            int sum = json[index] - '0';
            index++;
            if (json.length == index) {
                return sum * sign;
            }
            while (isJsonDigit(json[index])) {
                sum *= 10;
                sum += json[index] - '0';
                index++;
                if (json.length == index) {
                    return sum * sign;
                }
            }
            num += sum;
        } else if (json[index] == '0') {
            index++;
            if (json.length == index) {
                return 0.0;
            } else if (json[index] != '.' && json[index] != 'E' && json[index] != 'e') {
                return 0.0;
            }
        }
        if (json[index] == '.') {
            double frac = 0;
            index++;
            if (json.length == index) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            if (!isJsonDigit(json[index])) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            int cnt = 1;
            while (isJsonDigit(json[index])) {
                frac += (json[index] - '0') / Math.pow(10, cnt);
                index++;
                cnt++;
                if (json.length == index) {
                    return (num + frac) * sign;
                }
            }
            num += frac;
        }

        if (json[index] == 'e' || json[index] == 'E') {
            index++;
            if (json.length == index) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }

            if (json[index] == '-') {
                index++;
                if (json.length == index || !isJsonDigit(json[index])) {
                    throw new JsonValidateException("Данная строка не является JSON файлом!");
                }
                int exp = 0;
                while (isJsonDigit(json[index])) {
                    exp *= 10;
                    exp += json[index] - '0';
                    index++;
                    if (json.length == index) {
                        return (num / Math.pow(10, exp)) * sign;
                    }
                }
                num /= Math.pow(10, exp);
            } else {
                if (json[index] == '+') {
                    index++;
                }
                if (json.length == index || !isJsonDigit(json[index])) {
                    throw new JsonValidateException("Данная строка не является JSON файлом!");
                }
                int exp = 0;
                while (isJsonDigit(json[index])) {
                    exp *= 10;
                    exp += json[index] - '0';
                    index++;
                    if (json.length == index) {
                        return num * Math.pow(10, exp) * sign;
                    }
                }
                num *= Math.pow(10, exp);
            }
        }
        return num * sign;
    }

    private String readString(char[] json) throws JsonValidateException {
        StringBuilder buf = new StringBuilder();
        index++;
        if (json.length == index) {
            throw new JsonValidateException("Данная строка не является JSON файлом!");
        }
        while (json[index] != '"') {
            if (json[index] < 32) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            if (json[index] == '\\') {
                index++;
                if (json.length == index) {
                    throw new JsonValidateException("Данная строка не является JSON файлом!");
                }
                switch (json[index]) {
                    case '"':
                        buf.append('"');
                        break;
                    case '\\':
                        buf.append('\\');
                        break;
                    case '/':
                        buf.append('/');
                        break;
                    case 'b':
                        buf.append('\b');
                        break;
                    case 'f':
                        buf.append('\f');
                        break;
                    case 'n':
                        buf.append('\n');
                        break;
                    case 'r':
                        buf.append('\r');
                        break;
                    case 't':
                        buf.append('\t');
                        break;
                    case 'u':
                        int hex = 0;
                        for (int i = 0; i < 4; i++) {
                            index++;
                            if (json.length == index || Character.digit(json[index], 16) == -1) {
                                throw new JsonValidateException("Данная строка не является JSON файлом!");
                            }
                            hex *= 16;
                            hex += Character.digit(json[index], 16);
                        }
                        buf.append(Character.toChars(hex)[0]);
                        break;
                    default:
                        throw new JsonValidateException("Данная строка не является JSON файлом!");
                }
            } else {
                buf.append(json[index]);
            }
            index++;
            if (json.length == index) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
        }
        index++;
        return buf.toString();
    }

    private Boolean readBoolNull(char[] json) throws JsonValidateException {
        if (json[index] == 't') {
            if (index + 3 >= json.length) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            if (!(new String(json, index, 4).equals("true"))) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            index += 4;
            return true;
        } else if (json[index] == 'f') {
            if (index + 4 >= json.length) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            if (!(new String(json, index, 5).equals("false"))) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            index += 5;
            return false;
        } else {
            if (index + 3 >= json.length) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            if (!(new String(json, index, 4).equals("null"))) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            index += 4;
            return null;
        }
    }

    private Object[] readArray(char[] json) throws JsonValidateException {
        List<Object> res = new ArrayList<Object>();
        index++;
        if (json.length == index) {
            throw new JsonValidateException("Данная строка не является JSON файлом!");
        }

        readWhitespace(json);

        if (json.length == index) {
            throw new JsonValidateException("Данная строка не является JSON файлом!");
        }

        while (json[index] != ']') {
            res.add(readValue(json));
            if (json.length == index || (json[index] != ',' && json[index] != ']')) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            if (json[index] == ',') {
                index++;
                if (json.length == index) {
                    throw new JsonValidateException("Данная строка не является JSON файлом!");
                }
                if (json[index] == ']') {
                    throw new JsonValidateException("Данная строка не является JSON файлом!");
                }
            }

        }
        index++;
        return res.toArray();
    }

    private Map<String, Object> readObject(char[] json) throws JsonValidateException {
        Map<String, Object> res = new HashMap<String, Object>();
        index++;
        if (json.length == index) {
            throw new JsonValidateException("Данная строка не является JSON файлом!");
        }

        readWhitespace(json);

        if (json.length == index) {
            throw new JsonValidateException("Данная строка не является JSON файлом!");
        }

        while (json[index] != '}') {
            readWhitespace(json);
            String t = readString(json);
            readWhitespace(json);
            if (json.length == index || json[index] != ':') {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            index++;
            if (json.length == index) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            Object val = readValue(json);
            res.put(t, val);
            if (json.length == index || (json[index] != ',' && json[index] != '}')) {
                throw new JsonValidateException("Данная строка не является JSON файлом!");
            }
            if (json[index] == ',') {
                index++;
                if (json.length == index) {
                    throw new JsonValidateException("Данная строка не является JSON файлом!");
                }
                if (json[index] == '}') {
                    throw new JsonValidateException("Данная строка не является JSON файлом!");
                }
            }
        }
        index++;
        return res;
    }

    private Object readValue(char[] json) throws JsonValidateException {
        readWhitespace(json);

        Object res;
        if (json.length == index) {
            throw new JsonValidateException("Данная строка не является JSON файлом!");
        } else if (json[index] == '{') {
            res = readObject(json);
        } else if (json[index] == '[') {
            res = readArray(json);
        } else if (json[index] == '"') {
            res = readString(json);
        } else if (isJsonDigit(json[index]) || json[index] == '-') {
            res = readNumber(json);
        } else {
            res = readBoolNull(json);
        }

        readWhitespace(json);

        return res;
    }

    public boolean check(String json) {
        index = 0;
        try {
            readValue(json.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
        if (index != json.length()) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private String JsonObjectToString(Object o) {
        if (o instanceof Double || o instanceof Boolean) {
            return o.toString();
        }

        if (o == null) {
            return "null";
        }

        if (o instanceof String) {
            return "\"" + o.toString() + "\"";
        }

        if (o instanceof Object[]) {
            StringBuilder buf = new StringBuilder();
            buf.append("[");
            for (Object i : (Object[]) o) {
                buf.append(JsonObjectToString(i) + ",");
            }
            if (((Object[]) o).length > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            buf.append("]");
            return buf.toString();
        }
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        for (Map.Entry<String, Object> i : ((Map<String, Object>) o).entrySet()) {
            buf.append(JsonObjectToString(i.getKey()) + ":");
            buf.append(JsonObjectToString(i.getValue()) + ",");
        }
        if (((Map<String, Object>) o).size() > 0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append("}");
        return buf.toString();
    }

    @SuppressWarnings("unchecked")
    public String getValue(String json, String key) throws IllegalArgumentException, RuntimeException {
        index = 0;
        Object res = readValue(json.toCharArray());
        if (!(res instanceof Map)) {
            throw new IllegalArgumentException("JSON не представляет из себя объект!");
        }
        if (!((Map<String, Object>) res).containsKey(key)) {
            throw new RuntimeException("Ключ не найден в JSON файле!");
        }

        return JsonObjectToString(((Map<String, Object>) res).get(key));
    }

}
