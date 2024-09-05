package apros.codeart.dto;

import apros.codeart.pooling.util.StringPool;
import apros.codeart.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DTObjects implements Iterable<DTObject> {
    @NotNull
    @Override
    public Iterator<DTObject> iterator() {
        return _list.iterator();
    }


    private final List<DTObject> _list;

    public DTObjects() {
        _list = new ArrayList<>();
    }

    public DTObjects(List<DTObject> items) {
        _list = items;
    }

    public void add(DTObject item) {
        _list.add(item);
    }

    public DTObject get(int index) {
        return _list.get(index);
    }

    public int size() {
        return _list.size();
    }

    public boolean contains(DTObject item) {
        return _list.contains(item);
    }

    public int indexOf(DTObject item) {
        return _list.indexOf(item);
    }

    public void remove(DTObject item) {
        _list.remove(item);
    }

    /**
     * 将数组转为一个对象，数组作为对象的成员
     *
     * @param memberName
     * @return
     */
    public DTObject toObject(String memberName) {
        var obj = DTObject.editable();
        obj.pushObjects(memberName, _list);
        return obj;
    }

//    //region 自定义方法
//
//    public DTObject[] ToArray()
//    {
//        return _list.ToArray();
//    }
//
//    public T[] ToArray<T>()
//    {
//        T[] data = new T[_list.Count];
//        for (var i = 0; i < _list.Count; i++)
//        {
//            data[i] = _list[i].GetValue<T>();
//        }
//        return data;
//    }
//
//    public IEnumerable<T> OfType<T>()
//    {
//        return ToArray<T>();
//    }
//
//
//    public T[] ToArray<T>(Func<DTObject, T> func)
//    {
//        T[] data = new T[_list.Count];
//        for (var i = 0; i < _list.Count; i++)
//        {
//            data[i] = func(_list[i]);
//        }
//        return data;
//    }
//
//    /// <summary>
///// 直接提取枚举
///// </summary>
///// <typeparam name="T"></typeparam>
///// <returns></returns>
//    public IEnumerable<T> ToEnum<T>()
//    {
//        var enumType = typeof(T);
//        return this.Select((t) =>
//                {
//                        var value = t.GetValue<byte>();
//        return (T)Enum.ToObject(typeof(T), value);
//    });
//    }


    public String getCode(boolean sequential) {
        return StringPool.using((code) -> {
            code.append("[");
            for (DTObject item : _list) {
                code.append(item.getCode(sequential, false));
                code.append(",");
            }
            if (!_list.isEmpty()) StringUtil.removeLast(code);
            code.append("]");
        });

    }

    public static DTObjects readonly(String code) {
        var obj = DTObject.readonly(String.format("{rows:\"%s\"}", code));
        return obj.getList("rows");
    }


    public final static DTObjects Empty = new DTObjects(new ArrayList<>(0));

    public static DTObjects empty() {
        return Empty;
    }

}
