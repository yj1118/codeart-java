package apros.codeart.dto;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SchemeTest {

    @Test
    public void Common() {
        DTObject schema = DTObject.readonly("{id}");

        var members = schema.getMembers();

        assertEquals(1, Iterables.size(members));

        var member = Iterables.get(members, 0);
        assertEquals("id", member.getName());
    }

    @Test
    public void SchemeRuleTest() {


        var schema = DTObject.readonly("{Id,Name,Childs:[{name}],cs:[],parent,father:{name}}");

        assertEquals("Id", schema.matchChildSchema("Id"));
        assertEquals("Name", schema.matchChildSchema("Name"));

        var childsScheme = schema.getChildSchema("Childs");
        assertEquals("name", childsScheme.matchChildSchema("name"));

        var csScheme = schema.getChildSchema("cs");
        assertEquals("id", csScheme.matchChildSchema("id"));
        assertEquals("name", csScheme.matchChildSchema("name"));


        var parentScheme = schema.getChildSchema("parent");
        assertEquals("id", parentScheme.matchChildSchema("id"));
        assertEquals("id", parentScheme.matchChildSchema("id"));

        var fatherScheme = schema.getChildSchema("father");
        assertEquals("name", fatherScheme.matchChildSchema("name"));

    }

    public static class User {
        private int _id;

        public int getId() {
            return _id;
        }

        public void setId(int id) {
            _id = id;
        }

        private String _name;

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        public User(int id, String name) {
            _id = id;
            _name = name;
        }

        private User _parent;

        public User parent() {
            return _parent;
        }

        private User _father;

        public User father() {
            return _father;
        }

        void setParent(User value) {
            _parent = value;
            _father = value;
        }

        private final ArrayList<User> _childs = new ArrayList<>();

        public ArrayList<User> getChilds() {
            return _childs;
        }

        public void addChild(User child) {
            _childs.add(child);
            _cs.add(child);
        }

        private final ArrayList<User> _cs = new ArrayList<>();

        public ArrayList<User> getCS() {
            return _childs;
        }
    }

    @Test
    public void ObjectToDTO() {

        var user_1 = new User(1, "user_1");
        var user_2 = new User(2, "user_2");
        user_1.setParent(user_2);

        var user_3 = new User(3, "user_3");
        user_1.addChild(user_3);


        var dto = DTObject.readonly("{id,name,childs:[{name}],cs:[],parent,father:{name}}", user_1);

        assertEquals(1, dto.getLong("id"));
        assertEquals("user_1", dto.getString("name"));


        var childs = dto.getList("childs");
        assertEquals(1, childs.size());

        var cs = dto.getList("cs");
        assertEquals(1, cs.size());

        var parent = dto.getObject("parent");
        assertEquals(2, parent.getLong("id"));
        assertEquals("user_2", parent.getString("name"));

        var father = dto.getObject("father");
        assertEquals("user_2", father.getString("name"));
    }

}
